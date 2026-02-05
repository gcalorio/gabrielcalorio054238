package com.example.musicapi.controller;

import com.example.musicapi.dto.JwtRequest;
import com.example.musicapi.dto.JwtResponse;
import com.example.musicapi.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void shouldAuthenticateAndReturnToken() throws Exception {
        JwtRequest request = new JwtRequest("admin", "password");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("mocked-token");

        ResponseEntity<?> response = authenticationController.createAuthenticationToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof JwtResponse);
        assertEquals("mocked-token", ((JwtResponse) response.getBody()).getJwttoken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldRefreshToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer old-token");
        when(jwtTokenUtil.getUsernameFromToken("old-token")).thenReturn("admin");
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken("old-token", userDetails)).thenReturn(true);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("new-token");

        ResponseEntity<?> response = authenticationController.refreshAuthenticationToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-token", ((JwtResponse) response.getBody()).getJwttoken());
    }

    @Test
    void shouldReturnBadRequestWhenTokenInvalidOnRefresh() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtTokenUtil.getUsernameFromToken("invalid-token")).thenReturn("admin");
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken("invalid-token", userDetails)).thenReturn(false);

        ResponseEntity<?> response = authenticationController.refreshAuthenticationToken(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
