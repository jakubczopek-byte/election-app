package pl.election.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.election.adapter.out.persistence.adapter.VoterRepositoryAdapter;
import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.election.domain.model.VoterStatus.ACTIVE;
import static pl.election.domain.model.VoterStatus.BLOCKED;

class VoterRepositoryAdapterTest extends BaseRepositoryTest {

    @Autowired
    private VoterRepositoryAdapter voterRepository;

    @Test
    void should_persistAndRetrieveVoter_when_validDomainObject() {
        // given
        var voter = Voter.create(VoterId.generate(), "Jan Kowalski", "jan-repo@example.com", Instant.now());

        // when
        var saved = voterRepository.save(voter);
        var found = voterRepository.findById(saved.id());

        // then
        assertThat(found).isPresent().hasValueSatisfying(v -> {
            assertThat(v.name()).isEqualTo("Jan Kowalski");
            assertThat(v.email()).isEqualTo("jan-repo@example.com");
            assertThat(v.status()).isEqualTo(ACTIVE);
            assertThat(v.id()).isEqualTo(saved.id());
        });
    }

    @Test
    void should_returnEmpty_when_voterNotExists() {
        // given
        var nonExistentId = VoterId.of(UUID.randomUUID());

        // when
        var found = voterRepository.findById(nonExistentId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void should_returnTrue_when_emailAlreadyExists() {
        // given
        var voter = Voter.create(VoterId.generate(), "Anna Nowak", "anna-dup@example.com", Instant.now());
        voterRepository.save(voter);

        // when
        var exists = voterRepository.existsByEmail("anna-dup@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void should_returnFalse_when_emailNotRegistered() {
        // when
        var exists = voterRepository.existsByEmail("nonexistent-repo@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void should_returnAllVoters_when_multipleExist() {
        // given
        var voter1 = Voter.create(VoterId.generate(), "Voter One", "voter1-repo@example.com", Instant.now());
        var voter2 = Voter.create(VoterId.generate(), "Voter Two", "voter2-repo@example.com", Instant.now());
        voterRepository.save(voter1);
        voterRepository.save(voter2);

        // when
        var all = voterRepository.findAll();

        // then
        assertThat(all).hasSizeGreaterThanOrEqualTo(2)
                .extracting(Voter::email)
                .contains("voter1-repo@example.com", "voter2-repo@example.com");
    }

    @Test
    void should_persistBlockedStatus_when_voterBlocked() {
        // given
        var voter = Voter.create(VoterId.generate(), "Blocked One", "blocked-repo@example.com", Instant.now());
        var saved = voterRepository.save(voter);

        // when
        var blocked = saved.block();
        voterRepository.save(blocked);
        var found = voterRepository.findById(saved.id());

        // then
        assertThat(found).isPresent().hasValueSatisfying(v ->
                assertThat(v.status()).isEqualTo(BLOCKED));
    }

    @Test
    void should_restoreActiveStatus_when_voterUnblocked() {
        // given
        var voter = Voter.create(VoterId.generate(), "Unblock One", "unblock-repo@example.com", Instant.now());
        var saved = voterRepository.save(voter);
        var blocked = saved.block();
        voterRepository.save(blocked);

        // when
        var unblocked = blocked.unblock();
        voterRepository.save(unblocked);
        var found = voterRepository.findById(saved.id());

        // then
        assertThat(found).isPresent().hasValueSatisfying(v ->
                assertThat(v.status()).isEqualTo(ACTIVE));
    }
}
