package com.example.musicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
class MusicApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/artists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateAndReturnToken() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"password\"}";
        String response = mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwttoken").exists())
                .andReturn().getResponse().getContentAsString();

        String token = response.substring(response.indexOf(":") + 2, response.lastIndexOf("\""));

        mockMvc.perform(get("/api/artists")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRefreshToken() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"password\"}";
        String response = mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = response.substring(response.indexOf(":") + 2, response.lastIndexOf("\""));

        mockMvc.perform(get("/api/refresh")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwttoken").exists());
    }

    private String getAccessToken() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"password\"}";
        String response = mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return response.substring(response.indexOf(":") + 2, response.lastIndexOf("\""));
    }

    @Test
    void shouldCreateArtist() throws Exception {
        String token = getAccessToken();
        String artistJson = "{\"name\":\"New Artist\",\"type\":\"SOLO\",\"albums\":[{\"title\":\"Album 1\"}]}";

        mockMvc.perform(post("/api/artists")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(artistJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Artist")))
                .andExpect(jsonPath("$.albums", hasSize(1)))
                .andExpect(jsonPath("$.albums[0].title", is("Album 1")));
    }

    @Test
    void shouldUpdateArtist() throws Exception {
        String token = getAccessToken();
        // El ID 1 debería existir por la inicialización de datos (Serj Tankian)
        String updateJson = "{\"name\":\"Updated Artist Name\"}";

        mockMvc.perform(put("/api/artists/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Artist Name")));
    }

    @Test
    void shouldGetArtistById() throws Exception {
        String token = getAccessToken();

        mockMvc.perform(get("/api/artists/2")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    void shouldGetAlbumsWithPagination() throws Exception {
        String token = getAccessToken();

        // Temos 13 álbuns nos dados iniciais (3+4+3+3)
        // No entanto, o teste anterior 'shouldCreateArtist' adiciona mais 1 álbum, totalizando 14
        mockMvc.perform(get("/api/albums?page=0&size=5")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.totalElements").value(14));

        mockMvc.perform(get("/api/albums?page=2&size=5")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4))); // Página 3 (índice 2) deve ter os últimos 4 álbuns (14 total, 5+5+4)
    }

    @Test
    void shouldFilterArtistsByType() throws Exception {
        String token = getAccessToken();

        // Filtra por SOLO (Serj Tankian, Mike Shinoda, Michel Teló + 1 do teste shouldCreateArtist)
        mockMvc.perform(get("/api/artists?type=SOLO")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        // Filtra por BAND (Guns N’ Roses)
        mockMvc.perform(get("/api/artists?type=BAND")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Guns N’ Roses")));
    }
}
