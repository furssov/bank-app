package com.example.usersservice.feign;

import com.example.usersservice.dto.SecureCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "localhost:8300", name = "mail-sender-service")
public interface SecureCodeProxyService {
    @PostMapping("/security-code/send/to/{email}")
    public SecureCodeResponse sendSecureCode(@PathVariable String email);

    @GetMapping("/security-code/{email}")
    public SecureCodeResponse getSecureCode(@PathVariable String email);

    @DeleteMapping("/security-code/{email}")
    public void deleteSecureCode(@PathVariable String email);

}
