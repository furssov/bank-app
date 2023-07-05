package com.example.usersservice.controllers;

import com.example.usersservice.dto.UserDTO;
import com.example.usersservice.dto.UserMapper;
import com.example.usersservice.exceptions.UserException;
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

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable String id) throws UserException {
        return new ResponseEntity<>(userService.findById(id).get(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody @Valid UserDTO user, Errors errors) throws UserException {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(errors.getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(userService.save(UserMapper.map(user)), HttpStatus.CREATED);
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

    //TODO patch mapping
}
