package pl.election.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VoteTest {

    private static final VoteId VOTE_ID = VoteId.generate();
    private static final VoterId VOTER_ID = VoterId.generate();
    private static final ElectionId ELECTION_ID = ElectionId.generate();
    private static final VotingOptionId OPTION_ID = VotingOptionId.generate();
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_createVote_when_validIds() {
        // when
        var vote = Vote.cast(VOTE_ID, VOTER_ID, ELECTION_ID, OPTION_ID, NOW);

        // then
        assertThat(vote)
                .extracting(Vote::id, Vote::voterId, Vote::electionId, Vote::votingOptionId, Vote::castAt)
                .containsExactly(VOTE_ID, VOTER_ID, ELECTION_ID, OPTION_ID, NOW);
    }

    @Test
    void should_beEqual_when_sameFields() {
        // given
        var vote1 = Vote.cast(VOTE_ID, VOTER_ID, ELECTION_ID, OPTION_ID, NOW);
        var vote2 = Vote.cast(VOTE_ID, VOTER_ID, ELECTION_ID, OPTION_ID, NOW);

        // then
        assertThat(vote1).isEqualTo(vote2);
        assertThat(vote1.hashCode()).isEqualTo(vote2.hashCode());
    }

    @Test
    void should_notBeEqual_when_differentVoteId() {
        // given
        var vote1 = Vote.cast(VOTE_ID, VOTER_ID, ELECTION_ID, OPTION_ID, NOW);
        var vote2 = Vote.cast(VoteId.generate(), VOTER_ID, ELECTION_ID, OPTION_ID, NOW);

        // then
        assertThat(vote1).isNotEqualTo(vote2);
    }

    @Test
    void should_preserveAllFields_when_created() {
        // when
        var vote = Vote.cast(VOTE_ID, VOTER_ID, ELECTION_ID, OPTION_ID, NOW);

        // then
        assertThat(vote.id()).isEqualTo(VOTE_ID);
        assertThat(vote.voterId()).isEqualTo(VOTER_ID);
        assertThat(vote.electionId()).isEqualTo(ELECTION_ID);
        assertThat(vote.votingOptionId()).isEqualTo(OPTION_ID);
        assertThat(vote.castAt()).isEqualTo(NOW);
    }
}
