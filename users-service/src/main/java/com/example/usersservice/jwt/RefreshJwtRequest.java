package com.example.usersservice.jwt;

import lombok.Data;

@Data
public class RefreshJwtRequest {
    private String refreshToken;
}
