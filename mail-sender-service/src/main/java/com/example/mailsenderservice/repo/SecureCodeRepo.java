package com.example.mailsenderservice.repo;

import com.example.mailsenderservice.model.SecureCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecureCodeRepo extends CrudRepository<SecureCode, String> {
}
