package cinema.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("venue-dev")
class VenueApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void createsCinemaHallAndSeatThenReadsHierarchy() throws Exception {
        String cinemaId = mvc.perform(post("/cinemas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"  Aurora  \",\"address\":\"  Main street  \"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
        String hallId = mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cinemaId\":\"" + cinemaId + "\",\"name\":\"Hall A\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON)
                .content("{\"hallId\":\"" + hallId + "\",\"row\":\" a \",\"number\":1,\"type\":\"NORMAL\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/cinemas/" + cinemaId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aurora"))
                .andExpect(jsonPath("$.address").value("Main street"));
        mvc.perform(get("/cinemas/" + cinemaId + "/halls")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hall A"));
        mvc.perform(get("/halls/" + hallId + "/seats")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].row").value("A"));
    }

    @Test
    void validatesMissingParentsMalformedJsonAndDuplicates() throws Exception {
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON).content("{\"hallId\":\"missing\",\"row\":\"A\",\"number\":1,\"type\":\"NORMAL\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/cinemas").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        String cinemaId = mvc.perform(post("/cinemas").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Conflict Cinema\",\"address\":\"Address\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString().replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
        mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON).content("{\"cinemaId\":\"" + cinemaId + "\",\"name\":\"Same\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON).content("{\"cinemaId\":\"" + cinemaId + "\",\"name\":\" Same \"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void keepsDefaultRoutesClosed() throws Exception {
        mvc.perform(get("/movies")).andExpect(status().isForbidden());
    }
}
