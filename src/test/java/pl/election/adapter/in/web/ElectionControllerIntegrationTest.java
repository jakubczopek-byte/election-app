package pl.election.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ElectionControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void should_createElectionAndAddOptions_when_validRequests() throws Exception {
        var result = mockMvc.perform(post("/api/elections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mayor Election 2025"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mayor Election 2025"))
                .andReturn();

        var id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(post("/api/elections/" + id + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Candidate A"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Candidate A"));

        mockMvc.perform(get("/api/elections/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votingOptions").isArray())
                .andExpect(jsonPath("$.votingOptions[0].name").value("Candidate A"));
    }

    @Test
    void should_return404_when_electionNotFound() throws Exception {
        mockMvc.perform(get("/api/elections/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ELECTION_NOT_FOUND"));
    }
}
