package com.example.musicapi.service;

import com.example.musicapi.model.Regional;
import com.example.musicapi.repository.RegionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class RegionalSyncServiceTest {

    @Autowired
    private RegionalSyncService regionalSyncService;

    @Autowired
    private RegionalRepository regionalRepository;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        regionalRepository.deleteAll();
    }

    @Test
    void shouldSyncNewRegionais() {
        // Cenário: API retorna 2 regionais, banco vazio
        Regional r1 = new Regional(null, 1, "Regional 1", true);
        Regional r2 = new Regional(null, 2, "Regional 2", true);
        Regional[] response = {r1, r2};

        when(restTemplate.getForObject(anyString(), eq(Regional[].class))).thenReturn(response);

        regionalSyncService.syncRegionais();

        List<Regional> active = regionalRepository.findByAtivoTrue();
        assertEquals(2, active.size());
        assertTrue(active.stream().anyMatch(r -> r.getNome().equals("Regional 1")));
        assertTrue(active.stream().anyMatch(r -> r.getNome().equals("Regional 2")));
    }

    @Test
    void shouldInactivateAbsentRegionais() {
        // Cenário: Banco tem 1 e 2. API retorna apenas 2.
        regionalRepository.save(new Regional(null, 1, "Regional 1", true));
        regionalRepository.save(new Regional(null, 2, "Regional 2", true));

        Regional r2 = new Regional(null, 2, "Regional 2", true);
        Regional[] response = {r2};

        when(restTemplate.getForObject(anyString(), eq(Regional[].class))).thenReturn(response);

        regionalSyncService.syncRegionais();

        List<Regional> active = regionalRepository.findByAtivoTrue();
        assertEquals(1, active.size());
        assertEquals(2, active.get(0).getId());

        List<Regional> all = regionalRepository.findAll();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(r -> r.getId() == 1 && !r.isAtivo()));
    }

    @Test
    void shouldInactivateOldAndCreateNewWhenAttributeChanges() {
        // Cenário: Banco tem Regional 1 com nome antigo. API retorna Regional 1 com nome novo.
        regionalRepository.save(new Regional(null, 1, "Nome Antigo", true));

        Regional r1 = new Regional(null, 1, "Nome Novo", true);
        Regional[] response = {r1};

        when(restTemplate.getForObject(anyString(), eq(Regional[].class))).thenReturn(response);

        regionalSyncService.syncRegionais();

        List<Regional> active = regionalRepository.findByAtivoTrue();
        assertEquals(1, active.size());
        assertEquals("Nome Novo", active.get(0).getNome());

        List<Regional> all = regionalRepository.findAll();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(r -> r.getNome().equals("Nome Antigo") && !r.isAtivo()));
        assertTrue(all.stream().anyMatch(r -> r.getNome().equals("Nome Novo") && r.isAtivo()));
    }
}
