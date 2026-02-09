package pl.election.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.election.adapter.in.web.mapper.ElectionWebMapperImpl;
import pl.election.application.port.in.ElectionResults;
import pl.election.application.port.in.ElectionResults.OptionResult;
import pl.election.application.port.in.VotingUseCase;
import pl.election.domain.exception.DuplicateVoteException;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.exception.VoterBlockedException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.exception.VotingOptionNotFoundException;
import pl.election.domain.model.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = VotingController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class)
)
@Import(ElectionWebMapperImpl.class)
class VotingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VotingUseCase votingUseCase;

    private static final UUID ELECTION_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID VOTER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OPTION_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void should_returnCreated_when_validVote() throws Exception {
        // given
        var vote = Vote.cast(
                VoteId.generate(),
                VoterId.of(VOTER_UUID),
                ElectionId.of(ELECTION_UUID),
                VotingOptionId.of(OPTION_UUID),
                Instant.now());
        given(votingUseCase.castVote(
                VoterId.of(VOTER_UUID),
                ElectionId.of(ELECTION_UUID),
                VotingOptionId.of(OPTION_UUID))).willReturn(vote);

        // when/then
        mockMvc.perform(post("/api/elections/" + ELECTION_UUID + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(VOTER_UUID, OPTION_UUID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voterId").value(VOTER_UUID.toString()))
                .andExpect(jsonPath("$.votingOptionId").value(OPTION_UUID.toString()))
                .andExpect(jsonPath("$.electionId").value(ELECTION_UUID.toString()));
    }

    @Test
    void should_return404_when_voterNotFoundForVote() throws Exception {
        // given
        given(votingUseCase.castVote(any(), any(), any()))
                .willThrow(new VoterNotFoundException("Voter not found"));

        // when/then
        mockMvc.perform(post("/api/elections/" + ELECTION_UUID + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(UUID.randomUUID(), OPTION_UUID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("VOTER_NOT_FOUND"));
    }

    @Test
    void should_return404_when_electionNotFoundForVote() throws Exception {
        // given
        given(votingUseCase.castVote(any(), any(), any()))
                .willThrow(new ElectionNotFoundException("Election not found"));

        // when/then
        mockMvc.perform(post("/api/elections/" + UUID.randomUUID() + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(VOTER_UUID, OPTION_UUID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ELECTION_NOT_FOUND"));
    }

    @Test
    void should_return404_when_votingOptionNotFound() throws Exception {
        // given
        given(votingUseCase.castVote(any(), any(), any()))
                .willThrow(new VotingOptionNotFoundException("Option not found"));

        // when/then
        mockMvc.perform(post("/api/elections/" + ELECTION_UUID + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(VOTER_UUID, UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("VOTING_OPTION_NOT_FOUND"));
    }

    @Test
    void should_return409_when_voterBlocked() throws Exception {
        // given
        given(votingUseCase.castVote(any(), any(), any()))
                .willThrow(new VoterBlockedException("Voter is blocked"));

        // when/then
        mockMvc.perform(post("/api/elections/" + ELECTION_UUID + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(VOTER_UUID, OPTION_UUID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("VOTER_BLOCKED"));
    }

    @Test
    void should_return409_when_duplicateVote() throws Exception {
        // given
        given(votingUseCase.castVote(any(), any(), any()))
                .willThrow(new DuplicateVoteException("Already voted"));

        // when/then
        mockMvc.perform(post("/api/elections/" + ELECTION_UUID + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(VOTER_UUID, OPTION_UUID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_VOTE"));
    }

    @Test
    void should_returnResults_when_electionExists() throws Exception {
        // given
        var optionId = VotingOptionId.of(OPTION_UUID);
        var results = new ElectionResults(
                ElectionId.of(ELECTION_UUID),
                "Test Election",
                List.of(new OptionResult(optionId, "Option A", 5)));
        given(votingUseCase.getResults(ElectionId.of(ELECTION_UUID))).willReturn(results);

        // when/then
        mockMvc.perform(get("/api/elections/" + ELECTION_UUID + "/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.electionName").value("Test Election"))
                .andExpect(jsonPath("$.totalVotes").value(5))
                .andExpect(jsonPath("$.results[0].optionName").value("Option A"))
                .andExpect(jsonPath("$.results[0].voteCount").value(5))
                .andExpect(jsonPath("$.results[0].percentage").value(100.0));
    }

    @Test
    void should_returnEmptyResults_when_noVotesCast() throws Exception {
        // given
        var results = new ElectionResults(
                ElectionId.of(ELECTION_UUID),
                "Empty Election",
                List.of(new OptionResult(VotingOptionId.of(OPTION_UUID), "Option A", 0)));
        given(votingUseCase.getResults(ElectionId.of(ELECTION_UUID))).willReturn(results);

        // when/then
        mockMvc.perform(get("/api/elections/" + ELECTION_UUID + "/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(0))
                .andExpect(jsonPath("$.results[0].voteCount").value(0))
                .andExpect(jsonPath("$.results[0].percentage").value(0.0));
    }

    @Test
    void should_return400_when_missingVoterId() throws Exception {
        mockMvc.perform(post("/api/elections/" + ELECTION_UUID + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"votingOptionId":"%s"}
                                """.formatted(OPTION_UUID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void should_return400_when_missingVotingOptionId() throws Exception {
        mockMvc.perform(post("/api/elections/" + ELECTION_UUID + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s"}
                                """.formatted(VOTER_UUID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
