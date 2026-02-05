package com.example.musicapi.controller;

import com.example.musicapi.model.Album;
import com.example.musicapi.model.Artist;
import com.example.musicapi.model.ArtistType;
import com.example.musicapi.repository.AlbumRepository;
import com.example.musicapi.repository.ArtistRepository;
import com.example.musicapi.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Artists", description = "Gerenciamento de artistas e álbuns")
@SecurityRequirement(name = "bearerAuth")
public class ArtistController {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${upload.path:uploads/covers/}")
    private String uploadPath;

    public ArtistController(ArtistRepository artistRepository, AlbumRepository albumRepository, StorageService storageService, SimpMessagingTemplate messagingTemplate) {
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.storageService = storageService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/artists")
    @Operation(summary = "Listar artistas", description = "Retorna todos os artistas com filtros opcionais de nome e tipo, além de ordenação.")
    public List<Artist> getAllArtists(
            @RequestParam(required = false) ArtistType type,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "asc") String sort) {
        
        Sort.Direction direction = sort.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(direction, "name");

        List<Artist> artists;
        if (name != null) {
            artists = artistRepository.findByNameContainingIgnoreCase(name, sortOrder);
        } else if (type != null) {
            artists = artistRepository.findByType(type, sortOrder);
        } else {
            artists = artistRepository.findAll(sortOrder);
        }

        artists.forEach(this::enrichArtistWithPresignedUrls);
        return artists;
    }

    @GetMapping("/artists/{id}")
    @Operation(summary = "Obter artista por ID", description = "Retorna os detalhes de um artista específico, incluindo seus álbuns com URLs de capa pré-assinadas.")
    public ResponseEntity<Artist> getArtistById(@PathVariable Long id) {
        return artistRepository.findById(id)
                .map(artist -> {
                    enrichArtistWithPresignedUrls(artist);
                    return ResponseEntity.ok(artist);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/artists")
    @Operation(summary = "Criar artista", description = "Cria um novo artista e seus álbuns associados.")
    public Artist createArtist(@RequestBody Artist artist) {
        Artist savedArtist = artistRepository.save(artist);
        
        // Notificar via WebSocket sobre novos álbuns
        if (savedArtist.getAlbums() != null) {
            savedArtist.getAlbums().forEach(album -> 
                messagingTemplate.convertAndSend("/topic/albums", album)
            );
        }
        
        return savedArtist;
    }

    @PutMapping("/artists/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable Long id, @RequestBody Artist artistDetails) {
        return artistRepository.findById(id)
                .map(artist -> {
                    artist.setName(artistDetails.getName());
                    if (artistDetails.getAlbums() != null) {
                        artist.setAlbums(artistDetails.getAlbums());
                    }
                    Artist updatedArtist = artistRepository.save(artist);
                    return ResponseEntity.ok(updatedArtist);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/artists/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        return artistRepository.findById(id)
                .map(artist -> {
                    artistRepository.delete(artist);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/albums")
    @Operation(summary = "Listar álbuns (paginado)", description = "Retorna uma página de álbuns, incluindo as URLs de capa pré-assinadas.")
    public Page<Album> getAllAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Album> albumPage = albumRepository.findAll(PageRequest.of(page, size));
        albumPage.getContent().forEach(this::enrichAlbumWithPresignedUrls);
        return albumPage;
    }

    @PostMapping("/albums/{id}/covers")
    @Operation(summary = "Upload de capas de álbum", description = "Faz upload de uma ou mais imagens para a capa de um álbum específico. Armazena no MinIO e retorna o objeto álbum com as URLs pré-assinadas.")
    public ResponseEntity<Album> uploadAlbumCovers(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files) {
        
        return albumRepository.findById(id).map(album -> {
            try {
                if (album.getCoverUrls() == null) {
                    album.setCoverUrls(new ArrayList<>());
                }

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = storageService.uploadFile(file);
                        album.getCoverUrls().add(fileName);
                    }
                }

                Album savedAlbum = albumRepository.save(album);
                enrichAlbumWithPresignedUrls(savedAlbum);
                return ResponseEntity.ok(savedAlbum);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Album>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    private void enrichArtistWithPresignedUrls(Artist artist) {
        if (artist.getAlbums() != null) {
            artist.getAlbums().forEach(this::enrichAlbumWithPresignedUrls);
        }
    }

    private void enrichAlbumWithPresignedUrls(Album album) {
        if (album.getCoverUrls() != null) {
            List<String> presignedUrls = album.getCoverUrls().stream()
                    .map(storageService::generatePresignedUrl)
                    .collect(Collectors.toList());
            album.setCoverUrls(presignedUrls);
        }
    }
}
