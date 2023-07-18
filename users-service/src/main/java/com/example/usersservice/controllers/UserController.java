package com.example.usersservice.controllers;

import com.example.usersservice.dto.SecureCodeResponse;
import com.example.usersservice.dto.UserCreateRequest;
import com.example.usersservice.dto.UserMapper;
import com.example.usersservice.dto.UserUpdateRequest;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feigns.SecureCodeProxyService;
import com.example.usersservice.models.User;
import com.example.usersservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final SecureCodeProxyService codeProxyService;

    @Autowired
    public UserController(UserService userService, SecureCodeProxyService codeProxyService) {
        this.userService = userService;
        this.codeProxyService = codeProxyService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.name or hasAuthority('ADMIN')")
    public ResponseEntity<User> findById(@PathVariable String id) throws UserException {
        return new ResponseEntity<>(userService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody @Valid UserCreateRequest userDTO, Errors errors) throws UserException {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(errors.getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(userService.save(UserMapper.map(userDTO)), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.name")
    public ResponseEntity deleteById(@PathVariable String id, @RequestBody SecureCodeResponse secureCodeResponse) throws UserException {
        userService.deleteById(id, secureCodeResponse.getSecureCode());
       return new ResponseEntity(HttpStatus.OK) ;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("#id == authentication.name")
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody @Valid UserUpdateRequest userDTO, Errors errors) throws UserException {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(errors.getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }
        User user = UserMapper.map(userDTO);
        user.setId(id);
        return new ResponseEntity<>(userService.update(user, userDTO.getCode()), HttpStatus.OK);
    }



}
