package pl.election.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VotingOptionTest {

    private static final VotingOptionId ID = VotingOptionId.generate();

    @Test
    void should_createOption_when_validName() {
        // when
        var option = VotingOption.create(ID, "Candidate A");

        // then
        assertThat(option)
                .extracting(VotingOption::id, VotingOption::name)
                .containsExactly(ID, "Candidate A");
    }

    @Test
    void should_stripName_when_hasWhitespace() {
        // when
        var option = VotingOption.create(ID, "  Candidate A  ");

        // then
        assertThat(option.name()).isEqualTo("Candidate A");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void should_throwException_when_nameBlank(String name) {
        assertThatThrownBy(() -> VotingOption.create(ID, name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void should_throwException_when_nameTooShort() {
        assertThatThrownBy(() -> VotingOption.create(ID, "A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 200");
    }

    @Test
    void should_throwException_when_nameTooLong() {
        // given
        var longName = "A".repeat(201);

        // then
        assertThatThrownBy(() -> VotingOption.create(ID, longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 200");
    }

    @Test
    void should_acceptName_when_minLength() {
        // when
        var option = VotingOption.create(ID, "ab");

        // then
        assertThat(option.name()).isEqualTo("ab");
    }

    @Test
    void should_acceptName_when_exactlyMaxLength() {
        // given
        var maxName = "A".repeat(200);

        // when
        var option = VotingOption.create(ID, maxName);

        // then
        assertThat(option.name()).isEqualTo(maxName);
    }

    @Test
    void should_reconstituteOption_when_calledWithAllFields() {
        // when
        var option = VotingOption.reconstitute(ID, "Candidate A");

        // then
        assertThat(option)
                .extracting(VotingOption::id, VotingOption::name)
                .containsExactly(ID, "Candidate A");
    }

    @Test
    void should_beEqual_when_sameIdAndName() {
        // given
        var option1 = VotingOption.create(ID, "Candidate A");
        var option2 = VotingOption.reconstitute(ID, "Candidate A");

        // then
        assertThat(option1).isEqualTo(option2);
    }

    @Test
    void should_notBeEqual_when_differentId() {
        // given
        var option1 = VotingOption.create(ID, "Candidate A");
        var option2 = VotingOption.create(VotingOptionId.generate(), "Candidate A");

        // then
        assertThat(option1).isNotEqualTo(option2);
    }
}
