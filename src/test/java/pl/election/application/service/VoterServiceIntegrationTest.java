package pl.election.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.election.application.port.in.VoterUseCase;
import pl.election.domain.exception.DuplicateEmailException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.model.VoterId;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.election.domain.model.VoterStatus.ACTIVE;
import static pl.election.domain.model.VoterStatus.BLOCKED;

class VoterServiceIntegrationTest extends BaseUseCaseIntegrationTest {

    @Autowired
    private VoterUseCase voterUseCase;

    @Test
    void should_createAndRetrieveVoter_when_validData() {
        // given
        var uniqueEmail = "voter-svc-" + System.nanoTime() + "@example.com";

        // when
        var created = voterUseCase.createVoter("Jan Kowalski", uniqueEmail);
        var retrieved = voterUseCase.getVoter(created.id());

        // then
        assertThat(retrieved.name()).isEqualTo("Jan Kowalski");
        assertThat(retrieved.email()).isEqualTo(uniqueEmail);
        assertThat(retrieved.status()).isEqualTo(ACTIVE);
        assertThat(retrieved.createdAt()).isNotNull();
    }

    @Test
    void should_throwDuplicateEmail_when_emailAlreadyRegistered() {
        // given
        var uniqueEmail = "dup-svc-" + System.nanoTime() + "@example.com";
        voterUseCase.createVoter("First", uniqueEmail);

        // when/then
        assertThatThrownBy(() -> voterUseCase.createVoter("Second", uniqueEmail))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void should_blockAndUnblockVoter_when_exists() {
        // given
        var voter = voterUseCase.createVoter("Block Test", "block-svc-" + System.nanoTime() + "@example.com");

        // when
        var blocked = voterUseCase.blockVoter(voter.id());

        // then
        assertThat(blocked.status()).isEqualTo(BLOCKED);
        assertThat(blocked.isBlocked()).isTrue();

        // when
        var unblocked = voterUseCase.unblockVoter(voter.id());

        // then
        assertThat(unblocked.status()).isEqualTo(ACTIVE);
        assertThat(unblocked.isActive()).isTrue();
    }

    @Test
    void should_throwVoterNotFound_when_invalidId() {
        // given
        var nonExistentId = VoterId.of(UUID.randomUUID());

        // when/then
        assertThatThrownBy(() -> voterUseCase.getVoter(nonExistentId))
                .isInstanceOf(VoterNotFoundException.class);
    }

    @Test
    void should_returnAllVoters_when_multipleCreated() {
        // given
        var suffix = System.nanoTime();
        voterUseCase.createVoter("All Test A", "all-svc-a-" + suffix + "@example.com");
        voterUseCase.createVoter("All Test B", "all-svc-b-" + suffix + "@example.com");

        // when
        var all = voterUseCase.getAllVoters();

        // then
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void should_throwVoterNotFound_when_blockingNonExistentVoter() {
        // given
        var nonExistentId = VoterId.of(UUID.randomUUID());

        // when/then
        assertThatThrownBy(() -> voterUseCase.blockVoter(nonExistentId))
                .isInstanceOf(VoterNotFoundException.class);
    }
}
