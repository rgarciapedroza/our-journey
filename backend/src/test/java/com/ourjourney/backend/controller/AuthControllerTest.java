package com.ourjourney.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.LoginResponse;
import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserResponse;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-jwt-authentication-tests-123456789"
})
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private RegisterRequest createValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Rosmary");
        request.setEmail("rosmary@gmail.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        return request;
    }

    private LoginRequest createValidLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("rosmary@gmail.com");
        request.setPassword("password123");
        return request;
    }


    //Resgister Tests

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        
        RegisterRequest request = createValidRegisterRequest();

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName(request.getName());
        response.setEmail(request.getEmail());
        response.setProfilePicture(null);

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

         mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Rosmary"))
        .andExpect(jsonPath("$.email").value("rosmary@gmail.com"))
        .andExpect(jsonPath("$.profilePicture").isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setName("");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setPassword("");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenConfirmPasswordIsBlank() throws Exception {

        RegisterRequest request = createValidRegisterRequest();
        request.setConfirmPassword("");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalidDuringRegister() throws Exception {

        RegisterRequest request = createValidRegisterRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }

    //Login Tests

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = createValidLoginRequest();

        LoginResponse response = new LoginResponse();
        response.setId(1L);
        response.setName("Rosmary");
        response.setEmail(request.getEmail());
        response.setProfilePicture(null);

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

         mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Rosmary"))
        .andExpect(jsonPath("$.email").value("rosmary@gmail.com"));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalidDuringLogin() throws Exception {
        LoginRequest request = createValidLoginRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLoginPasswordIsBlank() throws Exception {

        LoginRequest request = createValidLoginRequest();
        request.setPassword("");

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }
}
