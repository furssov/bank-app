package com.example.usersservice.dto;

import lombok.Data;

@Data
public class SecureCodeResponse extends SecureCodeDto{
    private String receiverEmail;
}
