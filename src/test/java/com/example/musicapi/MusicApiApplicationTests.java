package com.example.musicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class MusicApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnArtistsAndAlbums() throws Exception {
        mockMvc.perform(get("/api/artists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name").value("Serj Tankian"))
                .andExpect(jsonPath("$[0].albums", hasSize(3)))
                .andExpect(jsonPath("$[1].name").value("Mike Shinoda"))
                .andExpect(jsonPath("$[2].name").value("Michel Teló"))
                .andExpect(jsonPath("$[3].name").value("Guns N’ Roses"));
    }
}
