package com.example.mailsenderservice.service.impl;

import com.example.mailsenderservice.exc.SecureCodeException;
import com.example.mailsenderservice.model.SecureCode;
import com.example.mailsenderservice.repo.SecureCodeRepo;
import com.example.mailsenderservice.service.SecureCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class
SecureCodeServiceImpl implements SecureCodeService {
    private final SecureCodeRepo repo;

    @Autowired
    public SecureCodeServiceImpl(SecureCodeRepo repo) {
        this.repo = repo;
    }

    @Override
    public SecureCode save(SecureCode code) {
        return repo.save(code);
    }

    @Override
    public SecureCode findByReceiverEmail(String email) throws SecureCodeException {
        Optional<SecureCode> code = repo.findById(email);
        if (code.isPresent()) {
            return code.get();
        }
        else {
            throw new SecureCodeException("No such email in secure code holder!");
        }
    }

    @Override
    public void deleteSecureCodeByEmail(String email) {
        repo.deleteById(email);
    }
}
