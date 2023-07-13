package com.example.usersservice.dto;

import lombok.Data;

@Data
public class SecureCodeResponse {
    private String secureCode;
    private String receiverEmail;
}
