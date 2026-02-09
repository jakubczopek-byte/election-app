package pl.election.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VotingControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void should_castVoteAndGetResults_when_allValid() throws Exception {
        var voterResult = mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jan Kowalski","email":"jan-vote@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var voterId = com.jayway.jsonpath.JsonPath.read(voterResult.getResponse().getContentAsString(), "$.id").toString();

        var electionResult = mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Vote Test Election"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var electionId = com.jayway.jsonpath.JsonPath.read(electionResult.getResponse().getContentAsString(), "$.id").toString();

        var optionResult = mockMvc.perform(post("/api/elections/" + electionId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Option Alpha"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var optionId = com.jayway.jsonpath.JsonPath.read(optionResult.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(voterId, optionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voterId").value(voterId))
                .andExpect(jsonPath("$.votingOptionId").value(optionId));

        mockMvc.perform(get("/api/elections/" + electionId + "/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1))
                .andExpect(jsonPath("$.results[0].optionName").value("Option Alpha"))
                .andExpect(jsonPath("$.results[0].voteCount").value(1));
    }

    @Test
    void should_return409_when_voterBlocked() throws Exception {
        var voterResult = mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Blocked User","email":"blocked-vote@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var voterId = com.jayway.jsonpath.JsonPath.read(voterResult.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(patch("/api/voters/" + voterId + "/block"))
                .andExpect(status().isOk());

        var electionResult = mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Blocked Vote Election"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var electionId = com.jayway.jsonpath.JsonPath.read(electionResult.getResponse().getContentAsString(), "$.id").toString();

        var optionResult = mockMvc.perform(post("/api/elections/" + electionId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Option Beta"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var optionId = com.jayway.jsonpath.JsonPath.read(optionResult.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(voterId, optionId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("VOTER_BLOCKED"));
    }

    @Test
    void should_return409_when_duplicateVote() throws Exception {
        var voterResult = mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dup Voter","email":"dup-vote@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var voterId = com.jayway.jsonpath.JsonPath.read(voterResult.getResponse().getContentAsString(), "$.id").toString();

        var electionResult = mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dup Vote Election"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var electionId = com.jayway.jsonpath.JsonPath.read(electionResult.getResponse().getContentAsString(), "$.id").toString();

        var optionResult = mockMvc.perform(post("/api/elections/" + electionId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Option Gamma"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var optionId = com.jayway.jsonpath.JsonPath.read(optionResult.getResponse().getContentAsString(), "$.id").toString();

        var voteBody = """
                {"voterId":"%s","votingOptionId":"%s"}
                """.formatted(voterId, optionId);

        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(voteBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(voteBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_VOTE"));
    }

    @Test
    void should_return404_when_invalidOption() throws Exception {
        var voterResult = mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid Option User","email":"invalid-opt@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var voterId = com.jayway.jsonpath.JsonPath.read(voterResult.getResponse().getContentAsString(), "$.id").toString();

        var electionResult = mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid Option Election"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var electionId = com.jayway.jsonpath.JsonPath.read(electionResult.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"voterId":"%s","votingOptionId":"%s"}
                                """.formatted(voterId, UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("VOTING_OPTION_NOT_FOUND"));
    }
}
