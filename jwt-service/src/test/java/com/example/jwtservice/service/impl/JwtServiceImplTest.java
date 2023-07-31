package com.example.jwtservice.service.impl;

import com.example.jwtservice.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class JwtServiceImplTest {

    private final JwtService jwtService;

    @Autowired
    JwtServiceImplTest(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Test
    void getClaims() {
        String username = "fursov";
        String role = "USER";
        String id = UUID.randomUUID().toString();

        String jwt = jwtService.generateJwt(username, role, id);

        Claims claims = jwtService.getClaims(jwt);
        Assertions.assertEquals(username, claims.getSubject());
        Assertions.assertEquals(role, claims.get("role"));
    }

    @Test
    void validateJwt() {
        String username = "fursov";
        String role = "USER";
        String id = UUID.randomUUID().toString();

        String jwt = jwtService.generateJwt(username, role, id);
        Assertions.assertTrue(jwtService.validateToken(jwt));
    }
}