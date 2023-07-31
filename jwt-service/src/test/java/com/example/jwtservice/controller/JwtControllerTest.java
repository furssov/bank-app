package com.example.jwtservice.controller;

import com.example.jwtservice.models.AuthRequest;
import com.example.jwtservice.models.JwtToken;
import com.example.jwtservice.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import netscape.javascript.JSObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(JwtController.class)
@ExtendWith(SpringExtension.class)
class JwtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @Test
    void generateAndValidate() throws Exception {
        String username = "fursov";
        String role = "USER";
        String id = UUID.randomUUID().toString();
        String jwt = UUID.randomUUID().toString();

        Mockito.when(jwtService.generateJwt(username, role, id)).thenReturn(jwt);
        Mockito.when(jwtService.validateToken(jwt)).thenReturn(true);

        AuthRequest authRequest = new AuthRequest(username, role, id);
        mockMvc.perform(post("/jwt/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(jwt)));

        mockMvc.perform(get("/jwt/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new JwtToken(jwt))))
                .andExpect(status().isOk());

        Mockito.when(jwtService.validateToken(jwt)).thenThrow(UnsupportedJwtException.class);

        mockMvc.perform(get("/jwt/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new JwtToken(jwt))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getClaims() throws Exception {
        String username = "fursov";
        String role = "USER";
        String id = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();

        Claims claims = new DefaultClaims();
        claims.setSubject(username);
        claims.put("role", role);
        claims.put("id", id);
        Mockito.when(jwtService.getClaims(token)).thenReturn(claims);

        mockMvc.perform(get("/jwt/claims/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(role))
                .andExpect(jsonPath("$.id").value(id));
    }
}