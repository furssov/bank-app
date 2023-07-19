package com.example.usersservice.repos;

import com.example.usersservice.models.BankCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankRepository extends MongoRepository<BankCard, String> {
    Optional<BankCard> findBankCardByCardNumber(String cardNumber);
}
