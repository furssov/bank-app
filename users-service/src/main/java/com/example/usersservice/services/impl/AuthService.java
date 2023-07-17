package com.example.usersservice.services.impl;

import com.example.jwtservice.models.AuthRequest;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feigns.JwtProxyService;
import com.example.usersservice.jwt.JwtAuthentication;
import com.example.usersservice.jwt.JwtRequest;
import com.example.usersservice.jwt.JwtResponse;
import com.example.usersservice.models.User;
import com.example.usersservice.services.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserService userService;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProxyService jwtProxyService;

    @Autowired
    public AuthService(UserService userService, JwtProxyService jwtProxyService) {
        this.userService = userService;
        this.jwtProxyService = jwtProxyService;
    }

    public JwtResponse login(@NonNull JwtRequest authRequest) throws UserException {
        final User user = userService.getByLogin(authRequest.getUsername());
        if (user.getPassword().equals(authRequest.getPassword())) {
            final String accessToken = jwtProxyService.generateJwtToken(new AuthRequest(user.getUsername(), user.getRole().name()));
            refreshStorage.put(user.getUsername(), accessToken);
            return new JwtResponse(accessToken);
        }
        else {
            throw new UserException("Password isn't correct", HttpStatus.BAD_REQUEST);
        }
    }


    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }

}
