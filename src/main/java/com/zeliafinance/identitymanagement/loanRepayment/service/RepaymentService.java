package com.zeliafinance.identitymanagement.loanRepayment.service;

import com.zeliafinance.identitymanagement.debitmandate.repository.CardRepository;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.EmailDetails;
import com.zeliafinance.identitymanagement.entity.Transactions;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loanDisbursal.entity.LoanDisbursal;
import com.zeliafinance.identitymanagement.loanDisbursal.repository.LoanDisbursalRepository;
import com.zeliafinance.identitymanagement.loanRepayment.dto.PayOffLoanRequest;
import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import com.zeliafinance.identitymanagement.loanRepayment.repository.RepaymentsRepository;
import com.zeliafinance.identitymanagement.repository.TransactionRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.request.ChargeCardRequest;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response.ChargeCardResponse;
import com.zeliafinance.identitymanagement.thirdpartyapis.paystack.service.PayStackService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class RepaymentService {
    private final RepaymentsRepository repaymentsRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final PayStackService payStackService;
    private final LoanDisbursalRepository loanDisbursalRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final EmailService emailService;

    public ResponseEntity<CustomResponse> userRepaymentHistory(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Logged in user: {}", email);
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        String walletId = userCredential.getWalletId();
        log.info("User wallet Id: {}", walletId);
        List<Repayments> repayments = repaymentsRepository.findByWalletId(walletId);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(repayments)
                .build());
    }

    public ResponseEntity<CustomResponse> payOffLoan(PayOffLoanRequest payOffLoanRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserCredential> userCredential = userCredentialRepository.findByEmail(email);

        if (userCredential.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Account doesn't exist")
                    .build());
        }
        String txnRef = AccountUtils.generateTxnRef("DEBIT");
        LoanDisbursal disbursedLoan = loanDisbursalRepository.findByLoanRefNo(payOffLoanRequest.getLoanRefNo());
        double amountToRepay = disbursedLoan.getAmountToPayBack();
        log.info("Amount to pay back: {}", amountToRepay);
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(payOffLoanRequest.getLoanRefNo()).get();
        if (payOffLoanRequest.getChannel().equals("Wallet")){

            userCredential.get().setAccountBalance(userCredential.get().getAccountBalance()- amountToRepay);

            List<Repayments> repayments = repaymentsRepository.findByLoanRefNo(payOffLoanRequest.getLoanRefNo());
            repayments.get(0).setAmountPaid(repayments.get(0).getAmountPaid()+ amountToRepay);

            loanApplication.setAmountRepaid(amountToRepay);
            if (repayments.get(0).getAmountPaid() >= disbursedLoan.getAmountToPayBack()){
                repayments.get(0).setRepaymentStatus("PAID");
                loanApplication.setLoanApplicationStatus("PAID");
                repaymentsRepository.save(repayments.get(0));
            }
            if(userCredential.get().getAccountBalance() < disbursedLoan.getAmountToPayBack()){
                emailService.sendEmailAlert(EmailDetails.builder()
                                .subject("FAILED LOAN REPAYMENT!")
                                .recipient(userCredential.get().getEmail())
                                .messageBody("Your attempted loan repayment failed. Ensure you repay the sum of " + disbursedLoan.getAmountToPayBack() + " your loan before the due date on " + repayments.get(0).getRepaymentDate())
                        .build());
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                                .statusCode(400)
                                .responseMessage("Pay off failed!")
                        .build());
            }
            else {
                repayments.get(0).setRepaymentStatus("ONGOING");
                loanApplication.setLoanApplicationStatus("ONGOING");
            }
            Transactions transactions = Transactions.builder()
                    .transactionCategory("DEBIT")
                    .transactionStatus("COMPLETED")
                    .transactionRef(txnRef)
                    .externalRefNumber(null)
                    .transactionAmount(amountToRepay)
                    .transactionType("Loan Liquidation")
                    .walletId(userCredential.get().getWalletId())
                    .build();
            userCredentialRepository.save(userCredential.get());
            repaymentsRepository.save(repayments.get(0));
            transactionRepository.save(transactions);
            loanApplicationRepository.save(loanApplication);

            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage("Success")
                    .responseBody(repayments.get(0))
                    .build());

        }

        String authorization = cardRepository.findByWalletId(userCredential.get().getWalletId()).getAuthCode();
        ChargeCardResponse chargeCardResponse = payStackService.chargeCard(ChargeCardRequest.builder()
                        .amount(String.valueOf(amountToRepay * 100))
                        .authorization_code(authorization)
                        .email(userCredential.get().getEmail())
                .build());

        String payStackRef = chargeCardResponse.getData().getReference();
        List<Repayments> repayments = repaymentsRepository.findByLoanRefNo(payOffLoanRequest.getLoanRefNo());
        repayments.get(0).setAmountPaid(repayments.get(0).getAmountPaid()+ amountToRepay);
        repaymentsRepository.save(repayments.get(0));
        Transactions transactions = Transactions.builder()
                .transactionCategory("DEBIT")
                .transactionStatus("COMPLETED")
                .transactionRef(txnRef)
                .externalRefNumber(payStackRef)
                .transactionAmount(amountToRepay)
                .transactionType("Loan Liquidation")
                .walletId(userCredential.get().getWalletId())
                .build();
        if (repayments.get(0).getAmountPaid() == disbursedLoan.getAmountToPayBack()){
            repayments.get(0).setRepaymentStatus("PAID");
            loanApplication.setLoanApplicationStatus("PAID");
        }
        else {
            repayments.get(0).setRepaymentStatus("ONGOING");
            loanApplication.setLoanApplicationStatus("ONGOING");
        }
        repaymentsRepository.save(repayments.get(0));
        transactionRepository.save(transactions);
        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(200)
                .responseMessage("Success")
                .build());
    }

    public ResponseEntity<CustomResponse> payOffLoanInstallments(PayOffLoanRequest payOffLoanRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserCredential> userCredential = userCredentialRepository.findByEmail(email);

        if (userCredential.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Account doesn't exist")
                    .build());
        }
        String txnRef = AccountUtils.generateTxnRef("DEBIT");
        LoanDisbursal disbursedLoan = loanDisbursalRepository.findByLoanRefNo(payOffLoanRequest.getLoanRefNo());
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(payOffLoanRequest.getLoanRefNo()).get();
        int numberOfInstallments=0;
        if (loanApplication.getLoanTenor() >= 30){
            numberOfInstallments = loanApplication.getLoanTenor()/30;
        }
        else {
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Instalmental payment is not available for this loan")
                    .build());
        }
        double amountToRepay = disbursedLoan.getAmountToPayBack()/numberOfInstallments;
        log.info("Amount to pay back: {}", amountToRepay);
        if (payOffLoanRequest.getChannel().equals("Wallet")){

            userCredential.get().setAccountBalance(userCredential.get().getAccountBalance()- amountToRepay);

            List<Repayments> repayments = repaymentsRepository.findByLoanRefNo(payOffLoanRequest.getLoanRefNo());
            repayments.get(0).setAmountPaid(repayments.get(0).getAmountPaid()+ amountToRepay);

            loanApplication.setAmountRepaid(amountToRepay);
            if (repayments.get(0).getAmountPaid() >= disbursedLoan.getAmountToPayBack()){
                repayments.get(0).setRepaymentStatus("PAID");
                loanApplication.setLoanApplicationStatus("PAID");
                repaymentsRepository.save(repayments.get(0));
            }
            if(userCredential.get().getAccountBalance() < disbursedLoan.getAmountToPayBack()){
                emailService.sendEmailAlert(EmailDetails.builder()
                        .subject("FAILED LOAN REPAYMENT!")
                        .recipient(userCredential.get().getEmail())
                        .messageBody("Your attempted loan repayment failed. Ensure you repay the sum of " + disbursedLoan.getAmountToPayBack() + " your loan before the due date on " + repayments.get(0).getRepaymentDate())
                        .build());
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(400)
                        .responseMessage("Pay off failed!")
                        .build());
            }
            else {
                repayments.get(0).setRepaymentStatus("ONGOING");
                loanApplication.setLoanApplicationStatus("ONGOING");
            }
            Transactions transactions = Transactions.builder()
                    .transactionCategory("DEBIT")
                    .transactionStatus("COMPLETED")
                    .transactionRef(txnRef)
                    .externalRefNumber(null)
                    .transactionAmount(amountToRepay)
                    .transactionType("Loan Liquidation")
                    .walletId(userCredential.get().getWalletId())
                    .build();
            userCredentialRepository.save(userCredential.get());
            repaymentsRepository.save(repayments.get(0));
            transactionRepository.save(transactions);
            loanApplicationRepository.save(loanApplication);

            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage("Success")
                    .responseBody(repayments.get(0))
                    .build());

        }

        String authorization = cardRepository.findByWalletId(userCredential.get().getWalletId()).getAuthCode();
        ChargeCardResponse chargeCardResponse = payStackService.chargeCard(ChargeCardRequest.builder()
                .amount(String.valueOf(amountToRepay * 100))
                .authorization_code(authorization)
                .email(userCredential.get().getEmail())
                .build());

        String payStackRef = chargeCardResponse.getData().getReference();
        List<Repayments> repayments = repaymentsRepository.findByLoanRefNo(payOffLoanRequest.getLoanRefNo());
        repayments.get(0).setAmountPaid(repayments.get(0).getAmountPaid()+ amountToRepay);
        repaymentsRepository.save(repayments.get(0));
        Transactions transactions = Transactions.builder()
                .transactionCategory("DEBIT")
                .transactionStatus("COMPLETED")
                .transactionRef(txnRef)
                .externalRefNumber(payStackRef)
                .transactionAmount(amountToRepay)
                .transactionType("Loan Liquidation")
                .walletId(userCredential.get().getWalletId())
                .build();
        if (repayments.get(0).getAmountPaid() == disbursedLoan.getAmountToPayBack()){
            repayments.get(0).setRepaymentStatus("PAID");
            loanApplication.setLoanApplicationStatus("PAID");
        }
        else {
            repayments.get(0).setRepaymentStatus("ONGOING");
            loanApplication.setLoanApplicationStatus("ONGOING");
        }
        repaymentsRepository.save(repayments.get(0));
        transactionRepository.save(transactions);
        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(200)
                .responseMessage("Success")
                .build());
    }
}
