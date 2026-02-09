package pl.election.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.election.application.port.out.ClockPort;
import pl.election.application.port.out.IdGeneratorPort;
import pl.election.application.port.out.VoterRepository;
import pl.election.domain.exception.DuplicateEmailException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class VoterServiceTest {

    @Mock
    private VoterRepository voterRepository;
    @Mock
    private IdGeneratorPort idGenerator;
    @Mock
    private ClockPort clock;
    @InjectMocks
    private VoterService voterService;

    private static final VoterId VOTER_ID = VoterId.generate();
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_createVoter_when_emailNotDuplicate() {
        // given
        given(voterRepository.existsByEmail("jan@example.com")).willReturn(false);
        given(idGenerator.generateVoterId()).willReturn(VOTER_ID);
        given(clock.now()).willReturn(NOW);
        given(voterRepository.save(any(Voter.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var voter = voterService.createVoter("Jan Kowalski", "jan@example.com");

        // then
        assertThat(voter)
                .extracting(Voter::name, Voter::email, Voter::id, Voter::createdAt)
                .containsExactly("Jan Kowalski", "jan@example.com", VOTER_ID, NOW);
        then(voterRepository).should().save(any(Voter.class));
    }

    @Test
    void should_throwDuplicateEmail_when_emailExists() {
        // given
        given(voterRepository.existsByEmail("jan@example.com")).willReturn(true);

        // when / then
        assertThatThrownBy(() -> voterService.createVoter("Jan Kowalski", "jan@example.com"))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("jan@example.com");
    }

    @Test
    void should_notSaveVoter_when_emailDuplicate() {
        // given
        given(voterRepository.existsByEmail("jan@example.com")).willReturn(true);

        // when
        try {
            voterService.createVoter("Jan Kowalski", "jan@example.com");
        } catch (DuplicateEmailException ignored) {
        }

        // then
        then(voterRepository).should(never()).save(any());
    }

    @Test
    void should_checkEmailFirst_when_creating() {
        // given
        given(voterRepository.existsByEmail("jan@example.com")).willReturn(true);

        // when / then
        assertThatThrownBy(() -> voterService.createVoter("Jan Kowalski", "jan@example.com"))
                .isInstanceOf(DuplicateEmailException.class);
        then(idGenerator).should(never()).generateVoterId();
    }

    @Test
    void should_blockVoter_when_voterExists() {
        // given
        var voter = Voter.create(VOTER_ID, "Jan Kowalski", "jan@example.com", NOW);
        given(voterRepository.findById(VOTER_ID)).willReturn(Optional.of(voter));
        given(voterRepository.save(any(Voter.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var result = voterService.blockVoter(VOTER_ID);

        // then
        assertThat(result.isBlocked()).isTrue();
        then(voterRepository).should().save(any(Voter.class));
    }

    @Test
    void should_throwVoterNotFound_when_blockingNonExistent() {
        // given
        given(voterRepository.findById(VOTER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> voterService.blockVoter(VOTER_ID))
                .isInstanceOf(VoterNotFoundException.class)
                .hasMessageContaining(VOTER_ID.value().toString());
    }

    @Test
    void should_unblockVoter_when_voterExists() {
        // given
        var blockedVoter = Voter.create(VOTER_ID, "Jan Kowalski", "jan@example.com", NOW).block();
        given(voterRepository.findById(VOTER_ID)).willReturn(Optional.of(blockedVoter));
        given(voterRepository.save(any(Voter.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var result = voterService.unblockVoter(VOTER_ID);

        // then
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void should_throwVoterNotFound_when_unblockingNonExistent() {
        // given
        given(voterRepository.findById(VOTER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> voterService.unblockVoter(VOTER_ID))
                .isInstanceOf(VoterNotFoundException.class);
    }

    @Test
    void should_returnVoter_when_found() {
        // given
        var voter = Voter.create(VOTER_ID, "Jan Kowalski", "jan@example.com", NOW);
        given(voterRepository.findById(VOTER_ID)).willReturn(Optional.of(voter));

        // when
        var result = voterService.getVoter(VOTER_ID);

        // then
        assertThat(result).isEqualTo(voter);
    }

    @Test
    void should_throwVoterNotFound_when_notFound() {
        // given
        given(voterRepository.findById(VOTER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> voterService.getVoter(VOTER_ID))
                .isInstanceOf(VoterNotFoundException.class);
    }

    @Test
    void should_returnAllVoters_when_called() {
        // given
        var voters = List.of(
                Voter.create(VoterId.generate(), "Jan", "jan@example.com", NOW),
                Voter.create(VoterId.generate(), "Anna", "anna@example.com", NOW)
        );
        given(voterRepository.findAll()).willReturn(voters);

        // when
        var result = voterService.getAllVoters();

        // then
        assertThat(result).hasSize(2).containsExactlyElementsOf(voters);
    }

    @Test
    void should_returnEmptyList_when_noVotersExist() {
        // given
        given(voterRepository.findAll()).willReturn(List.of());

        // when
        var result = voterService.getAllVoters();

        // then
        assertThat(result).isEmpty();
    }
}
