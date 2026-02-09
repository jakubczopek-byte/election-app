package pl.election.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.election.application.port.out.ClockPort;
import pl.election.application.port.out.ElectionRepository;
import pl.election.application.port.out.IdGeneratorPort;
import pl.election.application.port.out.VoteRepository;
import pl.election.application.port.out.VoterRepository;
import pl.election.domain.exception.DuplicateVoteException;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.exception.VoterBlockedException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.exception.VotingOptionNotFoundException;
import pl.election.domain.model.Election;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoteId;
import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOption;
import pl.election.domain.model.VotingOptionId;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class VotingServiceTest {

    @Mock
    private VoterRepository voterRepository;
    @Mock
    private ElectionRepository electionRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private IdGeneratorPort idGenerator;
    @Mock
    private ClockPort clock;
    @InjectMocks
    private VotingService votingService;

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_castVote_when_allValid() {
        // given
        var voter = Voter.create(VoterId.generate(), "Jan Kowalski", "jan@example.com", NOW);
        var optionId = VotingOptionId.generate();
        var option = VotingOption.create(optionId, "Candidate A");
        var election = Election.create(ElectionId.generate(), "Mayor Election 2025", NOW)
                .addVotingOption(option);
        var voteId = VoteId.generate();

        given(voterRepository.findById(voter.id())).willReturn(Optional.of(voter));
        given(electionRepository.findById(election.id())).willReturn(Optional.of(election));
        given(voteRepository.existsByVoterIdAndElectionId(voter.id(), election.id())).willReturn(false);
        given(idGenerator.generateVoteId()).willReturn(voteId);
        given(clock.now()).willReturn(NOW);
        given(voteRepository.save(any(Vote.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var vote = votingService.castVote(voter.id(), election.id(), optionId);

        // then
        assertThat(vote)
                .extracting(Vote::voterId, Vote::electionId, Vote::votingOptionId, Vote::castAt)
                .containsExactly(voter.id(), election.id(), optionId, NOW);
        then(voteRepository).should().save(any(Vote.class));
    }

    @Test
    void should_throwVoterNotFound_when_voterMissing() {
        // given
        var voterId = VoterId.generate();
        given(voterRepository.findById(voterId)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> votingService.castVote(voterId, ElectionId.generate(), VotingOptionId.generate()))
                .isInstanceOf(VoterNotFoundException.class)
                .hasMessageContaining(voterId.value().toString());
    }

    @Test
    void should_throwVoterBlocked_when_voterBlocked() {
        // given
        var voter = Voter.create(VoterId.generate(), "Jan Kowalski", "jan@example.com", NOW).block();
        given(voterRepository.findById(voter.id())).willReturn(Optional.of(voter));

        // when / then
        assertThatThrownBy(() -> votingService.castVote(voter.id(), ElectionId.generate(), VotingOptionId.generate()))
                .isInstanceOf(VoterBlockedException.class)
                .hasMessageContaining(voter.id().value().toString());
    }

    @Test
    void should_notCheckElection_when_voterBlocked() {
        // given
        var voter = Voter.create(VoterId.generate(), "Jan Kowalski", "jan@example.com", NOW).block();
        given(voterRepository.findById(voter.id())).willReturn(Optional.of(voter));

        // when
        try {
            votingService.castVote(voter.id(), ElectionId.generate(), VotingOptionId.generate());
        } catch (VoterBlockedException ignored) {
        }

        // then
        then(electionRepository).should(never()).findById(any());
    }

    @Test
    void should_throwElectionNotFound_when_electionMissing() {
        // given
        var voter = Voter.create(VoterId.generate(), "Jan Kowalski", "jan@example.com", NOW);
        var electionId = ElectionId.generate();
        given(voterRepository.findById(voter.id())).willReturn(Optional.of(voter));
        given(electionRepository.findById(electionId)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> votingService.castVote(voter.id(), electionId, VotingOptionId.generate()))
                .isInstanceOf(ElectionNotFoundException.class);
    }

    @Test
    void should_throwVotingOptionNotFound_when_optionInvalid() {
        // given
        var voter = Voter.create(VoterId.generate(), "Jan Kowalski", "jan@example.com", NOW);
        var election = Election.create(ElectionId.generate(), "Mayor Election 2025", NOW);
        var invalidOptionId = VotingOptionId.generate();
        given(voterRepository.findById(voter.id())).willReturn(Optional.of(voter));
        given(electionRepository.findById(election.id())).willReturn(Optional.of(election));

        // when / then
        assertThatThrownBy(() -> votingService.castVote(voter.id(), election.id(), invalidOptionId))
                .isInstanceOf(VotingOptionNotFoundException.class);
    }

    @Test
    void should_throwDuplicateVote_when_alreadyVoted() {
        // given
        var voter = Voter.create(VoterId.generate(), "Jan Kowalski", "jan@example.com", NOW);
        var optionId = VotingOptionId.generate();
        var option = VotingOption.create(optionId, "Candidate A");
        var election = Election.create(ElectionId.generate(), "Mayor Election 2025", NOW)
                .addVotingOption(option);
        given(voterRepository.findById(voter.id())).willReturn(Optional.of(voter));
        given(electionRepository.findById(election.id())).willReturn(Optional.of(election));
        given(voteRepository.existsByVoterIdAndElectionId(voter.id(), election.id())).willReturn(true);

        // when / then
        assertThatThrownBy(() -> votingService.castVote(voter.id(), election.id(), optionId))
                .isInstanceOf(DuplicateVoteException.class);
    }

    @Test
    void should_notSaveVote_when_duplicateDetected() {
        // given
        var voter = Voter.create(VoterId.generate(), "Jan Kowalski", "jan@example.com", NOW);
        var optionId = VotingOptionId.generate();
        var option = VotingOption.create(optionId, "Candidate A");
        var election = Election.create(ElectionId.generate(), "Mayor Election 2025", NOW)
                .addVotingOption(option);
        given(voterRepository.findById(voter.id())).willReturn(Optional.of(voter));
        given(electionRepository.findById(election.id())).willReturn(Optional.of(election));
        given(voteRepository.existsByVoterIdAndElectionId(voter.id(), election.id())).willReturn(true);

        // when
        try {
            votingService.castVote(voter.id(), election.id(), optionId);
        } catch (DuplicateVoteException ignored) {
        }

        // then
        then(voteRepository).should(never()).save(any());
    }

    @Test
    void should_returnResults_when_electionExists() {
        // given
        var optionAId = VotingOptionId.generate();
        var optionBId = VotingOptionId.generate();
        var optionA = VotingOption.create(optionAId, "Candidate A");
        var optionB = VotingOption.create(optionBId, "Candidate B");
        var election = Election.create(ElectionId.generate(), "Mayor Election 2025", NOW)
                .addVotingOption(optionA)
                .addVotingOption(optionB);
        given(electionRepository.findById(election.id())).willReturn(Optional.of(election));
        given(voteRepository.countByElectionIdGroupByOption(election.id()))
                .willReturn(Map.of(optionAId, 3L, optionBId, 2L));

        // when
        var results = votingService.getResults(election.id());

        // then
        assertThat(results.electionId()).isEqualTo(election.id());
        assertThat(results.electionName()).isEqualTo("Mayor Election 2025");
        assertThat(results.results()).hasSize(2);
        assertThat(results.results()).extracting("voteCount").containsExactlyInAnyOrder(3L, 2L);
    }

    @Test
    void should_returnZeroVotes_when_noVotesCast() {
        // given
        var optionId = VotingOptionId.generate();
        var option = VotingOption.create(optionId, "Candidate A");
        var election = Election.create(ElectionId.generate(), "Mayor Election 2025", NOW)
                .addVotingOption(option);
        given(electionRepository.findById(election.id())).willReturn(Optional.of(election));
        given(voteRepository.countByElectionIdGroupByOption(election.id())).willReturn(Map.of());

        // when
        var results = votingService.getResults(election.id());

        // then
        assertThat(results.results()).hasSize(1);
        assertThat(results.results().getFirst().voteCount()).isZero();
    }

    @Test
    void should_throwElectionNotFound_when_gettingResultsForNonExistent() {
        // given
        var electionId = ElectionId.generate();
        given(electionRepository.findById(electionId)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> votingService.getResults(electionId))
                .isInstanceOf(ElectionNotFoundException.class);
    }

    @Test
    void should_returnEmptyResults_when_noOptions() {
        // given
        var election = Election.create(ElectionId.generate(), "Mayor Election 2025", NOW);
        given(electionRepository.findById(election.id())).willReturn(Optional.of(election));
        given(voteRepository.countByElectionIdGroupByOption(election.id())).willReturn(Map.of());

        // when
        var results = votingService.getResults(election.id());

        // then
        assertThat(results.results()).isEmpty();
    }
}
