package com.example.musicapi.controller;

import com.example.musicapi.model.Album;
import com.example.musicapi.model.Artist;
import com.example.musicapi.model.ArtistType;
import com.example.musicapi.repository.AlbumRepository;
import com.example.musicapi.repository.ArtistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ArtistController {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    public ArtistController(ArtistRepository artistRepository, AlbumRepository albumRepository) {
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
    }

    @GetMapping("/artists")
    public List<Artist> getAllArtists(@RequestParam(required = false) ArtistType type) {
        if (type != null) {
            return artistRepository.findByType(type);
        }
        return artistRepository.findAll();
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<Artist> getArtistById(@PathVariable Long id) {
        return artistRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/artists")
    public Artist createArtist(@RequestBody Artist artist) {
        // Asegurarse de que los álbumes tengan la referencia al artista si se envían en el POST
        if (artist.getAlbums() != null) {
            artist.getAlbums().forEach(album -> album.setArtist(artist));
        }
        return artistRepository.save(artist);
    }

    @PutMapping("/artists/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable Long id, @RequestBody Artist artistDetails) {
        return artistRepository.findById(id)
                .map(artist -> {
                    artist.setName(artistDetails.getName());
                    if (artistDetails.getAlbums() != null) {
                        // En una implementación real, esto podría ser más complejo (borrar, actualizar, etc.)
                        // Para este caso simple, reemplazamos o actualizamos la lista
                        artistDetails.getAlbums().forEach(album -> album.setArtist(artist));
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
    public Page<Album> getAllAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return albumRepository.findAll(PageRequest.of(page, size));
    }
}
