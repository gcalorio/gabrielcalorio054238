package com.example.musicapi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private AmazonS3 s3Client;

    @InjectMocks
    private StorageService storageService;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "bucketName", bucketName);
    }

    @Test
    void shouldUploadFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        
        String fileName = storageService.uploadFile(file);
        
        assertNotNull(fileName);
        assertTrue(fileName.contains("test.jpg"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    void shouldGeneratePresignedUrl() throws Exception {
        String fileName = "test.jpg";
        URL mockUrl = new URL("http://localhost/test.jpg");
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);

        String url = storageService.generatePresignedUrl(fileName);

        assertEquals(mockUrl.toString(), url);
        verify(s3Client, times(1)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    void shouldReturnNullWhenFileNameIsNull() {
        assertNull(storageService.generatePresignedUrl(null));
        assertNull(storageService.generatePresignedUrl(""));
    }

    @Test
    void shouldCreateBucketIfNotExistsOnInit() {
        when(s3Client.doesBucketExistV2(bucketName)).thenReturn(false);

        storageService.init();

        verify(s3Client, times(1)).createBucket(bucketName);
    }

    @Test
    void shouldNotCreateBucketIfExistsOnInit() {
        when(s3Client.doesBucketExistV2(bucketName)).thenReturn(true);

        storageService.init();

        verify(s3Client, never()).createBucket(anyString());
    }
}
