package com.example.mailsenderservice.service;

import com.example.mailsenderservice.exc.SecureCodeException;
import com.example.mailsenderservice.model.SecureCode;

import java.util.List;
import java.util.Optional;

public interface SecureCodeService {
    List<SecureCode> getSecureCodes();

    SecureCode saveSecureCode(SecureCode secureCode);

    SecureCode updateSecureCode(SecureCode secureCode);

    SecureCode findSecureCodeByReceiverEmail(String email) throws SecureCodeException;
}
