package pl.election.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.election.application.port.in.ElectionUseCase;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VotingOption;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElectionServiceIntegrationTest extends BaseUseCaseIntegrationTest {

    @Autowired
    private ElectionUseCase electionUseCase;

    @Test
    void should_createAndRetrieveElection_when_validData() {
        // when
        var created = electionUseCase.createElection("Presidential Election");
        var retrieved = electionUseCase.getElection(created.id());

        // then
        assertThat(retrieved.name()).isEqualTo("Presidential Election");
        assertThat(retrieved.votingOptions()).isEmpty();
        assertThat(retrieved.createdAt()).isNotNull();
    }

    @Test
    void should_addVotingOption_when_electionExists() {
        // given
        var election = electionUseCase.createElection("Option Test Election");

        // when
        var option = electionUseCase.addVotingOption(election.id(), "Candidate Alpha");
        var retrieved = electionUseCase.getElection(election.id());

        // then
        assertThat(option.name()).isEqualTo("Candidate Alpha");
        assertThat(retrieved.votingOptions()).hasSize(1)
                .extracting(VotingOption::name)
                .containsExactly("Candidate Alpha");
    }

    @Test
    void should_addMultipleOptions_when_electionExists() {
        // given
        var election = electionUseCase.createElection("Multi Option Election");

        // when
        electionUseCase.addVotingOption(election.id(), "Option A");
        electionUseCase.addVotingOption(election.id(), "Option B");
        electionUseCase.addVotingOption(election.id(), "Option C");
        var retrieved = electionUseCase.getElection(election.id());

        // then
        assertThat(retrieved.votingOptions()).hasSize(3)
                .extracting(VotingOption::name)
                .containsExactlyInAnyOrder("Option A", "Option B", "Option C");
    }

    @Test
    void should_throwElectionNotFound_when_invalidId() {
        // given
        var nonExistentId = ElectionId.of(UUID.randomUUID());

        // when/then
        assertThatThrownBy(() -> electionUseCase.getElection(nonExistentId))
                .isInstanceOf(ElectionNotFoundException.class);
    }

    @Test
    void should_throwElectionNotFound_when_addingOptionToNonExistent() {
        // given
        var nonExistentId = ElectionId.of(UUID.randomUUID());

        // when/then
        assertThatThrownBy(() -> electionUseCase.addVotingOption(nonExistentId, "Option X"))
                .isInstanceOf(ElectionNotFoundException.class);
    }

    @Test
    void should_returnAllElections_when_multipleCreated() {
        // given
        electionUseCase.createElection("List Election A");
        electionUseCase.createElection("List Election B");

        // when
        var all = electionUseCase.getAllElections();

        // then
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }
}
