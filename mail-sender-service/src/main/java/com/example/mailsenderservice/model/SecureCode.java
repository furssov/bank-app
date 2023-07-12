package com.example.mailsenderservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Data
@RedisHash("code")
public class SecureCode implements Serializable {
    @Id
    @Indexed
    private String receiverEmail;

    private String secureCode;
    @TimeToLive
    private Long expiration;

    {
        expiration = 600L;
    }

}
