package pl.election.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.election.adapter.in.web.mapper.ElectionWebMapperImpl;
import pl.election.application.port.in.ElectionUseCase;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.model.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ElectionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class)
)
@Import(ElectionWebMapperImpl.class)
class ElectionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElectionUseCase electionUseCase;

    private static final UUID ELECTION_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final ElectionId ELECTION_ID = ElectionId.of(ELECTION_UUID);
    private static final Instant NOW = Instant.parse("2025-01-01T12:00:00Z");

    @Test
    void should_returnCreated_when_validElectionRequest() throws Exception {
        // given
        var election = Election.create(ELECTION_ID, "Mayor Election", NOW);
        given(electionUseCase.createElection("Mayor Election")).willReturn(election);

        // when/then
        mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mayor Election"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ELECTION_UUID.toString()))
                .andExpect(jsonPath("$.name").value("Mayor Election"))
                .andExpect(jsonPath("$.votingOptions").isArray())
                .andExpect(jsonPath("$.votingOptions").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {"name":""}
            """,
            """
            {"name":"X"}
            """
    })
    void should_return400_when_invalidElectionName(String body) throws Exception {
        mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void should_returnElection_when_existsById() throws Exception {
        // given
        var election = Election.create(ELECTION_ID, "City Council", NOW);
        given(electionUseCase.getElection(ELECTION_ID)).willReturn(election);

        // when/then
        mockMvc.perform(get("/api/elections/" + ELECTION_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("City Council"));
    }

    @Test
    void should_return404_when_electionNotFound() throws Exception {
        // given
        given(electionUseCase.getElection(any())).willThrow(new ElectionNotFoundException("Not found"));

        // when/then
        mockMvc.perform(get("/api/elections/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ELECTION_NOT_FOUND"));
    }

    @Test
    void should_returnAllElections_when_requested() throws Exception {
        // given
        var e1 = Election.create(ElectionId.generate(), "Election A", NOW);
        var e2 = Election.create(ElectionId.generate(), "Election B", NOW);
        given(electionUseCase.getAllElections()).willReturn(List.of(e1, e2));

        // when/then
        mockMvc.perform(get("/api/elections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void should_returnCreatedOption_when_validOptionRequest() throws Exception {
        // given
        var optionId = VotingOptionId.generate();
        var option = VotingOption.create(optionId, "Candidate X");
        given(electionUseCase.addVotingOption(eq(ELECTION_ID), eq("Candidate X"))).willReturn(option);

        // when/then
        mockMvc.perform(post("/api/elections/" + ELECTION_UUID + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Candidate X"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Candidate X"));
    }

    @Test
    void should_return404_when_addingOptionToNonExistentElection() throws Exception {
        // given
        var randomId = UUID.randomUUID();
        given(electionUseCase.addVotingOption(any(), any()))
                .willThrow(new ElectionNotFoundException("Not found"));

        // when/then
        mockMvc.perform(post("/api/elections/" + randomId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Candidate Y"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ELECTION_NOT_FOUND"));
    }
}
