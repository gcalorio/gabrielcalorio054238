package com.example.musicapi.controller;

import com.example.musicapi.model.Regional;
import com.example.musicapi.repository.RegionalRepository;
import com.example.musicapi.service.RegionalSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class RegionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegionalRepository regionalRepository;

    @MockBean
    private RegionalSyncService regionalSyncService;

    @Test
    void shouldReturnRegionais() throws Exception {
        Regional regional = new Regional(1L, 1, "Regional Norte", true);
        when(regionalRepository.findByAtivoTrue()).thenReturn(Collections.singletonList(regional));

        mockMvc.perform(get("/v1/regionais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Regional Norte")))
                .andExpect(jsonPath("$[0].ativo", is(true)));
    }

    @Test
    void shouldTriggerSync() throws Exception {
        mockMvc.perform(post("/v1/regionais/sync"))
                .andExpect(status().isOk());
    }
}
