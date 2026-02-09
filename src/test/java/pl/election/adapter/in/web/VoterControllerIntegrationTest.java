package pl.election.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VoterControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void should_createAndRetrieveVoter_when_validRequest() throws Exception {
        var result = mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jan Kowalski","email":"jan-it1@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jan Kowalski"))
                .andExpect(jsonPath("$.email").value("jan-it1@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var id = com.jayway.jsonpath.JsonPath.read(body, "$.id").toString();

        mockMvc.perform(get("/api/voters/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jan Kowalski"));
    }

    @Test
    void should_return400_when_invalidEmail() throws Exception {
        mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jan Kowalski","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void should_return409_when_duplicateEmail() throws Exception {
        mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jan","email":"dup-it@example.com"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Anna","email":"dup-it@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"));
    }

    @Test
    void should_blockAndUnblockVoter_when_exists() throws Exception {
        var result = mockMvc.perform(post("/api/voters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Anna Nowak","email":"anna-block@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        var id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(patch("/api/voters/" + id + "/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        mockMvc.perform(patch("/api/voters/" + id + "/unblock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
