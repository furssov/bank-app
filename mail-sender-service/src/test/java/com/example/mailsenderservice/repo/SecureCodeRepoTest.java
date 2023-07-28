package com.example.mailsenderservice.repo;

import com.example.mailsenderservice.model.SecureCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class SecureCodeRepoTest {

    private final SecureCodeRepo secureCodeRepo;


    @Autowired
    SecureCodeRepoTest(SecureCodeRepo secureCodeRepo, RedisTemplate redisTemplate) {
        this.secureCodeRepo = secureCodeRepo;
    }

    @Test
    void saveDeleteAndFind() {
        String code = UUID.randomUUID().toString();
        String email = "fursov@gmail.com";
        secureCodeRepo.deleteById(email);
        Assertions.assertEquals(Optional.empty(), secureCodeRepo.findById(email));

        SecureCode secureCode = new SecureCode();
        secureCode.setSecureCode(code);
        secureCode.setReceiverEmail(email);

        secureCodeRepo.save(secureCode);

        Assertions.assertEquals(secureCode, secureCodeRepo.findById(email).get());

        SecureCode secureCode1 = new SecureCode();
        email = "fursova@gmail.com";
        secureCode1.setSecureCode(code);
        secureCode1.setReceiverEmail(email);
        secureCodeRepo.save(secureCode1);
        Assertions.assertEquals(secureCode1, secureCodeRepo.findById(email).get());
    }
}