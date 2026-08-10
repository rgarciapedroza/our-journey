package com.ourjourney.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void shouldReturnUnauthorizedWhenAccessingCurrentUserWithoutToken() throws Exception {
        mockmvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
