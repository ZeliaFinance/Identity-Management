package com.zeliafinance.identitymanagement.mappings;

import com.zeliafinance.identitymanagement.debitmandate.entity.Card;
import com.zeliafinance.identitymanagement.debitmandate.repository.CardRepository;
import com.zeliafinance.identitymanagement.dto.UserCredentialResponse;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationResponse;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.repository.LoanProductRepository;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentData;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentResponse;
import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import com.zeliafinance.identitymanagement.loanRepayment.repository.RepaymentsRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class CustomMapper {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProductRepository loanProductRepository;
    private final RepaymentsRepository repaymentsRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final ModelMapper modelMapper;
    private final CardRepository cardRepository;

    public LoanApplicationResponse mapLoanApplicationToUserCredential(LoanApplication loanApplication) {
        Optional<UserCredential> userCredentialOptional = userCredentialRepository.findByWalletId(loanApplication.getWalletId());

        if (userCredentialOptional.isPresent()) {
            UserCredential userCredential = userCredentialOptional.get();
            UserCredentialResponse userCredentialResponse = modelMapper.map(userCredential, UserCredentialResponse.class);
            Card card = cardRepository.findByWalletId(userCredentialResponse.getWalletId());
            if (card == null){
                userCredentialResponse.setCardExists(false);
            } else {
                userCredentialResponse.setCardDetails(card);
            }


            LoanApplicationResponse loanApplicationResponse = modelMapper.map(loanApplication, LoanApplicationResponse.class);
            loanApplicationResponse.setUserDetails(userCredentialResponse);

            return loanApplicationResponse;
        } else {
            // Handle the case where the UserCredential is not present
            // Set userDetails to null and proceed
            LoanApplicationResponse loanApplicationResponse = modelMapper.map(loanApplication, LoanApplicationResponse.class);
            loanApplicationResponse.setUserDetails(null);
            return loanApplicationResponse;
        }
    }


    public List<RepaymentResponse> mapLoanApplicationToRepayment(LoanApplication loanApplication){
        List<Repayments> repayments = repaymentsRepository.findByLoanRefNo(loanApplication.getLoanRefNo());
        //log.info("Repayment Info: {}", repayments.get(0).getLoanRefNo());
        LoanApplicationResponse loanApplicationResponse = new LoanApplicationResponse();
        return repayments.stream()
                .map(repayment -> {
                    RepaymentResponse repaymentResponse = new RepaymentResponse();
                    List<RepaymentData> repaymentDataList = new ArrayList<>();
                    if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("DENIED")){
                        repaymentResponse = null;
                    } else {
                        repaymentResponse.setPrincipal(loanApplication.getLoanAmount());
                        repaymentResponse.setInterest(loanApplication.getInterest());
                        if (loanApplication.getLoanTenor() <= 30){
                            repaymentResponse.setRepaymentMonths(1);
                            RepaymentData  repaymentData = new RepaymentData();
                            repaymentData.setMonthCount(1);
                            if (loanApplication.getDateDisbursed() == null){
                                repaymentData.setRepaymentDate(null);
                            }
                            repaymentData.setRepaymentDate(String.valueOf(LocalDate.from(loanApplication.getCreatedAt().plusDays(loanApplication.getLoanTenor()))));
                            repaymentData.setRepaymentStatus(loanApplication.getRepaymentStatus());
                            repaymentData.setExpectedAmount(loanApplication.getAmountToPayBack());
                            repaymentData.setInterest(loanApplication.getInterest());
                            repaymentData.setAmountPaid(loanApplication.getAmountRepaid());
                            repaymentDataList.add(repaymentData);

                        } else {
                            int numberOfRepayments = loanApplication.getLoanTenor()/30;
                            log.info("Number of repayments: {}", numberOfRepayments);
                            int monthCount = 1;
                            RepaymentData repaymentData = new RepaymentData();
                            repaymentData.setRepaymentDate(String.valueOf(LocalDate.from(loanApplication.getCreatedAt().plusDays(loanApplication.getLoanTenor()))));
                            repaymentData.setMonthCount(monthCount);
                            repaymentData.setRepaymentStatus(loanApplication.getRepaymentStatus());
                            repaymentData.setExpectedAmount(loanApplication.getAmountToPayBack()/numberOfRepayments);
                            repaymentData.setInterest(loanApplication.getInterest());
                            repaymentData.setAmountPaid(loanApplication.getAmountRepaid());
                            repaymentDataList.add(repaymentData);
                            while (monthCount < numberOfRepayments){
                                double monthlyRepayment = loanApplication.getAmountToPayBack()/numberOfRepayments;
                                String repaymentStatus;
                                if (loanApplication.getAmountRepaid() == (repaymentData.getExpectedAmount()*monthCount)){
                                    repaymentStatus = "REPAID";
                                } else {
                                    repaymentStatus = "PENDING";
                                }
                                repaymentData.setRepaymentDate(LocalDate.parse(repaymentData.getRepaymentDate(), DateTimeFormatter.ISO_DATE).plusDays(30).toString());
                                repaymentDataList.add(RepaymentData.builder()
                                                .monthCount(++monthCount)
                                                .repaymentDate(repaymentData.getRepaymentDate())
                                                .repaymentStatus(repaymentStatus)
                                                .expectedAmount(monthlyRepayment)
                                        .build());
                            }
                            repaymentResponse.setRepaymentMonths(loanApplication.getLoanTenor()/30);
                        }
                        repaymentResponse.setNextRepayment(loanApplication.getDateDisbursed());
                        repaymentResponse.setRepaymentData(repaymentDataList);
                    }

                    return repaymentResponse;

                }).toList();
    }

    public Card mapUserToCard(UserCredentialResponse userCredentialResponse){
        Card card = cardRepository.findByWalletId(userCredentialResponse.getWalletId());
        if (card != null){
            userCredentialResponse.setCardExists(true);
            userCredentialResponse.setCardDetails(card);
            return cardRepository.findByWalletId(userCredentialResponse.getWalletId());
        }
        userCredentialResponse.setCardExists(false);
        return null;

    }
}
