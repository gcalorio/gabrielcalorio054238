package com.example.musicapi.service;

import com.example.musicapi.model.Regional;
import com.example.musicapi.repository.RegionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class RegionalSyncService {

    private final RegionalRepository regionalRepository;
    private final RestTemplate restTemplate;
    private static final String API_URL = "https://integrador-argus-api.geia.vip/v1/regionais";

    public RegionalSyncService(RegionalRepository regionalRepository, RestTemplate restTemplate) {
        this.regionalRepository = regionalRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public void syncRegionais() {
        try {
            Regional[] response = restTemplate.getForObject(API_URL, Regional[].class);
            if (response == null) return;

            List<Regional> apiRegionais = Arrays.asList(response);
            List<Regional> currentActiveRegionais = regionalRepository.findByAtivoTrue();

            // Map para busca rápida das regionais atuais por ID da API
            java.util.Map<Integer, Regional> currentMap = currentActiveRegionais.stream()
                    .collect(java.util.stream.Collectors.toMap(Regional::getId, r -> r));

            // IDs presentes no endpoint
            java.util.Set<Integer> apiIds = apiRegionais.stream()
                    .map(Regional::getId)
                    .collect(java.util.stream.Collectors.toSet());

            // 1. Novo no endpoint ou Atributo alterado
            for (Regional apiRegional : apiRegionais) {
                Regional current = currentMap.get(apiRegional.getId());

                if (current == null) {
                    // Novo no endpoint -> inserir
                    apiRegional.setInternalId(null);
                    apiRegional.setAtivo(true);
                    regionalRepository.save(apiRegional);
                } else {
                    // Verificar se atributo alterado (nome)
                    if (!current.getNome().equals(apiRegional.getNome())) {
                        // Atributo alterado -> inativar antigo e criar novo registro
                        current.setAtivo(false);
                        regionalRepository.save(current);

                        apiRegional.setInternalId(null);
                        apiRegional.setAtivo(true);
                        regionalRepository.save(apiRegional);
                    }
                }
            }

            // 2. Ausente no endpoint -> inativar
            for (Regional current : currentActiveRegionais) {
                if (!apiIds.contains(current.getId())) {
                    current.setAtivo(false);
                    regionalRepository.save(current);
                }
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Falha ao sincronizar regionais: " + e.getMessage());
        }
    }
}
