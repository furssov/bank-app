package com.example.usersservice.repos;

import com.example.usersservice.gen.BankCardGenerator;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BankRepositoryTest {

    private final BankRepository bankRepository;
    private final MongoTemplate mongoTemplate;
    private final BankCardGenerator bankCardGenerator;
    @Value("${mongo.name.cards}")
    private String cardsCol;


    @Autowired
    BankRepositoryTest(BankRepository bankRepository, MongoTemplate mongoTemplate, BankCardGenerator bankCardGenerator) {
        this.bankRepository = bankRepository;
        this.mongoTemplate = mongoTemplate;
        this.bankCardGenerator = bankCardGenerator;
    }

    @BeforeEach
    void setUp() {
        mongoTemplate.remove(new Query(), cardsCol);
    }

    @Test
    void createTheSame() {
        String dup = bankCardGenerator.generateBankCardNumber();
        dup.intern();
        BankCard bankCard = new BankCard();
        bankCard.setCardNumber(dup);

        bankRepository.save(bankCard);

        BankCard bankCard1 = new BankCard();
        bankCard1.setCardNumber(dup);
       Assertions.assertThrows(DuplicateKeyException.class, () ->
               bankRepository.save(bankCard1)
       );
    }

    @Test
    void create() {
        int quantity = 10;
        populate(quantity);
        Assertions.assertEquals(quantity, bankRepository.findAll().size());
    }


    @Test
    void read() {
        int quantity = 10;
        populate(quantity);
        List<BankCard> bankCards = bankRepository.findAll();

        Assertions.assertEquals(quantity, bankCards.size());

        Assertions.assertEquals(bankCards.get(5), bankRepository.findById(bankCards.get(5).getId()).get());
        Assertions.assertEquals(bankCards.get(1), bankRepository.findById(bankCards.get(1).getId()).get());
        Assertions.assertEquals(bankCards.get(7), bankRepository.findById(bankCards.get(7).getId()).get());
    }

    @Test
    void update() {
        int quantity = 10;
        populate(quantity);
        Assertions.assertEquals(quantity, bankRepository.findAll().size());
        BankCard bankCard = bankRepository.findAll().get(3);
        bankCard.setCardNumber(bankCardGenerator.generateBankCardNumber());
        Assertions.assertEquals(Optional.empty(), bankRepository.findBankCardByCardNumber(bankCard.getCardNumber()));
        bankRepository.save(bankCard);
        Assertions.assertEquals(bankCard, bankRepository.findBankCardByCardNumber(bankCard.getCardNumber()).get());
        Assertions.assertEquals(quantity, bankRepository.findAll().size());
    }

    @Test
    void delete() {
        int quantity = 10;
        populate(quantity);
        List<BankCard> bankCards = bankRepository.findAll();

        bankRepository.deleteById(bankCards.get(0).getId());
        Assertions.assertEquals(--quantity, bankRepository.findAll().size());

        bankRepository.deleteById(bankCards.get(5).getId());
        Assertions.assertEquals(--quantity, bankRepository.findAll().size());

        bankRepository.deleteById(bankCards.get(9).getId());
        Assertions.assertEquals(--quantity, bankRepository.findAll().size());

    }

    @Test
    void findCardByCardNumber() {
        int quantity = 10;
        populate(quantity);
        List<BankCard> bankCards = bankRepository.findAll();

        Assertions.assertEquals(bankCards.get(5), bankRepository.findBankCardByCardNumber(bankCards.get(5).getCardNumber()).get());
        Assertions.assertNotEquals(bankCards.get(5), bankRepository.findBankCardByCardNumber(bankCards.get(6).getCardNumber()).get());

        Assertions.assertEquals(Optional.empty(), bankRepository.findBankCardByCardNumber(String.valueOf(quantity)));

        Assertions.assertEquals(bankCards.get(2), bankRepository.findBankCardByCardNumber(bankCards.get(2).getCardNumber()).get());
        Assertions.assertEquals(bankCards.get(8), bankRepository.findBankCardByCardNumber(bankCards.get(8).getCardNumber()).get());
    }

    private void populate(int quantity) {
        for (int i = 0; i < quantity; i++) {
            bankRepository.save(BankCard.builder()
                    .cardNumber(bankCardGenerator.generateBankCardNumber())
                    .amount(BigDecimal.valueOf(i))
                    .pinCode(bankCardGenerator.generateBankCardPinCode())
                    .cvv(bankCardGenerator.generateBankCardCvv())
                    .cardCurrency(CardCurrency.UAH)
                    .build());
        }
    }


}