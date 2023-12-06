package com.zeliafinance.identitymanagement.debitmandate.service;

import com.zeliafinance.identitymanagement.debitmandate.dto.CardRequest;
import com.zeliafinance.identitymanagement.debitmandate.repository.CardRepository;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.entity.Transactions;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.impl.TransactionService;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.*;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateChargeResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.CreateFundResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.service.PayStackService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static com.zeliafinance.identitymanagement.utils.AccountUtils.*;

@Service
@AllArgsConstructor
@Slf4j
public class DebitMandateService {
    private final PayStackService payStackService;
    private final TransactionService transactionService;
    private final UserCredentialRepository userCredentialRepository;
    private final CardRepository cardRepository;


    public ResponseEntity<CustomResponse> addCard(CardRequest cardRequest){
        //Send card request to paystack, debit account and refund.
        //update transaction table for debit and refund
        //Save the card details if step 1 is successful
        //return adequate response
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        log.info("user firstname: {}", userCredential.getFirstName());
        String userName = userCredential.getFirstName() + " " + userCredential.getLastName() + " " + userCredential.getOtherName();

        CreateChargeResponse payStackResponse = payStackService.createCard(CreateChargeRequest.builder()
                        .amount("5000")
                        .email(SecurityContextHolder.getContext().getAuthentication().getName())
                        .metaData(MetaData.builder()
                                .custom_fields(List.of(CustomFields.builder()
                                        .display_name("TEST")
                                        .value("5000")
                                        .variable_name("Test Card")
                                        .build()))
                                .build())
                        .card(Card.builder()
                                .cvv(cardRequest.getCvv())
                                .expiry_month(cardRequest.getExpiryMonth())
                                .expiry_year(cardRequest.getExpiryYear())
                                .number(cardRequest.getCardNumber())
                                .build())
                .build());

        transactionService.saveTransaction(Transactions.builder()
                        .externalRefNumber(payStackResponse.getData().getReference())
                        .transactionRef(generateTxnRef("MANDATE_CHARGE"))
                        .walletId(userCredential.getWalletId())
                        .transactionAmount(50)
                        .createdAt(LocalDateTime.now())
                        .transactionStatus("COMPLETED")
                .build());

        //Check is card expired
        int currentCentury = Year.now().getValue()/100;
        int year = Integer.parseInt(currentCentury + cardRequest.getExpiryYear());
        int month = Integer.parseInt(cardRequest.getExpiryMonth());

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate expiryDate = LocalDate.of(year, month, yearMonth.atEndOfMonth().getDayOfMonth());

        if (expiryDate.isBefore(LocalDate.now())){
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(CARD_EXPIRED)
                            .responseBody(expiryDate)
                    .build());
        }

        if (!payStackResponse.isStatus()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(INSUFFICIENT_BALANCE)
                    .build());
        }

        CreateFundResponse refundResponse = payStackService.refundAccount(CreateRefundRequest.builder()
                        .transaction(payStackResponse.getData().getReference())
                .build());

        if (refundResponse.isStatus()){
            transactionService.saveTransaction(Transactions.builder()
                            .transactionStatus("COMPLETED")
                            .transactionAmount(50)
                            .transactionType("CARD_REFUND")
                            .transactionRef(generateTxnRef("REFUND"))
                            .createdAt(LocalDateTime.now())
                            .walletId(userCredential.getWalletId())
                            .externalRefNumber(payStackResponse.getData().getReference())
                    .build());
        }

        com.zeliafinance.identitymanagement.debitmandate.entity.Card customerCard = cardRepository.save(com.zeliafinance.identitymanagement.debitmandate.entity.Card.builder()
                        .walletId(userCredential.getWalletId())
                        .bin(cardRequest.getCardNumber().substring(0, 6))
                        .lastFour(cardRequest.getCardNumber().substring(cardRequest.getCardNumber().length()-5))
                        .authCode(payStackResponse.getData().getAuthorization().getAuthorization_code())
                        .cardExpiry(expiryDate)
                        .cvv(cardRequest.getCvv())
                        .cardType(payStackResponse.getData().getAuthorization().getCard_type())
                .build());


        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(customerCard)
                .build());
    }
}
