package com.example.usersservice.feigns;

import com.example.jwtservice.models.AuthRequest;
import com.example.jwtservice.models.JwtToken;
import com.example.usersservice.jwt.JwtResponse;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(url = "localhost:8400", name = "jwt-service")
public interface JwtProxyService {
    @PostMapping("/jwt/generate")
    String generateJwtToken(@RequestBody AuthRequest authRequest);

    @GetMapping("/jwt/validate")
    ResponseEntity validateJwtToken(@RequestBody JwtToken jwtToken);

    @GetMapping (value = "/jwt/claims/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    Claims getClaims(@PathVariable String token);
}
