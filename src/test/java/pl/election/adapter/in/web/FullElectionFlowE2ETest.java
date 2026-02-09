package pl.election.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FullElectionFlowE2ETest extends BaseIntegrationTest {

    @Test
    void should_completeFullElectionFlow_when_allStepsValid() throws Exception {
        // given - create election
        var electionBody = extractBody(mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Full Flow Election"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        var electionId = extractId(electionBody);

        // given - add voting options
        var optionABody = extractBody(mockMvc.perform(post("/api/elections/" + electionId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Option A"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        var optionAId = extractId(optionABody);

        var optionBBody = extractBody(mockMvc.perform(post("/api/elections/" + electionId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Option B"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        var optionBId = extractId(optionBBody);

        // given - create voters
        var voter1Body = extractBody(mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Voter One","email":"e2e-voter1-%s@example.com"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn());
        var voter1Id = extractId(voter1Body);

        var voter2Body = extractBody(mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Voter Two","email":"e2e-voter2-%s@example.com"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn());
        var voter2Id = extractId(voter2Body);

        var voter3Body = extractBody(mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Voter Three","email":"e2e-voter3-%s@example.com"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn());
        var voter3Id = extractId(voter3Body);

        // when - cast votes
        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votePayload(voter1Id, optionAId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votePayload(voter2Id, optionAId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votePayload(voter3Id, optionBId)))
                .andExpect(status().isCreated());

        // then - verify results
        mockMvc.perform(get("/api/elections/" + electionId + "/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.electionName").value("Full Flow Election"))
                .andExpect(jsonPath("$.totalVotes").value(3))
                .andExpect(jsonPath("$.results.length()").value(2));

        // then - verify election details
        mockMvc.perform(get("/api/elections/" + electionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votingOptions.length()").value(2));

        // then - verify voter list
        mockMvc.perform(get("/api/voters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }

    @Test
    void should_preventBlockedVoterFromVoting_when_fullFlow() throws Exception {
        // given - setup election with option
        var electionBody = extractBody(mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Block Flow Election"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        var electionId = extractId(electionBody);

        var optionBody = extractBody(mockMvc.perform(post("/api/elections/" + electionId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Only Option"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        var optionId = extractId(optionBody);

        // given - create and block voter
        var voterBody = extractBody(mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Blocked Voter","email":"e2e-blocked-%s@example.com"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn());
        var voterId = extractId(voterBody);

        mockMvc.perform(patch("/api/voters/" + voterId + "/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        // when - blocked voter tries to vote
        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votePayload(voterId, optionId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("VOTER_BLOCKED"));

        // when - unblock and vote
        mockMvc.perform(patch("/api/voters/" + voterId + "/unblock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/elections/" + electionId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votePayload(voterId, optionId)))
                .andExpect(status().isCreated());

        // then - verify results
        mockMvc.perform(get("/api/elections/" + electionId + "/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1));
    }

    @Test
    void should_return404ForAllEntities_when_nonExistentIds() throws Exception {
        var fakeId = UUID.randomUUID().toString();

        // when/then
        mockMvc.perform(get("/api/voters/" + fakeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("VOTER_NOT_FOUND"));

        mockMvc.perform(get("/api/elections/" + fakeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ELECTION_NOT_FOUND"));

        mockMvc.perform(get("/api/elections/" + fakeId + "/results"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ELECTION_NOT_FOUND"));
    }

    private static String extractBody(org.springframework.test.web.servlet.MvcResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }

    private static String extractId(String body) {
        return JsonPath.read(body, "$.id").toString();
    }

    private static String votePayload(String voterId, String optionId) {
        return """
                {"voterId":"%s","votingOptionId":"%s"}
                """.formatted(voterId, optionId);
    }
}
