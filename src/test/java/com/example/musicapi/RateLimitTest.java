package com.example.musicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldEnforceRateLimit() throws Exception {
        String token = getAccessToken();

        // 10 requisições devem passar
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/artists")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        // A 11ª deve falhar com 429
        mockMvc.perform(get("/api/v1/artists")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isTooManyRequests());
    }

    private String getAccessToken() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"password\"}";
        String response = mockMvc.perform(post("/api/v1/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        return extractToken(response);
    }

    private String extractToken(String response) {
        int start = response.indexOf("\"jwttoken\":\"") + 12;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
