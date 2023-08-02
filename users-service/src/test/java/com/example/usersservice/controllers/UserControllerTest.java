package com.example.usersservice.controllers;

import com.example.usersservice.controllers.handlers.BankExceptionHandler;
import com.example.usersservice.dto.SecureCodeResponse;
import com.example.usersservice.dto.UserCreateRequest;
import com.example.usersservice.dto.UserUpdateRequest;
import com.example.usersservice.exceptions.ext.UserException;
import com.example.usersservice.feigns.SecureCodeProxyService;
import com.example.usersservice.models.Role;
import com.example.usersservice.models.User;
import com.example.usersservice.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class UserControllerTest {
    @Autowired
    private UserController userController;

    @Autowired
    @Qualifier("userServiceImpl")
    private UserService userService;

    @MockBean
    private SecureCodeProxyService secureCodeProxyService;

    private MockMvc mockMvc;

    @MockBean
    private Authentication authentication;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${mongo.name.users}")
    private String usersCollection;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(BankExceptionHandler.class).build();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        mongoTemplate.remove(new Query(), usersCollection);
    }

    private UserUpdateRequest userUpdateRequest(String username, String password, String code) {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setUsername(username);
        userUpdateRequest.setPassword(password);
        userUpdateRequest.setCode(code);
        return userUpdateRequest;
    }
    private UserCreateRequest getUserCreateRequest() {
        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setFirstName("Andrii");
        userCreateRequest.setSecondName("Fursov");
        userCreateRequest.setUsername("fursovd70@gmail.com");
        userCreateRequest.setPassword("12345");
        return userCreateRequest;
    }

    private User createUser(String email, Role role) {
        return new User.UserBuilder()
                .withUsername(email)
                .withPassword("12345")
                .withRole(role)
                .build();
    }

    @Test
    void CreateUser_ValidProperties_StatusOk() throws Exception {
        UserCreateRequest userCreateRequest = getUserCreateRequest();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(userCreateRequest.getUsername()));

    }

    @Test
    void CreateUser_NotValidPassword_StatusBadRequest() throws Exception {
        UserCreateRequest notValidUser = getUserCreateRequest();
        notValidUser.setPassword("1234");

        String message = "password min size is 5, max is 30";
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(notValidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(message));
    }

    @Test
    void CreateUser_UsernameNotValid_StatusBadRequest() throws Exception {
        UserCreateRequest notValidUser = getUserCreateRequest();
        notValidUser.setUsername("fursov");

        String message = "Username must be an email";
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(notValidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(message));
    }

    @Test
    void CreateUser_UsernameAlreadyExists_UserException() throws Exception {
        UserCreateRequest existedUser = getUserCreateRequest();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(existedUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(existedUser.getUsername()));

        UserCreateRequest sameUser = getUserCreateRequest();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(sameUser)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    void FindById_FindAuthenticatedId_User() throws Exception {
        String email = "fursovd70@gmail.com";
        User user = createUser(email, Role.USER);
        userService.save(user);

        Mockito.when(authentication.getName()).thenReturn(user.getId());

        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    void FindById_FindNotAuthenticatedId_ServletExceptionCausedByAccessDenied() throws Exception {
        String email = "fursovd70@gmail.com";
        User user = createUser(email, Role.USER);
        userService.save(user);

        Mockito.when(authentication.getName()).thenReturn(user.getId());

        String randomId = UUID.randomUUID().toString();
           mockMvc.perform(get("/users/" + randomId))
                   .andExpect(status().isUnauthorized())
                   .andExpect(result ->
                           Assertions.assertTrue(result.getResolvedException() instanceof AccessDeniedException)
                           );
    }

    @Test
    void DeleteById_AuthenticatedDeletesAccount_StatusOk() throws Exception {
        String email = "fursovd70@gmail.com";
        User user = createUser(email, Role.USER);
        userService.save(user);

        Mockito.when(authentication.getName()).thenReturn(user.getId());
        SecureCodeResponse secureCodeResponse = new SecureCodeResponse("12345", email);
        Mockito.when(secureCodeProxyService.getSecureCode(email)).thenReturn(secureCodeResponse);

        mockMvc.perform(delete("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(secureCodeResponse)))
                .andExpect(status().isOk());
    }

    @Test
    void DeleteById_AuthenticatedDeletesAccount_UserExceptionCausedWrongCode() throws Exception {
        String email = "fursovd70@gmail.com";
        User user = createUser(email, Role.USER);
        userService.save(user);

        Mockito.when(authentication.getName()).thenReturn(user.getId());
        SecureCodeResponse scr = new SecureCodeResponse("111", email);
        Mockito.when(secureCodeProxyService.getSecureCode(email)).thenReturn(scr);
        SecureCodeResponse secureCodeResponse = new SecureCodeResponse("12345", email);

        mockMvc.perform(delete("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(secureCodeResponse)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        Assertions.assertTrue(result.getResolvedException() instanceof UserException)
                );
    }

    @Test
    void DeleteById_NotAuthenticatedDeletesAccount_AccessDenied() throws Exception {
        mockMvc.perform(delete("/users/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new SecureCodeResponse())))
                .andExpect(status().isUnauthorized())
                .andExpect(result ->
                        Assertions.assertTrue(result.getResolvedException() instanceof AccessDeniedException));
    }

    @Test
    void UpdateUser_AuthenticatedUpdatesAccount_StatusOk() throws Exception {
        String email = "fursovd70@gmail.com";
        String updatedEmail = "furssov@gmail.com";
        String password = "Andrey12";
        String code = UUID.randomUUID().toString();

        User user = createUser(email, Role.USER);
        userService.save(user);

        String id = user.getId();

        UserUpdateRequest userUpdateRequest = userUpdateRequest(updatedEmail, password, code);

        Mockito.when(authentication.getName()).thenReturn(id);
        Mockito.when(authentication.getPrincipal()).thenReturn(email);
        Mockito.when(secureCodeProxyService.getSecureCode(email)).thenReturn(new SecureCodeResponse(code, email));

        mockMvc.perform(patch("/users/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updatedEmail));
    }

    @Test
    void UpdateUser_NotValidUpdatingUsername_StatusBadRequest() throws Exception {
        String message = "Username must be an email";
        String updatedEmail = "furssov";
        String password = "12345A";
        String code = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();

        UserUpdateRequest userUpdateRequest = userUpdateRequest(updatedEmail, password, code);

        Mockito.when(authentication.getName()).thenReturn(id);

        mockMvc.perform(patch("/users/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(message));
    }

    @Test
    void UpdateUser_NotValidSecureCode_StatusBadRequest() throws Exception {
        String message = "Secure code can not be blank";
        String updatedEmail = "furssov@gmail.com";
        String password = "12345A";
        String code = "";
        String id = UUID.randomUUID().toString();

        UserUpdateRequest userUpdateRequest = userUpdateRequest(updatedEmail, password, code);

        Mockito.when(authentication.getName()).thenReturn(id);

        mockMvc.perform(patch("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(message));
    }

    @Test
    void UpdateUser_NotValidPassword_StatusBadRequest() throws Exception {
        String message = "password min size is 5, max is 30";
        String updatedEmail = "furssov@gmail.com";
        String code = "12345";
        String id = UUID.randomUUID().toString();
        String wrongPassword = "And2";

        UserUpdateRequest userUpdateRequest = userUpdateRequest(updatedEmail, wrongPassword, code);

        Mockito.when(authentication.getName()).thenReturn(id);

        mockMvc.perform(patch("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(message));
    }

    @Test
    void UpdateUser_AuthenticatedDeletesNotItselfAccount_AccessDenied() throws Exception {
        String password = "Andrey12";
        String email = "fursovd70@gmail.com";
        String updatedEmail = "furssov@gmail.com";
        String code = UUID.randomUUID().toString();

        User user = createUser(email, Role.USER);
        userService.save(user);

        String id = user.getId();

        UserUpdateRequest userUpdateRequest = userUpdateRequest(updatedEmail, password,code);
        Mockito.when(authentication.getName()).thenReturn(id);

        mockMvc.perform(patch("/users/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userUpdateRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(result ->
                        Assertions.assertTrue(result.getResolvedException() instanceof AccessDeniedException)
                );

    }

}