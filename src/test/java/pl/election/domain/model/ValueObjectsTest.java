package pl.election.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValueObjectsTest {

    private static final UUID FIXED_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    void should_createVoterId_when_calledWithUuid() {
        // when
        var id = VoterId.of(FIXED_UUID);

        // then
        assertThat(id.value()).isEqualTo(FIXED_UUID);
    }

    @Test
    void should_generateUniqueVoterId_when_calledMultipleTimes() {
        // when
        var id1 = VoterId.generate();
        var id2 = VoterId.generate();

        // then
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotEqualTo(id2.value());
    }

    @Test
    void should_beEqual_when_sameVoterIdUuid() {
        // given
        var id1 = VoterId.of(FIXED_UUID);
        var id2 = VoterId.of(FIXED_UUID);

        // then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void should_createElectionId_when_calledWithUuid() {
        // when
        var id = ElectionId.of(FIXED_UUID);

        // then
        assertThat(id.value()).isEqualTo(FIXED_UUID);
    }

    @Test
    void should_generateUniqueElectionId_when_calledMultipleTimes() {
        // when
        var id1 = ElectionId.generate();
        var id2 = ElectionId.generate();

        // then
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void should_beEqual_when_sameElectionIdUuid() {
        // given
        var id1 = ElectionId.of(FIXED_UUID);
        var id2 = ElectionId.of(FIXED_UUID);

        // then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void should_createVotingOptionId_when_calledWithUuid() {
        // when
        var id = VotingOptionId.of(FIXED_UUID);

        // then
        assertThat(id.value()).isEqualTo(FIXED_UUID);
    }

    @Test
    void should_generateUniqueVotingOptionId_when_calledMultipleTimes() {
        // when
        var id1 = VotingOptionId.generate();
        var id2 = VotingOptionId.generate();

        // then
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void should_beEqual_when_sameVotingOptionIdUuid() {
        // given
        var id1 = VotingOptionId.of(FIXED_UUID);
        var id2 = VotingOptionId.of(FIXED_UUID);

        // then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void should_createVoteId_when_calledWithUuid() {
        // when
        var id = VoteId.of(FIXED_UUID);

        // then
        assertThat(id.value()).isEqualTo(FIXED_UUID);
    }

    @Test
    void should_generateUniqueVoteId_when_calledMultipleTimes() {
        // when
        var id1 = VoteId.generate();
        var id2 = VoteId.generate();

        // then
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void should_beEqual_when_sameVoteIdUuid() {
        // given
        var id1 = VoteId.of(FIXED_UUID);
        var id2 = VoteId.of(FIXED_UUID);

        // then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void should_notBeEqual_when_differentValueObjectTypes() {
        // given
        var voterId = VoterId.of(FIXED_UUID);
        var electionId = ElectionId.of(FIXED_UUID);

        // then - different types, even with same UUID
        assertThat(voterId).isNotEqualTo(electionId);
    }
}
