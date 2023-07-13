package com.example.usersservice.controllers;

import com.example.usersservice.dto.UpdateUserRequest;
import com.example.usersservice.dto.UserDTO;
import com.example.usersservice.dto.UserMapper;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feign.SecureCodeProxyService;
import com.example.usersservice.models.User;
import com.example.usersservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable String id) throws UserException {
        return new ResponseEntity<>(userService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody @Valid UserDTO userDTO, Errors errors) throws UserException {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(errors.getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(userService.save(UserMapper.mapForCreating(userDTO)), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteById(@PathVariable String id) {
        if (userService.deleteById(id)) {
            return new ResponseEntity(HttpStatus.OK);
        }
        else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody @Valid UpdateUserRequest userDTO, Errors errors) throws UserException {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(errors.getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }
        User user = UserMapper.mapForUpdating(userDTO);
        user.setId(id);
        return new ResponseEntity<>(userService.update(user, userDTO.getCode()), HttpStatus.OK);
    }



}
