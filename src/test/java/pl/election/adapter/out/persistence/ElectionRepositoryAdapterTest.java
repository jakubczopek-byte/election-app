package pl.election.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.election.adapter.out.persistence.adapter.ElectionRepositoryAdapter;
import pl.election.domain.model.Election;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VotingOption;
import pl.election.domain.model.VotingOptionId;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionRepositoryAdapterTest extends BaseRepositoryTest {

    @Autowired
    private ElectionRepositoryAdapter electionRepository;

    @Test
    void should_persistAndRetrieveElection_when_validDomainObject() {
        // given
        var election = Election.create(ElectionId.generate(), "Presidential Election", Instant.now());

        // when
        var saved = electionRepository.save(election);
        var found = electionRepository.findById(saved.id());

        // then
        assertThat(found).isPresent().hasValueSatisfying(e -> {
            assertThat(e.name()).isEqualTo("Presidential Election");
            assertThat(e.votingOptions()).isEmpty();
            assertThat(e.id()).isEqualTo(saved.id());
        });
    }

    @Test
    void should_returnEmpty_when_electionNotExists() {
        // given
        var nonExistentId = ElectionId.of(UUID.randomUUID());

        // when
        var found = electionRepository.findById(nonExistentId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void should_persistVotingOptions_when_addedToElection() {
        // given
        var election = Election.create(ElectionId.generate(), "Options Election", Instant.now());
        var saved = electionRepository.save(election);
        var option = VotingOption.create(VotingOptionId.generate(), "Candidate Alpha");
        var withOption = saved.addVotingOption(option);

        // when
        electionRepository.save(withOption);
        var found = electionRepository.findById(saved.id());

        // then
        assertThat(found).isPresent().hasValueSatisfying(e -> {
            assertThat(e.votingOptions()).hasSize(1);
            assertThat(e.votingOptions().getFirst().name()).isEqualTo("Candidate Alpha");
        });
    }

    @Test
    void should_persistMultipleOptions_when_addedSequentially() {
        // given
        var election = Election.create(ElectionId.generate(), "Multi Options", Instant.now());
        var saved = electionRepository.save(election);
        var optionA = VotingOption.create(VotingOptionId.generate(), "Option A");
        var optionB = VotingOption.create(VotingOptionId.generate(), "Option B");
        var optionC = VotingOption.create(VotingOptionId.generate(), "Option C");
        var withOptions = saved.addVotingOption(optionA).addVotingOption(optionB).addVotingOption(optionC);

        // when
        electionRepository.save(withOptions);
        var found = electionRepository.findById(saved.id());

        // then
        assertThat(found).isPresent().hasValueSatisfying(e -> {
            assertThat(e.votingOptions()).hasSize(3)
                    .extracting(VotingOption::name)
                    .containsExactlyInAnyOrder("Option A", "Option B", "Option C");
        });
    }

    @Test
    void should_returnAllElections_when_multipleExist() {
        // given
        var election1 = Election.create(ElectionId.generate(), "Election One", Instant.now());
        var election2 = Election.create(ElectionId.generate(), "Election Two", Instant.now());
        electionRepository.save(election1);
        electionRepository.save(election2);

        // when
        var all = electionRepository.findAll();

        // then
        assertThat(all).hasSizeGreaterThanOrEqualTo(2)
                .extracting(Election::name)
                .contains("Election One", "Election Two");
    }

    @Test
    void should_findOptionById_when_optionPersisted() {
        // given
        var election = Election.create(ElectionId.generate(), "FindOption Election", Instant.now());
        var optionId = VotingOptionId.generate();
        var option = VotingOption.create(optionId, "Findable Option");
        var withOption = election.addVotingOption(option);
        electionRepository.save(withOption);

        // when
        var found = electionRepository.findById(election.id());

        // then
        assertThat(found).isPresent().hasValueSatisfying(e ->
                assertThat(e.hasOption(optionId)).isTrue());
    }
}
