package com.zeliafinance.identitymanagement.mappings;

import com.zeliafinance.identitymanagement.debitmandate.dto.CardResponse;
import com.zeliafinance.identitymanagement.debitmandate.entity.Card;
import com.zeliafinance.identitymanagement.debitmandate.repository.CardRepository;
import com.zeliafinance.identitymanagement.dto.DocumentDetails;
import com.zeliafinance.identitymanagement.dto.UserCredentialResponse;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationResponse;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.repository.LoanProductRepository;
import com.zeliafinance.identitymanagement.loanDisbursal.entity.LoanDisbursal;
import com.zeliafinance.identitymanagement.loanDisbursal.repository.LoanDisbursalRepository;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentData;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentResponse;
import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import com.zeliafinance.identitymanagement.loanRepayment.repository.RepaymentsRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    private final LoanDisbursalRepository loanDisbursalRepository;

    public LoanApplicationResponse mapLoanApplicationToUserCredential(LoanApplication loanApplication) {

        Optional<UserCredential> userCredentialOptional = userCredentialRepository.findByWalletId(loanApplication.getWalletId());

        if (userCredentialOptional.isPresent()) {
            UserCredential userCredential = userCredentialOptional.get();
            UserCredentialResponse userCredentialResponse = modelMapper.map(userCredential, UserCredentialResponse.class);
            Card card = cardRepository.findByWalletId(userCredentialResponse.getWalletId());
            if (card == null){
                userCredentialResponse.setCardExists(false);
            } else {
                userCredentialResponse.setCardExists(true);
                userCredentialResponse.setCardDetails(modelMapper.map(card, CardResponse.class));
            }


            LoanApplicationResponse loanApplicationResponse = modelMapper.map(loanApplication, LoanApplicationResponse.class);
            loanApplicationResponse.setUserDetails(userCredentialResponse);
            DocumentDetails documentDetails = new DocumentDetails();
            if (loanApplicationResponse.getCompanyIdCard() != null){
                documentDetails.setCompanyIdCard(loanApplicationResponse.getCompanyIdCard());
            }
            if (loanApplicationResponse.getWardIdCard() != null){
                documentDetails.setWardIdCard(loanApplicationResponse.getWardIdCard());
            }
            if (loanApplicationResponse.getCompanyOfferLetter() != null){
                documentDetails.setCompanyOfferLetter(loanApplicationResponse.getCompanyOfferLetter());
            }
            loanApplicationResponse.setDocumentDetails(documentDetails);


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
                    LoanDisbursal loanDisbursal = loanDisbursalRepository.findByLoanRefNo(loanApplication.getLoanRefNo());
                    int numberOfRepayments = loanApplication.getLoanTenor()/30;
                    double monthlyRepayment;
                    if(loanApplication.getLoanTenor()>30){
                        monthlyRepayment = loanApplication.getAmountToPayBack()/numberOfRepayments;
                    } else {
                        monthlyRepayment = loanApplication.getAmountToPayBack();
                    }

                    List<RepaymentData> repaymentDataList = new ArrayList<>();
                    if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("DENIED")){
                        repaymentResponse = null;
                    } else {
                        repaymentResponse.setPrincipal(loanApplication.getLoanAmount());
                        repaymentResponse.setInterest(loanApplication.getInterest());
                        if (loanApplication.getLoanTenor() <= 30){
                            repaymentResponse.setAmountPaid(repayment.getAmountPaid());
                            if (repayment.getAmountPaid() >= loanApplication.getAmountToPayBack()){
                                repaymentResponse.setRepaymentStatus("PAID");
                            }
                            if (repayment.getAmountPaid() < loanApplication.getAmountToPayBack()){
                                repaymentResponse.setRepaymentStatus("PENDING");
                            }
                            if (repayment.getAmountPaid() < loanApplication.getAmountToPayBack() && LocalDateTime.now().isAfter(loanDisbursal.getDateDisbursed().plusDays(loanApplication.getLoanTenor()))){
                                repaymentResponse.setRepaymentStatus("DEFAULT");
                            }
                                repaymentResponse.setRepaymentMonths(1);
                            RepaymentData  repaymentData = new RepaymentData();
                            repaymentData.setMonthCount(1);

                            repaymentData.setRepaymentDate(String.valueOf(LocalDateTime.from(loanDisbursal.getDateDisbursed().plusDays(loanApplication.getLoanTenor()))));
                            repaymentData.setRepaymentStatus(loanApplication.getRepaymentStatus());
                            repaymentData.setExpectedAmount(loanApplication.getAmountToPayBack());
                            repaymentData.setInterest(loanApplication.getInterest());
                            repaymentData.setAmountPaid(repayment.getAmountPaid());
                            repaymentDataList.add(repaymentData);

                        }
                        int monthCount = 0;
                        if (loanApplication.getLoanTenor() > 30){
                            for (monthCount = 1; monthCount <= numberOfRepayments; monthCount++) {
                                RepaymentData newRepaymentData = new RepaymentData();
                                newRepaymentData.setMonthCount(monthCount);
                                newRepaymentData.setExpectedAmount(monthlyRepayment);
                                newRepaymentData.setInterest(loanApplication.getInterest() / numberOfRepayments);
                                newRepaymentData.setRepaymentDate(loanDisbursal.getDateDisbursed().plusDays(monthCount * 30L).toString());
                                if (repayment.getAmountPaid() >= (monthCount * monthlyRepayment)) {
                                    newRepaymentData.setAmountPaid(monthlyRepayment);
                                    newRepaymentData.setRepaymentStatus("PAID");
                                } else if (repayment.getAmountPaid() == 0 || repayment.getAmountPaid() != (monthCount * monthlyRepayment)) {
                                    newRepaymentData.setAmountPaid(0);
                                    newRepaymentData.setRepaymentStatus("PENDING");
                                } else if (LocalDateTime.now().isAfter(loanDisbursal.getDateDisbursed().plusDays(monthCount * 30L)) && repayment.getAmountPaid() < monthCount * monthlyRepayment) {
                                    newRepaymentData.setAmountPaid(0);
                                    newRepaymentData.setRepaymentStatus("DEFAULT");
                                }

                                repaymentDataList.add(newRepaymentData);
                            }
                        }}
                    assert repaymentResponse != null;
                    repaymentResponse.setRepaymentMonths(numberOfRepayments);
                    if (repayment.getAmountPaid() < loanApplication.getAmountToPayBack()){
                        repaymentResponse.setRepaymentStatus("PENDING");
                    }
                    if (repaymentResponse.getRepaymentMonths() < loanApplication.getAmountToPayBack() && (LocalDateTime.now().isAfter(loanDisbursal.getDateDisbursed().plusDays(loanApplication.getLoanTenor())))){
                        repaymentResponse.setRepaymentStatus("DEFAULT");
                    }
                    if (repayment.getAmountPaid() >= loanApplication.getAmountToPayBack()){
                        repaymentResponse.setRepaymentStatus("PAID");
                    }
                    repaymentResponse.setLoanType(loanApplication.getLoanType());
                    repaymentResponse.setMonthlyRepayment(monthlyRepayment);
                    repaymentResponse.setAmountPaid(repayment.getAmountPaid());
                    repaymentResponse.setRepaymentData(repaymentDataList);
                    repaymentResponse.setWalletId(loanApplicationResponse.getWalletId());
                    repaymentResponse.setLoanTenor(loanApplication.getLoanTenor());

                    return repaymentResponse;

                }).toList();
    }

    public Card mapUserToCard(UserCredentialResponse userCredentialResponse){
        Card card = cardRepository.findByWalletId(userCredentialResponse.getWalletId());
        if (card != null){
            userCredentialResponse.setCardExists(true);
            userCredentialResponse.setCardDetails(modelMapper.map(card, CardResponse.class));
            return cardRepository.findByWalletId(userCredentialResponse.getWalletId());
        }
        userCredentialResponse.setCardExists(false);
        return null;

    }

}
