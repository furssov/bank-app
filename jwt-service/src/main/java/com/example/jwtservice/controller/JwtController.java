package com.example.jwtservice.controller;

import com.example.jwtservice.models.AuthRequest;
import com.example.jwtservice.models.JwtToken;
import com.example.jwtservice.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jwt")
public class JwtController {

    private final JwtService jwtService;

    @Autowired
    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateJwt(@RequestBody AuthRequest authRequest) {
        return new ResponseEntity<>(jwtService.generateJwt(authRequest.getUsername(), authRequest.getRole()), HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity validateJwt(@RequestBody JwtToken jwtToken) {
        return ResponseEntity.ok(jwtService.validateToken(jwtToken.getToken()));
    }

    @GetMapping(value = "/claims/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Claims getClaims(@PathVariable String token) {
        return jwtService.getClaims(token);
    }

}
