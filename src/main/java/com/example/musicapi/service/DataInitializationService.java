package com.example.musicapi.service;

import com.example.musicapi.model.Album;
import com.example.musicapi.model.Artist;
import com.example.musicapi.model.ArtistType;
import com.example.musicapi.repository.ArtistRepository;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DataInitializationService {

    private final ArtistRepository artistRepository;

    public DataInitializationService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @PostConstruct
    public void init() {
        saveArtistWithAlbums("Serj Tankian", ArtistType.SOLO, Arrays.asList("Harakiri", "Black Blooms", "The Rough Dog"));
        saveArtistWithAlbums("Mike Shinoda", ArtistType.SOLO, Arrays.asList("The Rising Tied", "Post Traumatic", "Post Traumatic EP", "Where’d You Go"));
        saveArtistWithAlbums("Michel Teló", ArtistType.SOLO, Arrays.asList("Bem Sertanejo", "Bem Sertanejo - O Show (Ao Vivo)", "Bem Sertanejo - (1ª Temporada) - EP"));
        saveArtistWithAlbums("Guns N’ Roses", ArtistType.BAND, Arrays.asList("Use Your Illusion I", "Use Your Illusion II", "Greatest Hits"));
    }

    private void saveArtistWithAlbums(String name, ArtistType type, List<String> albumTitles) {
        Artist artist = new Artist();
        artist.setName(name);
        artist.setType(type);
        
        List<Album> albums = new ArrayList<>();
        for (String title : albumTitles) {
            Album album = new Album();
            album.setTitle(title);
            album.setArtist(artist);
            albums.add(album);
        }
        artist.setAlbums(albums);
        artistRepository.save(artist);
    }
}
