package com.example.musicapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private final String secret = "test_secret_key_1234567890_test_secret_key_1234567890";

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", secret);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        String token = jwtTokenUtil.generateToken(userDetails);
        assertNotNull(token);

        assertTrue(jwtTokenUtil.validateToken(token, userDetails));
        assertEquals("testuser", jwtTokenUtil.getUsernameFromToken(token));
    }

    @Test
    void shouldFailValidationForDifferentUser() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user1");

        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("user2");

        String token = jwtTokenUtil.generateToken(userDetails);
        assertFalse(jwtTokenUtil.validateToken(token, differentUser));
    }
}
