package pl.election.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.election.application.port.out.ClockPort;
import pl.election.application.port.out.ElectionRepository;
import pl.election.application.port.out.IdGeneratorPort;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.model.Election;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VotingOption;
import pl.election.domain.model.VotingOptionId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ElectionServiceTest {

    @Mock
    private ElectionRepository electionRepository;
    @Mock
    private IdGeneratorPort idGenerator;
    @Mock
    private ClockPort clock;
    @InjectMocks
    private ElectionService electionService;

    private static final ElectionId ELECTION_ID = ElectionId.generate();
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_createElection_when_validName() {
        // given
        given(idGenerator.generateElectionId()).willReturn(ELECTION_ID);
        given(clock.now()).willReturn(NOW);
        given(electionRepository.save(any(Election.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var election = electionService.createElection("Mayor Election 2025");

        // then
        assertThat(election)
                .extracting(Election::name, Election::id, Election::createdAt)
                .containsExactly("Mayor Election 2025", ELECTION_ID, NOW);
        assertThat(election.votingOptions()).isEmpty();
        then(electionRepository).should().save(any(Election.class));
    }

    @Test
    void should_addOption_when_electionExists() {
        // given
        var election = Election.create(ELECTION_ID, "Mayor Election 2025", NOW);
        var optionId = VotingOptionId.generate();
        given(electionRepository.findById(ELECTION_ID)).willReturn(Optional.of(election));
        given(idGenerator.generateVotingOptionId()).willReturn(optionId);
        given(electionRepository.save(any(Election.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var option = electionService.addVotingOption(ELECTION_ID, "Candidate A");

        // then
        assertThat(option.name()).isEqualTo("Candidate A");
        assertThat(option.id()).isEqualTo(optionId);
        then(electionRepository).should().save(any(Election.class));
    }

    @Test
    void should_throwElectionNotFound_when_addingOptionToNonExistent() {
        // given
        given(electionRepository.findById(ELECTION_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> electionService.addVotingOption(ELECTION_ID, "Candidate A"))
                .isInstanceOf(ElectionNotFoundException.class)
                .hasMessageContaining(ELECTION_ID.value().toString());
    }

    @Test
    void should_returnElection_when_found() {
        // given
        var election = Election.create(ELECTION_ID, "Mayor Election 2025", NOW);
        given(electionRepository.findById(ELECTION_ID)).willReturn(Optional.of(election));

        // when
        var result = electionService.getElection(ELECTION_ID);

        // then
        assertThat(result).isEqualTo(election);
    }

    @Test
    void should_throwElectionNotFound_when_gettingNonExistent() {
        // given
        given(electionRepository.findById(ELECTION_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> electionService.getElection(ELECTION_ID))
                .isInstanceOf(ElectionNotFoundException.class);
    }

    @Test
    void should_returnAllElections_when_called() {
        // given
        var elections = List.of(
                Election.create(ElectionId.generate(), "E1", NOW),
                Election.create(ElectionId.generate(), "E2", NOW)
        );
        given(electionRepository.findAll()).willReturn(elections);

        // when
        var result = electionService.getAllElections();

        // then
        assertThat(result).hasSize(2).containsExactlyElementsOf(elections);
    }

    @Test
    void should_returnEmptyList_when_noElectionsExist() {
        // given
        given(electionRepository.findAll()).willReturn(List.of());

        // when
        var result = electionService.getAllElections();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void should_addMultipleOptions_when_calledSequentially() {
        // given
        var election = Election.create(ELECTION_ID, "Mayor Election 2025", NOW);
        var optionId1 = VotingOptionId.generate();
        var optionId2 = VotingOptionId.generate();

        given(electionRepository.findById(ELECTION_ID))
                .willReturn(Optional.of(election))
                .willReturn(Optional.of(election.addVotingOption(VotingOption.create(optionId1, "Candidate A"))));
        given(idGenerator.generateVotingOptionId()).willReturn(optionId1).willReturn(optionId2);
        given(electionRepository.save(any(Election.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var option1 = electionService.addVotingOption(ELECTION_ID, "Candidate A");
        var option2 = electionService.addVotingOption(ELECTION_ID, "Candidate B");

        // then
        assertThat(option1.name()).isEqualTo("Candidate A");
        assertThat(option2.name()).isEqualTo("Candidate B");
    }
}
