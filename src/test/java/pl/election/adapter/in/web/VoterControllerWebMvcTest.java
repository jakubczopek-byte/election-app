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
import pl.election.adapter.in.web.mapper.VoterWebMapperImpl;
import pl.election.application.port.in.VoterUseCase;
import pl.election.domain.exception.DuplicateEmailException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = VoterController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class)
)
@Import(VoterWebMapperImpl.class)
class VoterControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoterUseCase voterUseCase;

    private static final UUID VOTER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final VoterId VOTER_ID = VoterId.of(VOTER_UUID);
    private static final Instant NOW = Instant.parse("2025-01-01T12:00:00Z");

    @Test
    void should_returnCreated_when_validVoterRequest() throws Exception {
        // given
        var voter = Voter.create(VOTER_ID, "Jan Kowalski", "jan@example.com", NOW);
        given(voterUseCase.createVoter("Jan Kowalski", "jan@example.com")).willReturn(voter);

        // when/then
        mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jan Kowalski","email":"jan@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(VOTER_UUID.toString()))
                .andExpect(jsonPath("$.name").value("Jan Kowalski"))
                .andExpect(jsonPath("$.email").value("jan@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {"name":"","email":"jan@example.com"}
            """,
            """
            {"name":"J","email":"jan@example.com"}
            """,
            """
            {"name":"Jan","email":"not-an-email"}
            """
    })
    void should_return400_when_invalidInput(String body) throws Exception {
        mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void should_return409_when_duplicateEmail() throws Exception {
        // given
        given(voterUseCase.createVoter(any(), any())).willThrow(new DuplicateEmailException("Email exists"));

        // when/then
        mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jan Kowalski","email":"dup@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"));
    }

    @Test
    void should_returnVoter_when_existsById() throws Exception {
        // given
        var voter = Voter.create(VOTER_ID, "Anna Nowak", "anna@example.com", NOW);
        given(voterUseCase.getVoter(VOTER_ID)).willReturn(voter);

        // when/then
        mockMvc.perform(get("/api/voters/" + VOTER_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anna Nowak"))
                .andExpect(jsonPath("$.email").value("anna@example.com"));
    }

    @Test
    void should_return404_when_voterNotFound() throws Exception {
        // given
        given(voterUseCase.getVoter(any())).willThrow(new VoterNotFoundException("Not found"));

        // when/then
        mockMvc.perform(get("/api/voters/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("VOTER_NOT_FOUND"));
    }

    @Test
    void should_returnAllVoters_when_requested() throws Exception {
        // given
        var voter1 = Voter.create(VoterId.generate(), "First", "first@example.com", NOW);
        var voter2 = Voter.create(VoterId.generate(), "Second", "second@example.com", NOW);
        given(voterUseCase.getAllVoters()).willReturn(List.of(voter1, voter2));

        // when/then
        mockMvc.perform(get("/api/voters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("First"))
                .andExpect(jsonPath("$[1].name").value("Second"));
    }

    @Test
    void should_returnBlockedVoter_when_blockCalled() throws Exception {
        // given
        var blockedVoter = Voter.create(VOTER_ID, "Jan", "jan@example.com", NOW).block();
        given(voterUseCase.blockVoter(VOTER_ID)).willReturn(blockedVoter);

        // when/then
        mockMvc.perform(patch("/api/voters/" + VOTER_UUID + "/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void should_returnActiveVoter_when_unblockCalled() throws Exception {
        // given
        var activeVoter = Voter.create(VOTER_ID, "Jan", "jan@example.com", NOW);
        given(voterUseCase.unblockVoter(VOTER_ID)).willReturn(activeVoter);

        // when/then
        mockMvc.perform(patch("/api/voters/" + VOTER_UUID + "/unblock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
