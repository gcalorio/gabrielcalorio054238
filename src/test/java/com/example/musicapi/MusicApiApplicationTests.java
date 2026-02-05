package com.example.musicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.example.musicapi.service.StorageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
class MusicApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/artists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateAndReturnToken() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"password\"}";
        String response = mockMvc.perform(post("/api/v1/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwttoken").exists())
                .andReturn().getResponse().getContentAsString();

        String token = extractToken(response);

        mockMvc.perform(get("/api/v1/artists")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAccessOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    void shouldAccessSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRefreshToken() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"password\"}";
        String response = mockMvc.perform(post("/api/v1/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = extractToken(response);

        mockMvc.perform(get("/api/v1/refresh")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwttoken").exists());
    }

    private String extractToken(String response) {
        // Formato esperado: {"jwttoken":"..."}
        int start = response.indexOf("\"jwttoken\":\"") + 12;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
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

    @Test
    void shouldCreateArtist() throws Exception {
        String token = getAccessToken();
        String artistJson = "{\"name\":\"New Artist\",\"type\":\"SOLO\",\"albums\":[{\"title\":\"Album 1\"}]}";

        mockMvc.perform(post("/api/v1/artists")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(artistJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Artist")))
                .andExpect(jsonPath("$.albums", hasSize(1)))
                .andExpect(jsonPath("$.albums[0].title", is("Album 1")));
    }

    @Test
    void shouldUpdateArtist() throws Exception {
        String token = getAccessToken();
        // El ID 1 debería existir por la inicialización de datos (Serj Tankian)
        String updateJson = "{\"name\":\"Updated Artist Name\"}";

        mockMvc.perform(put("/api/v1/artists/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Artist Name")));
    }

    @Test
    void shouldGetArtistById() throws Exception {
        String token = getAccessToken();

        mockMvc.perform(get("/api/v1/artists/2")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    void shouldGetAlbumsWithPagination() throws Exception {
        String token = getAccessToken();

        // Temos 13 álbuns nos dados iniciais (3+4+3+3)
        // No entanto, o teste anterior 'shouldCreateArtist' adiciona mais 1 álbum, totalizando 14
        mockMvc.perform(get("/api/v1/albums?page=0&size=5")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.totalElements").value(14));

        mockMvc.perform(get("/api/v1/albums?page=2&size=5")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4))); // Página 3 (índice 2) deve ter os últimos 4 álbuns (14 total, 5+5+4)
    }

    @Test
    void shouldFilterArtistsByType() throws Exception {
        String token = getAccessToken();

        // Filtra por SOLO (Serj Tankian, Mike Shinoda, Michel Teló + 1 do teste shouldCreateArtist)
        mockMvc.perform(get("/api/v1/artists?type=SOLO")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        // Filtra por BAND (Guns N’ Roses)
        mockMvc.perform(get("/api/v1/artists?type=BAND")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Guns N’ Roses")));
    }

    @Test
    void shouldSearchArtistsByName() throws Exception {
        String token = getAccessToken();

        // Busca por "Mike"
        mockMvc.perform(get("/api/v1/artists?name=Mike")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Mike Shinoda")));

        // Busca por "Mi" (Mike Shinoda e Michel Teló)
        mockMvc.perform(get("/api/v1/artists?name=Mi")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldSortArtistsByName() throws Exception {
        String token = getAccessToken();

        // Ordenação ASC (Guns N’ Roses, Michel Teló, Mike Shinoda, Serj Tankian...)
        // Nota: O ID 1 vira "Updated Artist Name" no teste shouldUpdateArtist
        // Ordem ASC esperada: Guns N' Roses, Michel Teló, Mike Shinoda, New Artist, Updated Artist Name
        mockMvc.perform(get("/api/v1/artists?sort=asc")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Guns N’ Roses")));

        // Ordenação DESC
        // Ordem DESC esperada: Updated Artist Name, Serj Tankian (se não alterado) ou New Artist...
        mockMvc.perform(get("/api/v1/artists?sort=desc")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void shouldUploadAlbumCovers() throws Exception {
        String token = getAccessToken();

        MockMultipartFile file1 = new MockMultipartFile("files", "cover1.jpg", "image/jpeg", "image1content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "cover2.png", "image/png", "image2content".getBytes());

        when(storageService.uploadFile(any())).thenReturn("mocked_filename.jpg");
        when(storageService.generatePresignedUrl(any())).thenReturn("http://presigned-url.com/mocked_filename.jpg");

        // Usando o ID 1 (Álbum de Serj Tankian inicializado)
        mockMvc.perform(multipart("/api/v1/albums/1/covers")
                .file(file1)
                .file(file2)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverUrls", hasSize(2)))
                .andExpect(jsonPath("$.coverUrls[0]", is("http://presigned-url.com/mocked_filename.jpg")));
    }

    @Test
    void shouldAccessHealthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    void shouldAccessLiveness() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    void shouldAccessReadiness() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
