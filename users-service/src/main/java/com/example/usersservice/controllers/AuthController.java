package com.example.usersservice.controllers;

import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.jwt.JwtRequest;
import com.example.usersservice.jwt.JwtResponse;
import com.example.usersservice.jwt.RefreshJwtRequest;
import com.example.usersservice.services.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest authRequest) throws UserException {
        JwtResponse jwtResponse = authService.login(authRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/token")
    public ResponseEntity<JwtResponse> getNewAccessToken(@RequestBody RefreshJwtRequest refreshRequest) throws UserException {
        final JwtResponse token = authService.getAccessToken(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(token);
    }


    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> getNewRefreshToken(@RequestBody RefreshJwtRequest request) throws UserException {
        final JwtResponse token = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }
}
