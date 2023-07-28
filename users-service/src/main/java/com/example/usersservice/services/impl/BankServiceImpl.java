package com.example.usersservice.services.impl;

import com.example.usersservice.exceptions.BankCardException;
import com.example.usersservice.exceptions.CardReleaseException;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feigns.SecureCodeProxyService;
import com.example.usersservice.feigns.TransferMoneyProxyService;
import com.example.usersservice.gen.BankCardGenerator;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.models.User;
import com.example.usersservice.repos.BankRepository;
import com.example.usersservice.repos.UserRepository;
import com.example.usersservice.services.BankService;
import com.example.usersservice.validators.BankCardValidator;
import com.springboot.conversion.beans.CurrencyConversionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service("bankServiceImpl")
public class BankServiceImpl implements BankService {

    @Autowired
    private TransferMoneyProxyService transferMoneyServiceProxy;
    @Autowired
    private UserRepository repository;
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private BankCardGenerator bankCardGenerator;
    @Autowired
    private BankCardValidator bankCardValidator;

    @Override
    @Transactional
    public TransferMoneyResult transferMoney(String senderCardNumber, BigDecimal amount, String receiverCardNumber) throws Throwable {
            BankCard senderCard = checkIfCardBelongsToAuthorized(senderCardNumber);
            BankCard receiverCard = findBankCardByCardNumber(receiverCardNumber);
            CurrencyConversionBean ccb = getCurrencyConversion(senderCard, receiverCard, amount);
            return transfer(senderCard, receiverCard, ccb);
    }

    @Override
    @Transactional
    public boolean cardRelease(BankCard bankCard) throws UserException, CardReleaseException {
        User user = getAuthorized();
        generateAndValidateCardAttributes(bankCard);
        //TODO
        // Necessary to think about if some users will have the same bank card number
        mapUserAndBankCard(user, bankCard);
        return true;

    }

    private BankCard generateAndValidateCardAttributes(BankCard bc) throws CardReleaseException, UserException {
        String cn = bankCardGenerator.generateBankCardNumber();
        String cvv = bankCardGenerator.generateBankCardCvv();
        BankCard valid = BankCard.builder()
                        .cardNumber(cn)
                        .cardCurrency(bc.getCardCurrency())
                        .cvv(cvv)
                        .build();

        if (bankCardValidator.validateUserBankCard(getAuthorized(), valid)) {
            bc.setCardNumber(cn);
            bc.setCvv(cvv);
            return bc;
        }
        else {
            throw new CardReleaseException("Sorry, you already have such card!");
        }
    }

    private void mapUserAndBankCard(User user, BankCard bankCard) {
            bankCard.setUser(user);
            bankRepository.save(bankCard);
            user.getBankCards().add(bankCard);
            repository.save(user);
    }



    @Override
    public BankCard findBankCardByCardNumber(String cardNumber) throws Throwable {
        return bankRepository.findBankCardByCardNumber(cardNumber).orElseThrow(() ->
                new BankCardException("There is no any bank card by inputted card number")
        );
    }

    private User getAuthorized() throws UserException {
        return repository.findById(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UserException("Don't authorized", HttpStatus.UNAUTHORIZED));
    }

    private BankCard checkIfCardBelongsToAuthorized(String card) throws Throwable {
        User authorized = getAuthorized();
        return authorized.getBankCards()
                .stream()
                .parallel()
                .filter(bankCard -> bankCard.getCardNumber().equals(card))
                .findAny().orElseThrow(() -> new BankCardException("You don't have this card"));
    }

    private TransferMoneyResult transfer(BankCard bankCardSender, BankCard bankCardReceiver, CurrencyConversionBean currencyConversionBean) throws TransferMoneyException {
        BigDecimal senderAmount = bankCardSender.getAmount();
        BigDecimal toSendAmount = currencyConversionBean.getQuantity();
        BigDecimal toSendConvertedAmount = currencyConversionBean.getTotalAmount();
        BigDecimal receiverAmount = bankCardReceiver.getAmount();
        if (bankCardValidator.validateAmount(senderAmount, toSendAmount)) {
            bankCardSender.setAmount(senderAmount.subtract(toSendAmount));
            bankCardReceiver.setAmount(receiverAmount.add(toSendConvertedAmount));
            bankRepository.save(bankCardSender);
            bankRepository.save(bankCardReceiver);
            return getTransferMoneyResult(bankCardSender, bankCardReceiver, currencyConversionBean);
        }
        else {
            throw new TransferMoneyException("Not enough money on your card");
        }

    }

    private CurrencyConversionBean getCurrencyConversion(BankCard cardSender, BankCard cardReceiver, BigDecimal amount) {
        return transferMoneyServiceProxy.getResultOfConversion(cardSender.getCardCurrency().name(), cardReceiver.getCardCurrency().name(), amount);
    }

    private TransferMoneyResult getTransferMoneyResult(BankCard senderCard, BankCard receiverCard, CurrencyConversionBean ccb) {
        return TransferMoneyResult.builder()
                .bankCardFrom(senderCard.getCardNumber())
                .fromFirstName(senderCard.getUser().getFirstName())
                .fromSecondName(senderCard.getUser().getSecondName())
                .bankCardTo(receiverCard.getCardNumber())
                .toFirstName(receiverCard.getUser().getFirstName())
                .toSecondName(receiverCard.getUser().getSecondName())
                .amount(ccb.getTotalAmount())
                .currency(ccb.getTo())
                .build();
    }
}
