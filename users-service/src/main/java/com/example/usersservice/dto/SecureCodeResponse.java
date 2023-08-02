package com.example.usersservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class SecureCodeResponse extends SecureCodeDto{
    @Getter
    private String receiverEmail;
    public SecureCodeResponse(String code, String email) {
        super(code);
        receiverEmail = email;
    }

    public SecureCodeResponse() {
        super();
    }
}
