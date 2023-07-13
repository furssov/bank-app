package com.example.mailsenderservice.service;

import com.example.mailsenderservice.exc.SecureCodeException;
import com.example.mailsenderservice.model.SecureCode;

public interface SecureCodeService {
    SecureCode save(SecureCode code);
    SecureCode findByReceiverEmail(String email) throws SecureCodeException;

    void deleteSecureCodeByEmail(String email);
}
