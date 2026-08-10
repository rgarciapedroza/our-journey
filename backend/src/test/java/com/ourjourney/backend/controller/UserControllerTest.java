package com.ourjourney.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ourjourney.backend.dto.UserResponse;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-jwt-authentication"
})
class UserControllerTest {
    
    @Autowired
    private MockMvc mockmvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturnUnauthorizedWhenAccessingCurrentUserWithoutToken() throws Exception {
        mockmvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test void shouldReturnUnauthorizedWhenAccessingCurrentUserWithInvalidToken() throws Exception { 
        mockmvc.perform( 
            get("/api/users/me") 
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized()); 
    }

    @Test void shouldReturnOkWhenAccessingCurrentUserWithValidToken() throws Exception { 
        String email = "test@example.com"; 
        String token = jwtService.generateToken(email); 
        UserResponse userResponse = new UserResponse(); 
        
        when(userService.getCurrentUser(email)) 
        .thenReturn(userResponse); 
        
        mockmvc.perform( 
            get("/api/users/me") 
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()); 
                
        verify(userService).getCurrentUser(eq(email)); 
    }
}
