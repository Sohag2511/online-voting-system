package com.voting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voting.dto.LoginRequest;
import com.voting.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VotingApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void contextLoads() { }

    @Test
    void register_and_login_flow() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("testvoter");
        reg.setEmail("testvoter@example.com");
        reg.setPassword("Test@1234");
        reg.setFullName("Test Voter");

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("VOTER"));

        LoginRequest login = new LoginRequest();
        login.setUsername("testvoter");
        login.setPassword("Test@1234");

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_with_wrong_password_returns_401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("admin");
        login.setPassword("wrongpassword");

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_endpoint_requires_auth() throws Exception {
        mvc.perform(post("/api/admin/elections"))
                .andExpect(status().isForbidden());
    }
}
