package com.example.usersservice.controllers;

import com.example.usersservice.dto.UserCreateRequest;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feigns.JwtProxyService;
import com.example.usersservice.feigns.SecureCodeProxyService;
import com.example.usersservice.mappers.BankMapper;
import com.example.usersservice.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class UserControllerTest {
    @Autowired
    private UserController userController;

    private MockMvc mockMvc;

    @MockBean
    private Authentication authentication;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${mongo.name.users}")
    private String usersCollection;



    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        mongoTemplate.remove(new Query(), usersCollection);
    }

    @Test
    void saveUser() throws Exception {
        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setFirstName("Andrii");
        userCreateRequest.setSecondName("Fursov");
        userCreateRequest.setUsername("fursovd70@gmail.com");
        userCreateRequest.setPassword("12345");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(userCreateRequest.getUsername()));

        UserCreateRequest notValidUser = new UserCreateRequest();
        notValidUser.setPassword("12345");
        notValidUser.setFirstName("Andrii");
        notValidUser.setSecondName("Fursov");
        /*username has already exist*/
        notValidUser.setUsername("fursovd70@gmail.com");
/*
        String message = "User with such username has already exists";
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(notValidUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString(message)));
 */
    }
}