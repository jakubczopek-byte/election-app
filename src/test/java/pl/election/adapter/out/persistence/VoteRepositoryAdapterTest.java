package pl.election.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.election.adapter.out.persistence.adapter.ElectionRepositoryAdapter;
import pl.election.adapter.out.persistence.adapter.VoteRepositoryAdapter;
import pl.election.adapter.out.persistence.adapter.VoterRepositoryAdapter;
import pl.election.domain.model.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VoteRepositoryAdapterTest extends BaseRepositoryTest {

    @Autowired
    private VoteRepositoryAdapter voteRepository;
    @Autowired
    private VoterRepositoryAdapter voterRepository;
    @Autowired
    private ElectionRepositoryAdapter electionRepository;

    private Voter savedVoter;
    private Election savedElection;
    private VotingOption savedOption;

    @BeforeEach
    void setUp() {
        savedVoter = voterRepository.save(
                Voter.create(VoterId.generate(), "Test Voter", "vote-adapter-" + System.nanoTime() + "@example.com", Instant.now()));
        var election = Election.create(ElectionId.generate(), "Vote Adapter Election", Instant.now());
        var option = VotingOption.create(VotingOptionId.generate(), "Test Option");
        var withOption = election.addVotingOption(option);
        savedElection = electionRepository.save(withOption);
        savedOption = savedElection.votingOptions().getFirst();
    }

    @Test
    void should_persistAndRetrieveVote_when_validDomainObject() {
        // given
        var vote = Vote.cast(VoteId.generate(), savedVoter.id(), savedElection.id(), savedOption.id(), Instant.now());

        // when
        var saved = voteRepository.save(vote);

        // then
        assertThat(saved.id()).isEqualTo(vote.id());
        assertThat(saved.voterId()).isEqualTo(savedVoter.id());
        assertThat(saved.electionId()).isEqualTo(savedElection.id());
        assertThat(saved.votingOptionId()).isEqualTo(savedOption.id());
    }

    @Test
    void should_returnTrue_when_voterAlreadyVotedInElection() {
        // given
        var vote = Vote.cast(VoteId.generate(), savedVoter.id(), savedElection.id(), savedOption.id(), Instant.now());
        voteRepository.save(vote);

        // when
        var exists = voteRepository.existsByVoterIdAndElectionId(savedVoter.id(), savedElection.id());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void should_returnFalse_when_voterHasNotVotedInElection() {
        // when
        var exists = voteRepository.existsByVoterIdAndElectionId(savedVoter.id(), savedElection.id());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void should_findVotesByElection_when_votesExist() {
        // given
        var vote = Vote.cast(VoteId.generate(), savedVoter.id(), savedElection.id(), savedOption.id(), Instant.now());
        voteRepository.save(vote);

        // when
        var found = voteRepository.findByElectionId(savedElection.id());

        // then
        assertThat(found).hasSize(1)
                .first()
                .satisfies(v -> assertThat(v.voterId()).isEqualTo(savedVoter.id()));
    }

    @Test
    void should_returnEmptyList_when_noVotesForElection() {
        // when
        var found = voteRepository.findByElectionId(savedElection.id());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void should_countVotesGroupedByOption_when_multipleVotersVote() {
        // given
        var optionB = VotingOption.create(VotingOptionId.generate(), "Option B");
        var updatedElection = savedElection.addVotingOption(optionB);
        savedElection = electionRepository.save(updatedElection);
        var secondOption = savedElection.votingOptions().stream()
                .filter(o -> o.name().equals("Option B"))
                .findFirst()
                .orElseThrow();

        var voter2 = voterRepository.save(
                Voter.create(VoterId.generate(), "Voter Two", "voter2-adapter-" + System.nanoTime() + "@example.com", Instant.now()));
        var voter3 = voterRepository.save(
                Voter.create(VoterId.generate(), "Voter Three", "voter3-adapter-" + System.nanoTime() + "@example.com", Instant.now()));

        voteRepository.save(Vote.cast(VoteId.generate(), savedVoter.id(), savedElection.id(), savedOption.id(), Instant.now()));
        voteRepository.save(Vote.cast(VoteId.generate(), voter2.id(), savedElection.id(), savedOption.id(), Instant.now()));
        voteRepository.save(Vote.cast(VoteId.generate(), voter3.id(), savedElection.id(), secondOption.id(), Instant.now()));

        // when
        var counts = voteRepository.countByElectionIdGroupByOption(savedElection.id());

        // then
        assertThat(counts).hasSize(2);
        assertThat(counts.get(savedOption.id())).isEqualTo(2L);
        assertThat(counts.get(secondOption.id())).isEqualTo(1L);
    }

    @Test
    void should_returnEmptyMap_when_noVotesForCounting() {
        // when
        var counts = voteRepository.countByElectionIdGroupByOption(savedElection.id());

        // then
        assertThat(counts).isEmpty();
    }
}
