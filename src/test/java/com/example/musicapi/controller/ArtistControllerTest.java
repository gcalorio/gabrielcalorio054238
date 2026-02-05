package com.example.musicapi.controller;

import com.example.musicapi.model.Album;
import com.example.musicapi.model.Artist;
import com.example.musicapi.model.ArtistType;
import com.example.musicapi.repository.AlbumRepository;
import com.example.musicapi.repository.ArtistRepository;
import com.example.musicapi.service.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistControllerTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ArtistController artistController;

    @Test
    void shouldGetAllArtists() {
        Artist artist = new Artist();
        artist.setName("Artist 1");
        when(artistRepository.findAll(any(Sort.class))).thenReturn(Collections.singletonList(artist));

        List<Artist> result = artistController.getAllArtists(null, null, "asc");

        assertEquals(1, result.size());
        assertEquals("Artist 1", result.get(0).getName());
        verify(artistRepository).findAll(any(Sort.class));
    }

    @Test
    void shouldGetArtistById() {
        Artist artist = new Artist();
        artist.setId(1L);
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        ResponseEntity<Artist> response = artistController.getArtistById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void shouldReturnNotFoundWhenArtistDoesNotExist() {
        when(artistRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Artist> response = artistController.getArtistById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldCreateArtistAndSendWebSocketNotification() {
        Artist artist = new Artist();
        artist.setName("New Artist");
        Album album = new Album();
        album.setTitle("New Album");
        artist.setAlbums(Collections.singletonList(album));
        
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        Artist result = artistController.createArtist(artist);

        assertNotNull(result);
        assertEquals("New Artist", result.getName());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/albums"), any(Album.class));
    }

    @Test
    void shouldGetAllAlbums() {
        Album album = new Album();
        Page<Album> page = new PageImpl<>(Collections.singletonList(album));
        when(albumRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Album> result = artistController.getAllAlbums(0, 10);

        assertEquals(1, result.getContent().size());
        verify(albumRepository).findAll(any(PageRequest.class));
    }

    @Test
    void shouldUploadAlbumCovers() throws IOException {
        Album album = new Album();
        album.setId(1L);
        album.setCoverUrls(new ArrayList<>());
        
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(storageService.uploadFile(any())).thenReturn("file1.jpg");
        when(albumRepository.save(any(Album.class))).thenReturn(album);
        when(storageService.generatePresignedUrl("file1.jpg")).thenReturn("http://presigned.com/file1.jpg");

        MockMultipartFile file = new MockMultipartFile("files", "test.jpg", "image/jpeg", "content".getBytes());
        MultipartFile[] files = {file};

        ResponseEntity<Album> response = artistController.uploadAlbumCovers(1L, files);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getCoverUrls().size());
        assertEquals("http://presigned.com/file1.jpg", response.getBody().getCoverUrls().get(0));
    }
}
