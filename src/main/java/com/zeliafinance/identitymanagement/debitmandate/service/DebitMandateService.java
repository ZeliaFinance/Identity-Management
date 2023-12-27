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
                                .expiry_month(cardRequest.getExpiryMonth())
                                .expiry_year(cardRequest.getExpiryYear())
                                .number(cardRequest.getCardNumber())
                                .cvv(cardRequest.getCvv())
                                .build())
                .build());
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



        transactionService.saveTransaction(Transactions.builder()
                        .externalRefNumber(payStackResponse.getData().getReference())
                        .transactionRef(generateTxnRef("MANDATE_CHARGE"))
                        .walletId(userCredential.getWalletId())
                        .transactionAmount(50)
                        .createdAt(LocalDateTime.now())
                        .transactionType("CARD SETUP")
                        .transactionStatus("COMPLETED")
                        .transactionCategory("DEBIT")
                .build());

        //Check is card expired

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
                            .transactionCategory("CREDIT")
                    .build());
        }
        boolean isCardExist = cardRepository.existsByWalletId(userCredential.getWalletId());
        com.zeliafinance.identitymanagement.debitmandate.entity.Card existingCard = cardRepository.findByWalletId(userCredential.getWalletId());
        com.zeliafinance.identitymanagement.debitmandate.entity.Card cardToReturn;
        log.info("Does card exist: {}", isCardExist);
        if (isCardExist){
            existingCard.setCardExpiry(expiryDate);
            existingCard.setCardType(payStackResponse.getData().getAuthorization().getCard_type());
            existingCard.setBin(cardRequest.getCardNumber().substring(0, 6));
            existingCard.setLastFour(cardRequest.getCardNumber().substring(cardRequest.getCardNumber().length()-4));
            existingCard.setAuthCode(payStackResponse.getData().getAuthorization().getAuthorization_code());
            existingCard.setWalletId(userCredential.getWalletId());
            cardToReturn = existingCard;

            cardRepository.save(existingCard);
        } else {
            cardToReturn = cardRepository.save(com.zeliafinance.identitymanagement.debitmandate.entity.Card.builder()
                    .walletId(userCredential.getWalletId())
                    .bin(cardRequest.getCardNumber().replaceAll(" ", "").substring(0, 6))
                    .lastFour(cardRequest.getCardNumber().substring(cardRequest.getCardNumber().length()-4))
                    .authCode(payStackResponse.getData().getAuthorization().getAuthorization_code())
                    .cardExpiry(expiryDate)
                    .cardType(payStackResponse.getData().getAuthorization().getCard_type())
                    .build());
        }


        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(payStackResponse)
                .build());
    }

    public ResponseEntity<CustomResponse> fetchUserCardDetails(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        String walletId = userCredential.getWalletId();
        com.zeliafinance.identitymanagement.debitmandate.entity.Card card = cardRepository.findByWalletId(walletId);
        if (card == null){
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage("No Card(s) present")
                    .build());
        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(SUCCESS_MESSAGE)
                        .responseBody(card)
                .build());
    }
}
