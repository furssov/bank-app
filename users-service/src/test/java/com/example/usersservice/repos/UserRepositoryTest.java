package com.example.usersservice.repos;

import com.example.usersservice.gen.BankCardGeneratorImpl;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Optional;

//TODO refactoring/comments
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest
class UserRepositoryTest {
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final BankRepository bankRepository;

    @Value("${mongo.name.users}")
    private String usersCol;

    @Value("${mongo.name.cards}")
    private String cardsCol;

    @Autowired
    UserRepositoryTest(UserRepository userRepository, MongoTemplate mongoTemplate, BankRepository bankRepository) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
        this.bankRepository = bankRepository;
    }

    @Test
    void create() {
        int quantity = 1000;
        populate(quantity);
        List<User> users = userRepository.findAll();
        Assertions.assertEquals(quantity, userRepository.findAll().size());
        Assertions.assertEquals("Name54", users.get(54).getFirstName());
        Assertions.assertEquals("Name555", users.get(555).getFirstName());
        Assertions.assertEquals("Name999", users.get(999).getFirstName());
        Assertions.assertEquals("Name0", users.get(0).getFirstName());

    }

    @Test
    void update() {
        int quantity = 10;
        populate(quantity);

        List<User> users = userRepository.findAll();
        Assertions.assertEquals(quantity, users.size());

        User userFromDb = users.get(3);
        Assertions.assertEquals(userFromDb.getFirstName(), "Name3");

        userFromDb.setBankCards(List.of(bankRepository.save(BankCard.builder()
                .user(userFromDb)
                .cardNumber(new BankCardGeneratorImpl().generateBankCardNumber())
                .build())));
        userFromDb.setFirstName("Name3.1");

        userRepository.save(userFromDb);

        users = userRepository.findAll();
        Assertions.assertEquals(quantity, users.size() );
        Assertions.assertEquals(userFromDb.getFirstName(), users.get(3).getFirstName());
        Assertions.assertEquals(userFromDb.getBankCards().get(0).getCardNumber(), users.get(3).getBankCards().get(0).getCardNumber());
    }

    @Test
    void delete() {
        int quantity = 10;
        populate(10);
        Assertions.assertEquals(quantity, userRepository.findAll().size());
        userRepository.deleteById(String.valueOf(5));
        Assertions.assertEquals(--quantity, userRepository.findAll().size());
        userRepository.deleteById(String.valueOf(5));
        Assertions.assertEquals(quantity, userRepository.findAll().size());
        userRepository.deleteById(String.valueOf(3));
        Assertions.assertEquals(--quantity, userRepository.findAll().size());
        userRepository.deleteById(String.valueOf(8));
        Assertions.assertEquals(--quantity, userRepository.findAll().size());
    }

    @Test
    void read() {
        int quantity = 10;
        populate(quantity);
        Assertions.assertEquals(quantity, userRepository.findAll().size());
        Assertions.assertEquals(String.valueOf(5), userRepository.findById(String.valueOf(5)).get().getId());
        Assertions.assertEquals(String.valueOf(9), userRepository.findById(String.valueOf(9)).get().getId());
        Assertions.assertEquals(String.valueOf(1), userRepository.findById(String.valueOf(1)).get().getId());
    }

    @Test
    void findByUsername() {
        int quantity = 10;
        populate(quantity);
        String username1 = "Username3";
        String username2 = "Username6";
        String username3 = "Username9";
        Assertions.assertEquals(Optional.empty(), userRepository.findUserByUsername("Bla bla bla"));
        Assertions.assertEquals(username1, userRepository.findUserByUsername(username1).get().getUsername());
        Assertions.assertEquals(username2, userRepository.findUserByUsername(username2).get().getUsername());
        Assertions.assertEquals(username3, userRepository.findUserByUsername(username3).get().getUsername());
    }
    private void populate(int quantity) {
        if (quantity > 0) {
            for (int i = 0; i < quantity; i++) {
                userRepository.save(new User.UserBuilder()
                        .withId(String.valueOf(i))
                        .withFirstName("Name" + i)
                        .withUsername("Username" + i)
                        .withBankCards(List.of(bankRepository.save(BankCard.builder()
                                .cardNumber(new BankCardGeneratorImpl().generateBankCardNumber())
                                .build())))
                        .build());
            }
        }
    }

    @AfterEach
    void aVoid() {
        mongoTemplate.dropCollection(usersCol);
        mongoTemplate.dropCollection(cardsCol);
    }
}