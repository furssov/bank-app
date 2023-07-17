package com.example.usersservice.feigns;

import com.example.usersservice.dto.SecureCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "localhost:8300", name = "mail-sender-service")
public interface SecureCodeProxyService {
    @PostMapping("/security-code/send/to/{email}")
    SecureCodeResponse sendSecureCode(@PathVariable String email);

    @GetMapping("/security-code/{email}")
    SecureCodeResponse getSecureCode(@PathVariable String email);

    @DeleteMapping("/security-code/{email}")
    void deleteSecureCode(@PathVariable String email);

}
