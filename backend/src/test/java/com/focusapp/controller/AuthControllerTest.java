package com.focusapp.controller;

import com.focusapp.dto.LoginRequest;
import com.focusapp.dto.RegisterRequest;
import com.focusapp.entity.User;
import com.focusapp.repository.UserRepository;
import com.focusapp.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private PasswordEncoder passwordEncoder;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    @MockBean
    private AuthenticationManager authenticationManager;
    
    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(jwtUtil.generateToken("newuser")).thenReturn("test-jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }
    
    @Test
    void testRegister_UsernameExists() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"existinguser\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }
}
