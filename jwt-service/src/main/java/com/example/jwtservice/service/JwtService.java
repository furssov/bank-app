package com.example.jwtservice.service;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String generateJwt(String username, String role);
    boolean validateToken(String token);

    Claims getClaims(String token);
}
