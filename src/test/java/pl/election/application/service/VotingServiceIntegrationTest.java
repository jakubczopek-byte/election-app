package pl.election.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.election.application.port.in.ElectionResults.OptionResult;
import pl.election.application.port.in.ElectionUseCase;
import pl.election.application.port.in.VoterUseCase;
import pl.election.application.port.in.VotingUseCase;
import pl.election.domain.exception.DuplicateVoteException;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.exception.VoterBlockedException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.exception.VotingOptionNotFoundException;
import pl.election.domain.model.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VotingServiceIntegrationTest extends BaseUseCaseIntegrationTest {

    @Autowired
    private VotingUseCase votingUseCase;
    @Autowired
    private VoterUseCase voterUseCase;
    @Autowired
    private ElectionUseCase electionUseCase;

    private Voter activeVoter;
    private Election election;
    private VotingOption optionA;
    private VotingOption optionB;

    @BeforeEach
    void setUp() {
        var suffix = System.nanoTime();
        activeVoter = voterUseCase.createVoter("Voting Tester", "voting-svc-" + suffix + "@example.com");
        election = electionUseCase.createElection("Voting Integration Election " + suffix);
        optionA = electionUseCase.addVotingOption(election.id(), "Option Alpha");
        optionB = electionUseCase.addVotingOption(election.id(), "Option Beta");
        election = electionUseCase.getElection(election.id());
    }

    @Test
    void should_castVote_when_allConditionsMet() {
        // when
        var vote = votingUseCase.castVote(activeVoter.id(), election.id(), optionA.id());

        // then
        assertThat(vote.voterId()).isEqualTo(activeVoter.id());
        assertThat(vote.electionId()).isEqualTo(election.id());
        assertThat(vote.votingOptionId()).isEqualTo(optionA.id());
        assertThat(vote.castAt()).isNotNull();
    }

    @Test
    void should_throwDuplicateVote_when_voterVotesTwice() {
        // given
        votingUseCase.castVote(activeVoter.id(), election.id(), optionA.id());

        // when/then
        assertThatThrownBy(() -> votingUseCase.castVote(activeVoter.id(), election.id(), optionB.id()))
                .isInstanceOf(DuplicateVoteException.class);
    }

    @Test
    void should_throwVoterBlocked_when_blockedVoterVotes() {
        // given
        voterUseCase.blockVoter(activeVoter.id());

        // when/then
        assertThatThrownBy(() -> votingUseCase.castVote(activeVoter.id(), election.id(), optionA.id()))
                .isInstanceOf(VoterBlockedException.class);
    }

    @Test
    void should_throwVoterNotFound_when_nonExistentVoterVotes() {
        // given
        var fakeVoterId = VoterId.of(UUID.randomUUID());

        // when/then
        assertThatThrownBy(() -> votingUseCase.castVote(fakeVoterId, election.id(), optionA.id()))
                .isInstanceOf(VoterNotFoundException.class);
    }

    @Test
    void should_throwElectionNotFound_when_nonExistentElection() {
        // given
        var fakeElectionId = ElectionId.of(UUID.randomUUID());

        // when/then
        assertThatThrownBy(() -> votingUseCase.castVote(activeVoter.id(), fakeElectionId, optionA.id()))
                .isInstanceOf(ElectionNotFoundException.class);
    }

    @Test
    void should_throwOptionNotFound_when_invalidOption() {
        // given
        var fakeOptionId = VotingOptionId.of(UUID.randomUUID());

        // when/then
        assertThatThrownBy(() -> votingUseCase.castVote(activeVoter.id(), election.id(), fakeOptionId))
                .isInstanceOf(VotingOptionNotFoundException.class);
    }

    @Test
    void should_returnCorrectResults_when_multipleVotersVote() {
        // given
        var suffix = System.nanoTime();
        var voter2 = voterUseCase.createVoter("Voter Two", "voter2-svc-" + suffix + "@example.com");
        var voter3 = voterUseCase.createVoter("Voter Three", "voter3-svc-" + suffix + "@example.com");

        votingUseCase.castVote(activeVoter.id(), election.id(), optionA.id());
        votingUseCase.castVote(voter2.id(), election.id(), optionA.id());
        votingUseCase.castVote(voter3.id(), election.id(), optionB.id());

        // when
        var results = votingUseCase.getResults(election.id());

        // then
        assertThat(results.electionId()).isEqualTo(election.id());
        assertThat(results.electionName()).isEqualTo(election.name());
        assertThat(results.results()).hasSize(2);

        var alphaResult = results.results().stream()
                .filter(r -> r.optionName().equals("Option Alpha"))
                .findFirst()
                .orElseThrow();
        assertThat(alphaResult.voteCount()).isEqualTo(2L);

        var betaResult = results.results().stream()
                .filter(r -> r.optionName().equals("Option Beta"))
                .findFirst()
                .orElseThrow();
        assertThat(betaResult.voteCount()).isEqualTo(1L);
    }

    @Test
    void should_returnZeroCounts_when_noVotesCast() {
        // when
        var results = votingUseCase.getResults(election.id());

        // then
        assertThat(results.results()).hasSize(2)
                .allSatisfy(r -> assertThat(r.voteCount()).isZero());
    }

    @Test
    void should_throwElectionNotFound_when_gettingResultsForNonExistent() {
        // given
        var fakeElectionId = ElectionId.of(UUID.randomUUID());

        // when/then
        assertThatThrownBy(() -> votingUseCase.getResults(fakeElectionId))
                .isInstanceOf(ElectionNotFoundException.class);
    }

    @Test
    void should_allowVotingAfterUnblock_when_voterWasBlocked() {
        // given
        voterUseCase.blockVoter(activeVoter.id());
        assertThatThrownBy(() -> votingUseCase.castVote(activeVoter.id(), election.id(), optionA.id()))
                .isInstanceOf(VoterBlockedException.class);

        // when
        voterUseCase.unblockVoter(activeVoter.id());
        var vote = votingUseCase.castVote(activeVoter.id(), election.id(), optionA.id());

        // then
        assertThat(vote.voterId()).isEqualTo(activeVoter.id());
    }
}
