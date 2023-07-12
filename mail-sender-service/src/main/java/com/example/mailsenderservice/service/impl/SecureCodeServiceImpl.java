package com.example.mailsenderservice.service.impl;

import com.example.mailsenderservice.exc.SecureCodeException;
import com.example.mailsenderservice.model.SecureCode;
import com.example.mailsenderservice.repo.SecureCodeRepo;
import com.example.mailsenderservice.service.SecureCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SecureCodeServiceImpl implements SecureCodeService {
    private final SecureCodeRepo secureCodeRepo;

    @Autowired
    public SecureCodeServiceImpl(SecureCodeRepo secureCodeRepo) {
        this.secureCodeRepo = secureCodeRepo;
    }



    @Override
    public List<SecureCode> getSecureCodes() {
        List<SecureCode> list = new ArrayList<>();
        secureCodeRepo.findAll().forEach(secureCode -> list.add(secureCode));
        return list;
    }

    @Override
    public SecureCode saveSecureCode(SecureCode secureCode) {
        return secureCodeRepo.save(secureCode);
    }

    @Override
    public SecureCode updateSecureCode(SecureCode secureCode) {
        return secureCodeRepo.save(secureCode);
    }

    @Override
    public SecureCode findSecureCodeByReceiverEmail(String email) throws SecureCodeException {
        Optional<SecureCode> code = secureCodeRepo.findById(email);
        if (code.isPresent()) {
            return code.get();
        }
        else {
            throw new SecureCodeException("There is not secure code by this email");
        }
    }
}
