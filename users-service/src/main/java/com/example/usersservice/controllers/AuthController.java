package com.example.usersservice.controllers;

import com.example.usersservice.dto.UserDTO;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.User;
import com.example.usersservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAuthUser() throws UserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        Object principal = authentication.getPrincipal();
        UserDTO user = (principal instanceof UserDTO) ? (UserDTO) principal : null;
        return Objects.nonNull(user) ? new ResponseEntity(userService.getByLogin(user.getUsername()), HttpStatus.OK) : new ResponseEntity(HttpStatus.NOT_FOUND);
    }

}
