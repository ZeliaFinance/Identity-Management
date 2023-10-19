package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.PinSetupDto;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanCalculatorRequest;
import com.zeliafinance.identitymanagement.loan.entity.ApplicantCategory;
import com.zeliafinance.identitymanagement.loan.entity.EmploymentType;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import com.zeliafinance.identitymanagement.loan.service.LoanCalculatorService;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.impl.AuthService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final LoanCalculatorService loanCalculatorService;
    private final AuthService authService;
    private final AccountUtils accountUtils;

    @Override
    public ResponseEntity<CustomResponse> stageOne(LoanApplicationRequest request) {
        LoanApplication loanApplication = new LoanApplication();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).orElseThrow();
        String walletId = userCredential.getWalletId();
        boolean isLoanExists = loanApplicationRepository.existsByWalletId(walletId);
        if (isLoanExists){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage(AccountUtils.PENDING_LOAN_MESSAGE)
                    .build());
        }
        if (loanApplication.getLoanApplicationLevel() < 1){
            loanApplication.setWalletId(userCredential.getWalletId());
            loanApplication.setLoanAmount(request.getLoanAmount());
            loanApplication.setLoanTenor(request.getLoanTenor());
            loanApplication.setLoanRefNo(accountUtils.generateLoanRefNo());
            loanApplication.setLoanType(request.getLoanType());
            CustomResponse loanCalculatorResponse = loanCalculatorService.calculateLoan(LoanCalculatorRequest.builder()
                    .loanType(request.getLoanType())
                    .loanTenor(request.getLoanTenor())
                    .loanAmount(request.getLoanAmount())
                    .build()).getBody();
            if (loanCalculatorResponse != null){
                loanApplication.setAmountToPayBack(loanCalculatorResponse.getLoanCalculatorResponse().getAmountToPayBack());
                loanApplication.setInterestRate(loanCalculatorResponse.getLoanCalculatorResponse().getInterestRate());
            }
            loanApplication.setCreatedBy(email);
            loanApplication.setModifiedBy(email);
            loanApplication.setLoanApplicationLevel(1);
            loanApplication = loanApplicationRepository.save(loanApplication);

        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(loanApplication)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> stageTwo(Optional<MultipartFile> multipartFile, String loanRefNo, LoanApplicationRequest request) throws Exception {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).orElseThrow(Exception::new);
        File file;
        String fileName = "";
        if (multipartFile.isPresent()){
            file = authService.convertMultiPartFileToFile(multipartFile);
            fileName = authService.uploadFileToS3Bucket(file);
        }
        if (loanApplication.getLoanType().equalsIgnoreCase("SME Loan")){
            loanApplication.setCompanyName(request.getCompanyName());
            loanApplication.setCompanyAddress(request.getCompanyAddress());
            loanApplication.setCompanyEmailAddress(request.getCompanyEmailAddress());
            loanApplication.setCacRegistration(request.getCacRegistration());
            loanApplication.setBusinessBankName(request.getBusinessBankName());
            loanApplication.setBusinessAccount(request.getBusinessAccountNumber());
            //insert integration for account name enquiry
            loanApplication.setLoanPurpose(request.getLoanPurpose());
            if (loanApplication.getLoanApplicationLevel() < 2){
                loanApplication.setLoanApplicationLevel(2);
            }

            loanApplicationRepository.save(loanApplication);

            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(loanApplication)
                    .build());
        }
        if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && request.getApplicantCategory().equalsIgnoreCase(ApplicantCategory.PARENT.toString())){
            loanApplication.setWardLastName(request.getWardLastName());
            loanApplication.setWardFirstName(request.getWardFirstName());
            loanApplication.setWardInstitutionName(request.getWardInstitutionName());

            loanApplication.setWardIdCard(fileName);

            if (loanApplication.getLoanApplicationLevel() < 2){
                loanApplication.setLoanApplicationLevel(2);
            }
            loanApplicationRepository.save(loanApplication);
            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(loanApplication)
                    .build());
        }
        if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && request.getApplicantCategory().equalsIgnoreCase(ApplicantCategory.STUDENT.toString())){
            loanApplication.setWardInstitutionName(request.getWardInstitutionName());
            loanApplication.setFacultyName(request.getFacultyName());
            loanApplication.setDepartmentName(request.getDepartName());

            log.info("File upload is completed");
            loanApplication.setWardIdCard(fileName);

            if (loanApplication.getLoanApplicationLevel() < 2){
                loanApplication.setLoanApplicationLevel(2);
            }
            loanApplicationRepository.save(loanApplication);
            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(loanApplication)
                    .build());
        }
        if(loanApplication.getLoanType().equalsIgnoreCase("STUDENT_PERSONAL_LOAN")){
            loanApplication.setWardInstitutionName(request.getWardInstitutionName());
            loanApplication.setFacultyName(request.getFacultyName());
            loanApplication.setDepartmentName(request.getDepartName());
            log.info("File upload is completed");
            loanApplication.setWardIdCard(fileName);
            loanApplicationRepository.save(loanApplication);
            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(loanApplication)
                    .build());
        }

        return ResponseEntity.badRequest().body(CustomResponse.builder()
                .statusCode(400)
                .responseMessage(AccountUtils.LOAN_NOT_FOUND)
                .build());

    }

    @Override
    public ResponseEntity<CustomResponse> stageThree(Optional<MultipartFile> file1, Optional<MultipartFile> file2,
                                                     String loanRefNo, LoanApplicationRequest loanApplicationRequest) throws Exception {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).orElseThrow(Exception::new);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        File companyIdCard;
        File companyOfferLetter;
        String companyIdCardName = "";
        String companyOfferLetterName = "";
        if (file1.isPresent()){
            companyIdCard = authService.convertMultiPartFileToFile(file1);
            companyIdCardName = authService.uploadFileToS3Bucket(companyIdCard);
        }
        if (file2.isPresent()){
            companyOfferLetter = authService.convertMultiPartFileToFile(file2);
            companyOfferLetterName = authService.uploadFileToS3Bucket(companyOfferLetter);
        }

        if (loanApplication.getLoanType().equalsIgnoreCase("SME Loan")){
            CustomResponse customResponse = authService.verifyPin(PinSetupDto.builder()
                            .email(email)
                            .pin(loanApplicationRequest.getPin())
                            .confirmPin(loanApplicationRequest.getConfirmPin())
                    .build()).getBody();

            assert customResponse != null;
            if (customResponse.getPinVerificationStatus() && loanApplication.getLoanApplicationLevel() < 3){
                loanApplication.setLoanApplicationLevel(3);
            }
            loanApplicationRepository.save(loanApplication);
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                            .responseBody(loanApplication)
                    .build());
        }

        if (loanApplication.getLoanStatus().equalsIgnoreCase("Tuition Advance") && loanApplication.getApplicantCategory().equals(ApplicantCategory.PARENT.toString())){
            if(loanApplicationRequest.getEmploymentType().equalsIgnoreCase("EMPLOYED")){
                loanApplication.setEmploymentType(EmploymentType.EMPLOYED.toString());
                loanApplication.setCompanyName(loanApplicationRequest.getCompanyName());
                loanApplication.setCompanyAddress(loanApplicationRequest.getCompanyAddress());
                loanApplication.setMonthlySalary(loanApplicationRequest.getMonthlySalary());
                loanApplication.setCompanyIdCard(companyIdCardName);
                loanApplication.setCompanyOfferLetter(companyOfferLetterName);
                loanApplication.setSalaryAccount("");
                loanApplicationRepository.save(loanApplication);
            }
        }
        return ResponseEntity.badRequest().body(CustomResponse.builder()
                .statusCode(400)
                .responseMessage(AccountUtils.LOAN_NOT_FOUND)
                .build());

    }
}
