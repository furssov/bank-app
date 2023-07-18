package com.example.jwtservice.service;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String generateJwt(String username, String role, String id);
    boolean validateToken(String token);

    Claims getClaims(String token);
}
