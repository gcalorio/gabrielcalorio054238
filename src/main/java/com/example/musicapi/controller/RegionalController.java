package com.example.musicapi.controller;

import com.example.musicapi.model.Regional;
import com.example.musicapi.repository.RegionalRepository;
import com.example.musicapi.service.RegionalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/regionais")
@Tag(name = "Regionais", description = "Endpoints para consulta e sincronização de regionais")
public class RegionalController {

    private final RegionalRepository regionalRepository;
    private final RegionalSyncService regionalSyncService;

    public RegionalController(RegionalRepository regionalRepository, RegionalSyncService regionalSyncService) {
        this.regionalRepository = regionalRepository;
        this.regionalSyncService = regionalSyncService;
    }

    @GetMapping
    @Operation(summary = "Listar regionais", description = "Retorna a lista de regionais importadas da API externa.")
    public List<Regional> getAllRegionais() {
        return regionalRepository.findByAtivoTrue();
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar regionais", description = "Força a sincronização com a API externa.")
    public void sync() {
        regionalSyncService.syncRegionais();
    }
}
