package com.zeliafinance.identitymanagement.loan.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.EmailDetails;
import com.zeliafinance.identitymanagement.dto.PinSetupDto;
import com.zeliafinance.identitymanagement.dto.UserCredentialResponse;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanCalculatorRequest;
import com.zeliafinance.identitymanagement.loan.entity.ApplicantCategory;
import com.zeliafinance.identitymanagement.loan.entity.EmploymentType;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.entity.LoanProduct;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.repository.LoanProductRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import com.zeliafinance.identitymanagement.loan.service.LoanCalculatorService;
import com.zeliafinance.identitymanagement.loanDisbursal.dto.DisbursalRequest;
import com.zeliafinance.identitymanagement.loanDisbursal.entity.LoanDisbursal;
import com.zeliafinance.identitymanagement.loanDisbursal.repository.LoanDisbursalRepository;
import com.zeliafinance.identitymanagement.loanDisbursal.service.LoanDisbursalService;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentData;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentResponse;
import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import com.zeliafinance.identitymanagement.loanRepayment.repository.RepaymentsRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.service.impl.AuthService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private LoanApplicationRepository loanApplicationRepository;
    private UserCredentialRepository userCredentialRepository;
    private LoanCalculatorService loanCalculatorService;
    private AuthService authService;
    private AccountUtils accountUtils;
    private ModelMapper modelMapper;
    private AmazonS3 amazonS3;
    private LoanProductRepository loanProductRepository;
    private RepaymentsRepository repaymentsRepository;
    private LoanDisbursalRepository loanDisbursalRepository;
    private EmailService emailService;
    private LoanDisbursalService loanDisbursalService;

    @Override
    public ResponseEntity<CustomResponse> stageOne(LoanApplicationRequest request) {
        LoanApplication loanApplication = new LoanApplication();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).orElseThrow();
        String walletId = userCredential.getWalletId();
        List<LoanApplication> loanApplications = loanApplicationRepository.findByWalletId(walletId).get();
        //check for the last loan collected
        boolean isLastLoanRepaid = false;
        if (!loanApplications.isEmpty()){
            loanApplications = loanApplications.stream().sorted(Comparator.comparing(LoanApplication::getCreatedAt)).toList();
            isLastLoanRepaid = loanApplications.get(loanApplications.size()-1).getLoanApplicationStatus().equalsIgnoreCase("REPAID") ||
            loanApplications.get(loanApplications.size()-1).getLoanApplicationStatus().equals("REJECTED")
            || loanApplications.get(loanApplications.size()-1).getLoanApplicationStatus().equalsIgnoreCase("CANCELED")
            ;
        }


        boolean isLoanExists = loanApplicationRepository.existsByWalletId(walletId);
        if (isLoanExists  && !isLastLoanRepaid){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage("You have an unpaid loan still running")
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
            log.info("loan calculator response: {}", loanCalculatorResponse);
            if (loanCalculatorResponse != null){
                loanApplication.setAmountToPayBack(loanCalculatorResponse.getLoanCalculatorResponse().getAmountToPayBack());
                loanApplication.setInterestRate(loanCalculatorResponse.getLoanCalculatorResponse().getInterestRate());
                loanApplication.setInterest(loanCalculatorResponse.getLoanCalculatorResponse().getInterest());
            }
            loanApplication.setCreatedBy(email);
            loanApplication.setModifiedBy(email);
            loanApplication.setLoanApplicationLevel(1);
            loanApplication.setLoanApplicationStatus("DRAFTS");
            loanApplication = loanApplicationRepository.save(loanApplication);

        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                .build());
    }

    private boolean checkCanceledLoan(String loanRefNo){
        return loanApplicationRepository.findByLoanRefNo(loanRefNo).get().getLoanApplicationStatus().equalsIgnoreCase("CANCELED");
    }

    private boolean checkDeniedLoan(String loanRefNo){
        return loanApplicationRepository.findByLoanRefNo(loanRefNo).get().getLoanApplicationStatus().equalsIgnoreCase("DENIED");
    }

    @Override
    public ResponseEntity<CustomResponse> stageTwo(String loanRefNo, LoanApplicationRequest request) throws Exception {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).orElseThrow(Exception::new);
        if (loanApplication.getId() == null){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Loan Not Found")
                    .build());
        }
        if (checkDeniedLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("This loan application has been denied")
                    .build());
        }
        if (checkCanceledLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan with reference No " + loanRefNo + "has been canceled")
                    .build());
        }

        if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("SUBMITTED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Loan has been submitted and already under processing")
                    .build());
        } else {
            String ext;
            String fileName = "";
            if (request.getStudentIdCard() != null){
                ext =  "." + request.getStudentIdCard().substring(request.getStudentIdCard().indexOf("/")+1, request.getStudentIdCard().indexOf(";"));
                fileName = uploadFile(AccountUtils.BUCKET_NAME, request.getStudentIdCard().substring(request.getStudentIdCard().indexOf(",")+1)) + ext;

                log.info("file being processed... {}",  fileName);

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
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }
            if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && request.getApplicantCategory().equalsIgnoreCase(ApplicantCategory.PARENT.toString())){
                loanApplication.setWardLastName(request.getWardLastName());
                loanApplication.setWardFirstName(request.getWardFirstName());
                loanApplication.setWardInstitutionName(request.getWardInstitutionName());
                loanApplication.setApplicantCategory(request.getApplicantCategory());
                loanApplication.setWardIdCard(fileName);

                if (loanApplication.getLoanApplicationLevel() < 2){
                    loanApplication.setLoanApplicationLevel(2);
                }
                loanApplicationRepository.save(loanApplication);
                return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }
            if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && request.getApplicantCategory().equalsIgnoreCase(ApplicantCategory.STUDENT.toString())){
                loanApplication.setWardInstitutionName(request.getWardInstitutionName());
                loanApplication.setFacultyName(request.getFacultyName());
                loanApplication.setDepartmentName(request.getDepartName());
                loanApplication.setApplicantCategory(request.getApplicantCategory());

                log.info("File upload is completed");
                loanApplication.setWardIdCard(fileName);

                if (loanApplication.getLoanApplicationLevel() < 2){
                    loanApplication.setLoanApplicationLevel(2);
                }
                loanApplicationRepository.save(loanApplication);
                return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }
            if(loanApplication.getLoanType().equalsIgnoreCase("STUDENT PERSONAL LOAN")){
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
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }

            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage(AccountUtils.LOAN_NOT_FOUND)
                    .build());

        }

    }

    @Override
    public ResponseEntity<CustomResponse> stageThree(String loanRefNo, LoanApplicationRequest loanApplicationRequest) throws Exception {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).orElseThrow(Exception::new);
        if (loanApplication == null){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan Not Found")
                    .build());
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (checkDeniedLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("This loan application has been denied")
                    .build());
        }
        if (checkCanceledLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan with reference No " + loanRefNo + "has been canceled")
                    .build());
        }

        String companyIdCardExt;
        String companyIdCard = "";
        String companyOfferLetterExt;
        String companyOfferLetter = "";
        if (loanApplicationRequest.getCompanyIdCard() != null && loanApplicationRequest.getCompanyOfferLetter() != null){
            companyIdCardExt =  "." + loanApplicationRequest.getCompanyIdCard().substring(loanApplicationRequest.getCompanyIdCard().indexOf("/")+1, loanApplicationRequest.getCompanyIdCard().indexOf(";"));
            companyIdCard = uploadFile(AccountUtils.BUCKET_NAME, loanApplicationRequest.getCompanyIdCard().substring(loanApplicationRequest.getCompanyIdCard().indexOf(",")+1)) + companyIdCardExt;
            companyOfferLetterExt = "." + loanApplicationRequest.getCompanyOfferLetter().substring(loanApplicationRequest.getCompanyOfferLetter().indexOf("/")+1, loanApplicationRequest.getCompanyOfferLetter().indexOf(";"));
            companyOfferLetter = uploadFile(AccountUtils.BUCKET_NAME, loanApplicationRequest.getCompanyOfferLetter().substring(loanApplicationRequest.getCompanyOfferLetter().indexOf(",")+1)) + companyOfferLetterExt;
        }
        if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("SUBMITTED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan has been submitted and already under processing")
                    .build());
        } else {
            if (loanApplication.getLoanType().equalsIgnoreCase("SME Loan") || loanApplication.getLoanType().equalsIgnoreCase("Student Personal Loan")){
                CustomResponse customResponse = authService.verifyPin(PinSetupDto.builder()
                        .email(email)
                        .pin(loanApplicationRequest.getPin())
                        .confirmPin(loanApplicationRequest.getConfirmPin())
                        .build()).getBody();

                assert customResponse != null;
                if (!customResponse.getPinVerificationStatus()) {
                    return ResponseEntity.internalServerError().body(CustomResponse.builder()
                            .statusCode(500)
                            .responseMessage("Pin Error")
                            .build());
                }
                if (loanApplication.getLoanApplicationLevel() < 3){
                    loanApplication.setLoanApplicationLevel(3);
                }
                loanApplication.setLoanApplicationStatus("SUBMITTED");
                loanApplicationRepository.save(loanApplication);
                return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }

            if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplicationRequest.getEmploymentType() != null){
                if(loanApplication.getApplicantCategory().equals("PARENT") && loanApplicationRequest.getEmploymentType().equals("EMPLOYED")){
                    log.info("Tuition Advance for Employed Parent");
                    loanApplication.setEmploymentType("EMPLOYED");
                    loanApplication.setCompanyName(loanApplicationRequest.getCompanyName());
                    loanApplication.setCompanyAddress(loanApplicationRequest.getCompanyAddress());
                    loanApplication.setMonthlySalary(loanApplicationRequest.getMonthlySalary());
                    loanApplication.setCompanyIdCard(companyIdCard);
                    loanApplication.setCompanyOfferLetter(companyOfferLetter);
                    loanApplication.setSalaryAccount(loanApplicationRequest.getSalaryBankName());
                    loanApplication.setSalaryAccountNumber(loanApplicationRequest.getSalaryAccountNumber());
                    loanApplication.setSalaryAccountName(loanApplicationRequest.getSalaryAccountName());
                    if (loanApplication.getLoanApplicationLevel() < 3){
                        loanApplication.setLoanApplicationLevel(3);
                    }
                    loanApplicationRepository.save(loanApplication);
                    return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                            .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                            .build());
                }
                if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplicationRequest.getEmploymentType().equalsIgnoreCase(EmploymentType.SELF_EMPLOYED.toString())) {
                    log.info("Tuition advance for self employed parent");
                    loanApplication.setEmploymentType("SELF_EMPLOYED");
                    loanApplication.setBusinessName(loanApplicationRequest.getBusinessName());
                    loanApplication.setBusinessAddress(loanApplicationRequest.getBusinessAddress());
                    loanApplication.setBusinessMonthlyEarnings(loanApplicationRequest.getBusinessMonthlyEarnings());
                    loanApplication.setCacRegistration(loanApplicationRequest.getCacRegistration());
                    loanApplication.setBusinessBankName(loanApplicationRequest.getBusinessBankName());
                    loanApplication.setBusinessAccount(loanApplicationRequest.getBusinessAccountNumber());
                    loanApplication.setBusinessAccountName(loanApplicationRequest.getBusinessAccountName());
                    if (loanApplication.getLoanApplicationLevel() < 3) {
                        loanApplication.setLoanApplicationLevel(3);
                    }
                    loanApplicationRepository.save(loanApplication);
                    return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                            .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                            .build());

                }
            }

            }
            log.info("Entering tuition advance for student");
            if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplication.getApplicantCategory().equals(ApplicantCategory.STUDENT.toString())){
                log.info("Tuition Advance for student");
                loanApplication.setCoSignerFirstName(loanApplicationRequest.getCoSignerFirstName());
                loanApplication.setCoSignerLastName(loanApplicationRequest.getCoSignerLastName());
                loanApplication.setCoSignerAddress(loanApplicationRequest.getCoSignerAddress());
                loanApplication.setCoSignerRelationship(loanApplicationRequest.getCoSignerRelationship());
                loanApplication.setCoSignerPhoneNumber(loanApplicationRequest.getCoSignerPhoneNumber());
                loanApplication.setCoSignerEmploymentType(loanApplicationRequest.getCoSignerEmploymentType());
                if (loanApplication.getLoanApplicationLevel() < 3){
                    loanApplication.setLoanApplicationLevel(3);
                }
                loanApplicationRepository.save(loanApplication);
                return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage(AccountUtils.LOAN_NOT_FOUND)
                    .build());

    }

    @Override
    public ResponseEntity<CustomResponse> stageFour(String loanRefNo, LoanApplicationRequest request) throws Exception {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).orElseThrow(Exception::new);
        if (loanApplication == null) {
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan Not Found")
                    .build());
        }
        if (checkDeniedLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("This loan application has been denied")
                    .build());
        }
        if (checkCanceledLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan with reference No " + loanRefNo + "has been canceled")
                    .build());
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String companyIdCardExt;
        String companyIdCard = "";
        String companyOfferLetterExt;
        String companyOfferLetter = "";
        if (request.getCompanyIdCard() != null && request.getCompanyOfferLetter() != null) {
            companyIdCardExt = "." + request.getCompanyIdCard().substring(request.getCompanyIdCard().indexOf("/") + 1, request.getCompanyIdCard().indexOf(";"));
            companyIdCard = uploadFile(AccountUtils.BUCKET_NAME, request.getCompanyIdCard().substring(request.getCompanyIdCard().indexOf(",") + 1)) + companyIdCardExt;
            companyOfferLetterExt = "." + request.getCompanyOfferLetter().substring(request.getCompanyOfferLetter().indexOf("/") + 1, request.getCompanyOfferLetter().indexOf(";"));
            companyOfferLetter = uploadFile(AccountUtils.BUCKET_NAME, request.getCompanyOfferLetter().substring(request.getCompanyOfferLetter().indexOf(",") + 1)) + companyOfferLetterExt;

        }

        if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("SUBMITTED")) {
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan has been submitted and already under processing")
                    .build());
        } else if (request.getCoSignerEmploymentType() != null) {
            if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplication.getCoSignerEmploymentType().equalsIgnoreCase("EMPLOYED")) {
                loanApplication.setCompanyName(request.getCompanyName());
                loanApplication.setCompanyAddress(request.getCompanyAddress());
                loanApplication.setMonthlySalary(request.getMonthlySalary());
                loanApplication.setCompanyIdCard(companyIdCard);
                loanApplication.setCompanyOfferLetter(companyOfferLetter);
                loanApplication.setSalaryAccount(request.getSalaryBankName());
                loanApplication.setSalaryAccountNumber(request.getSalaryAccountNumber());
                loanApplication.setSalaryAccountName(request.getSalaryAccountName());
                if (loanApplication.getLoanApplicationLevel() < 4) {
                    loanApplication.setLoanApplicationLevel(4);
                }
                loanApplicationRepository.save(loanApplication);
                return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }

            if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplication.getCoSignerEmploymentType().equalsIgnoreCase(EmploymentType.SELF_EMPLOYED.toString())) {
                loanApplication.setBusinessName(request.getBusinessName());
                loanApplication.setBusinessAddress(request.getBusinessAddress());
                loanApplication.setBusinessMonthlyEarnings(request.getBusinessMonthlyEarnings());
                loanApplication.setCacRegistration(request.getCacRegistration());
                loanApplication.setBusinessBankName(request.getBusinessBankName());
                loanApplication.setBusinessAccount(request.getBusinessAccountNumber());
                loanApplication.setBusinessAccountName(request.getBusinessAccountName());
                if (loanApplication.getLoanApplicationLevel() < 4) {
                    loanApplication.setLoanApplicationLevel(4);
                }
                loanApplicationRepository.save(loanApplication);
                return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }


        }
        if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplication.getApplicantCategory().equalsIgnoreCase("PARENT")) {
            CustomResponse customResponse = authService.verifyPin(PinSetupDto.builder()
                    .email(email)
                    .pin(request.getPin())
                    .confirmPin(request.getConfirmPin())
                    .build()).getBody();

            assert customResponse != null;
            if (customResponse.getPinVerificationStatus()) {
                return ResponseEntity.internalServerError().body(CustomResponse.builder()
                        .statusCode(500)
                        .responseMessage("Pin Error")
                        .build());
            }
            if (loanApplication.getLoanApplicationLevel() < 4) {
                loanApplication.setLoanApplicationLevel(4);
            }
            loanApplication.setLoanApplicationStatus("SUBMITTED");
            loanApplicationRepository.save(loanApplication);

        }
        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(200)
                .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> stageFive(String loanRefNo, LoanApplicationRequest request) throws Exception {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).orElseThrow(Exception::new);
        if (checkCanceledLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan with reference No " + loanRefNo + "has been canceled")
                    .build());
        }
        if (checkDeniedLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("This loan application has been denied")
                    .build());
        }
        log.info("loan ref no: {}", loanRefNo);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomResponse pinVerificationResponse = authService.verifyPin(PinSetupDto.builder()
                        .email(email)
                        .pin(request.getPin())
                        .confirmPin(request.getConfirmPin())
                .build()).getBody();
        log.info("pin verification response: {}", pinVerificationResponse);
        assert pinVerificationResponse != null;
        if(!pinVerificationResponse.getPinVerificationStatus()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("PIN ERROR")
                    .build());
        }

        if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("SUBMITTED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan has been submitted and already under processing")
                    .build());
        }
        if (loanApplication.getLoanApplicationLevel() < 5){
            loanApplication.setLoanApplicationLevel(5);
        }
        loanApplication.setLoanApplicationStatus("SUBMITTED");
        loanApplication = loanApplicationRepository.save(loanApplication);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationRequest.class))
                .build());
    }


    @Override
    public ResponseEntity<CustomResponse> fetchAllLoanApplications() {
        List<LoanApplicationResponse> loanApplicationList = loanApplicationRepository.findAll()
                .stream().map(loanApplication -> {
                    LoanApplicationResponse loanApplicationResponse = modelMapper.map(loanApplication, LoanApplicationResponse.class);
                    UserCredential userCredential = userCredentialRepository.findByWalletId(loanApplicationResponse.getWalletId()).get();
                    UserCredentialResponse userCredentialResponse = modelMapper.map(userCredential, UserCredentialResponse.class);
                    loanApplicationResponse.setUserDetails(userCredentialResponse);

                    List<Repayments> repayments = repaymentsRepository.findByLoanRefNo(loanApplicationResponse.getLoanRefNo());
                    loanApplicationResponse.setRepaymentsList(repayments.stream().map(repayment -> modelMapper.map(repayment, RepaymentResponse.class)).toList());
                    return loanApplicationResponse;

                    //map user details and repayment
                    })
                .toList();

        if (loanApplicationList.isEmpty()) {
            return ResponseEntity.ok(
                    CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage("You have no loan applications")
                            .build()
            );
        }

        log.info("Loan Applications: {}", loanApplicationList);
        return ResponseEntity.ok(
                CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(loanApplicationList)
                        .build()
        );
    }

    private Optional<LoanApplicationResponse> mapToLoanApplicationResponse(LoanApplication loanApplication) {
        try {
            LoanApplicationResponse loanApplicationResponse = modelMapper.map(loanApplication, LoanApplicationResponse.class);

            UserCredential userCredential = userCredentialRepository.findByWalletId(loanApplication.getWalletId()).orElse(null);
            if (userCredential != null) {
                loanApplicationResponse.setUserDetails(modelMapper.map(userCredential, UserCredentialResponse.class));
            }

            DisbursalRequest loanDisbursal = modelMapper.map(loanDisbursalRepository.findByLoanRefNo(loanApplication.getLoanRefNo()), DisbursalRequest.class);
            if (loanDisbursal != null) {
                loanApplicationResponse.setLoanDisbursal(modelMapper.map(loanDisbursal, DisbursalRequest.class));
            }

            List<RepaymentResponse> repayments = repaymentsRepository.findByLoanRefNo(loanApplication.getLoanRefNo())
                    .stream()
                    .map(repayments1 -> modelMapper.map(repayments1, RepaymentResponse.class))
                    .collect(Collectors.toList());
            loanApplicationResponse.setRepaymentsList(repayments);

            log.info("Loan App Response: {}", loanApplicationResponse);
            return Optional.of(loanApplicationResponse);

        } catch (Exception e) {
            log.error("Error mapping loan application to response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public ResponseEntity<CustomResponse> loanApplicationHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        String walletId = userCredential.getWalletId();
        List<LoanApplicationResponse> loanApplicationList = loanApplicationRepository.findAll().stream()
                .filter(loanApplication -> loanApplication.getWalletId().equals(walletId))
                .sorted(Comparator.comparing(LoanApplication::getCreatedAt).reversed())
                .map(loanApplication -> {

                    List<LoanProduct> loanProduct = loanProductRepository.findAll().stream().filter(loanProduct1 -> loanProduct1.getLoanProductName().equalsIgnoreCase(loanApplication.getLoanType())
                    && loanProduct1.getMaxAmount() >= loanApplication.getLoanAmount() && loanProduct1.getMinAmount() <= loanApplication.getLoanAmount() && loanProduct1.getMinDuration() <= loanApplication.getLoanTenor()
                            && loanProduct1.getMaxDuration() >= loanApplication.getLoanTenor() && loanProduct1.getInterestRate() == (loanApplication.getInterestRate())
                    ).toList();

                    log.info("Interest Rate: {}", loanApplication.getInterestRate());
                    LoanApplicationResponse response = modelMapper.map(loanApplication, LoanApplicationResponse.class);
                    response.setLoanProduct(loanProduct);
                    response.setUserDetails(modelMapper.map(userCredential, UserCredentialResponse.class));

                    //List of Disbursals, List of repayments
                    List<LoanDisbursal> disbursalRequests = loanDisbursalRepository.findByWalletId(loanApplication.getWalletId());
                    List<DisbursalRequest> requestList = disbursalRequests.stream().map(loanDisbursal -> modelMapper.map(loanDisbursal, DisbursalRequest.class)).toList();
                    List<Repayments> repaymentsList = repaymentsRepository.findByLoanRefNo(loanApplication.getLoanRefNo());
                    List<RepaymentResponse> mappedRepaymentList = repaymentsList.stream().map(repayments -> {
                        RepaymentResponse repaymentResponse = new RepaymentResponse();
                        LoanDisbursal loan = loanDisbursalRepository.findByLoanRefNo(repayments.getLoanRefNo());
                        double amountDisbursed = loan.getAmountDisbursed();
                        log.info("Amount Disbursed: {}", amountDisbursed);
                        LoanApplication loanApplication1 = loanApplicationRepository.findByLoanRefNo(repayments.getLoanRefNo()).get();
                        int loanTenor = loanApplication1.getLoanTenor();

                        repaymentResponse.setMonthlyRepayment(loanApplication1.getAmountToPayBack() / (loanTenor/30));
                        repaymentResponse.setNextRepayment(repayments.getNextRepaymentDate());
                        repaymentResponse.setRepaymentMonths(loanTenor/30);
                        repaymentResponse.setLoanType(loanApplication1.getLoanType());
                        repaymentResponse.setAmountPaid(repayments.getAmountPaid());
                        repaymentResponse.setRepaymentStatus(repayments.getRepaymentStatus());
                        repaymentResponse.setUserId(userCredential.getId());
                        repaymentResponse.setLoanTenor(loanTenor);
                        repaymentResponse.setInterest(loanApplication.getInterest());
                        repaymentResponse.setInterestRate((int)loanApplication.getInterestRate());
                        int monthCount = 1;
                        List<RepaymentData> repaymentData = new ArrayList<>();
                        while (monthCount <= repaymentResponse.getRepaymentMonths()){
                            repaymentData.add(RepaymentData.builder()
                                            .monthCount(monthCount)
                                            .amountPaid(repaymentResponse.getAmountPaid())
                                    //expected amount;
                                            .expectedAmount(loanDisbursalRepository.findByLoanRefNo(loanApplication.getLoanRefNo()).getAmountToPayBack()/(loanTenor/30))
                                    .build());
                            monthCount++;

                        }
                        repaymentResponse.setRepaymentData(repaymentData);
                        return repaymentResponse;
                    }).toList();
                    response.setDisbursalList(requestList);
                    response.setRepaymentsList(mappedRepaymentList);
                    return response;
                })
                .toList();



        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(loanApplicationList)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> viewLoanApplicationsByStatus(String loanApplicationStatus) {
        return null;
    }

    @Override
    public ResponseEntity<CustomResponse> updateStageOne(String loanRefNo, LoanApplicationRequest request) throws Exception {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).orElseThrow(Exception::new);
        if (loanApplication == null){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(404)
                            .responseMessage(AccountUtils.LOAN_NOT_FOUND)
                    .build());
        }
        if (checkDeniedLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("This loan application has been denied")
                    .build());
        }
        if (checkCanceledLoan(loanRefNo)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan with reference No " + loanRefNo + "has been canceled")
                    .build());
        }
        if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("SUBMITTED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("Loan has been submitted and already under processing")
                    .build());
        } else {
            if (!loanApplication.getLoanApplicationStatus().equalsIgnoreCase("SUBMITTED")){
                loanApplication.setLoanAmount(request.getLoanAmount());
                loanApplication.setLoanTenor(request.getLoanTenor());
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
                loanApplication.setModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
                loanApplication = loanApplicationRepository.save(loanApplication);
                return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage("You cannot update a submitted loan.")
                    .build());
        }
    }

    @Override
    public ResponseEntity<CustomResponse> searchByPhoneNumber(String phoneNumber) {
        //get wallet id of the user who owns the phone
        UserCredential userCredential = userCredentialRepository.findByPhoneNumber(phoneNumber).get();
        String walletId = userCredential.getWalletId();
        List<LoanApplication> loanApplications = loanApplicationRepository.findByWalletId(walletId).get();
        if (loanApplications.isEmpty()){
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage("User currently has no loan applications")
                    .build());
        }
        List<LoanApplicationResponse> applicationResponseList = loanApplications.stream()
                .map(loanApplication -> {
                    UserCredentialResponse userCredentialResponse = modelMapper.map(userCredentialRepository.findByWalletId(walletId).get(), UserCredentialResponse.class);
                    LoanApplicationResponse response = modelMapper.map(loanApplication, LoanApplicationResponse.class);
                    response.setUserDetails(userCredentialResponse);
                    return response;
                })
                .toList();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(applicationResponseList)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> searchByLoanAppStatus(String loanApplicationStatus) {
        List<LoanApplicationResponse> loanApplications = loanApplicationRepository.findByLoanApplicationStatus(loanApplicationStatus).get().stream()
                .map(loanApplication -> {
                    UserCredentialResponse userCredentialResponse = modelMapper.map(userCredentialRepository.findByWalletId(loanApplication.getWalletId()).get(), UserCredentialResponse.class);
                    LoanApplicationResponse loanApplicationResponse = modelMapper.map(loanApplication, LoanApplicationResponse.class);
                    loanApplicationResponse.setUserDetails(userCredentialResponse);
                    return loanApplicationResponse;
                }).toList();
        if (loanApplications.isEmpty()){
            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage("There are no " + loanApplicationStatus + " loans presently")
                    .build());
        }
        return ResponseEntity.ok(CustomResponse.builder()
                .statusCode(200)
                .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .responseBody(loanApplications)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> deleteLoan(Long loanId) {
        loanApplicationRepository.deleteById(loanId);
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> cancelLoan(String loanRefNo) {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).get();
        loanApplication.setLoanApplicationStatus("CANCELED");
        loanApplicationRepository.save(loanApplication);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchByLoanRefNo(String loanRefNo) {
        LoanApplicationResponse loanApplicationResponse = modelMapper.map(loanApplicationRepository.findByLoanRefNo(loanRefNo).get(), LoanApplicationResponse.class);
        UserCredential userCredential = userCredentialRepository.findByWalletId(loanApplicationResponse.getWalletId()).get();
        loanApplicationResponse.setUserDetails(modelMapper.map(userCredential, UserCredentialResponse.class));
        List<RepaymentResponse> repayments = repaymentsRepository.findByLoanRefNo(loanRefNo).stream().map(repayment -> {
            RepaymentResponse repaymentResponse = modelMapper.map(repayment, RepaymentResponse.class);
            List<RepaymentData> repaymentData = new ArrayList<>();
            int monthCount = 1;
            if (loanApplicationResponse.getLoanTenor() == 30){
                repaymentData.add(RepaymentData.builder()
                        .monthCount(monthCount)
                        .amountPaid(repayment.getAmountPaid())
                        .expectedAmount(loanDisbursalRepository.findByLoanRefNo(loanApplicationResponse.getLoanRefNo()).getAmountToPayBack()/(loanApplicationResponse.getLoanTenor()/30))
                        .build());
                monthCount++;
            }
            while(monthCount <= repaymentResponse.getLoanTenor()/30){
                repaymentData.add(RepaymentData.builder()
                                .monthCount(monthCount)
                                .amountPaid(repayment.getAmountPaid())
                                .expectedAmount(loanDisbursalRepository.findByLoanRefNo(loanApplicationResponse.getLoanRefNo()).getAmountToPayBack()/(loanApplicationResponse.getLoanTenor()/30))
                        .build());
                monthCount++;
            }
            repaymentResponse.setRepaymentData(repaymentData);
            return repaymentResponse;
        }).toList();
        loanApplicationResponse.setRepaymentsList(repayments);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(loanApplicationResponse)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> denyLoan(String loanRefNo) {
        LoanApplication loanToDeny = loanApplicationRepository.findByLoanRefNo(loanRefNo).get();
        loanToDeny.setLoanApplicationStatus("DENIED");
        loanToDeny.setModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        loanToDeny = loanApplicationRepository.save(loanToDeny);

        String walletId = loanToDeny.getWalletId();
        log.info("Wallet Id: {}", walletId);
        UserCredential userCredential = userCredentialRepository.findByWalletId(walletId).get();
        String email = userCredential.getEmail();
        emailService.sendEmailAlert(EmailDetails.builder()
                        .subject("LOAN DENIED")
                        .recipient(email)
                        .messageBody("Your loan application has been denied. Please check our other loan products and apply again")
                .build());
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanToDeny, LoanApplicationResponse.class))
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> approveLoan(String loanRefNo) {
        LoanApplication loanToApprove = loanApplicationRepository.findByLoanRefNo(loanRefNo).get();
        loanToApprove.setLoanApplicationStatus("APPROVED");
        loanToApprove.setModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        loanToApprove = loanApplicationRepository.save(loanToApprove);

        String walletId = loanToApprove.getWalletId();
        log.info("Wallet Id: {}", walletId);
        UserCredential userCredential = userCredentialRepository.findByWalletId(walletId).get();
        String email = userCredential.getEmail();



        emailService.sendEmailAlert(EmailDetails.builder()
                        .messageBody("Congratulations! Your loan has been approved. Log onto the app to accept your loan offer")
                        .recipient(email)
                        .subject("LOAN OFFER APPROVED")
                .build());

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanToApprove, LoanApplicationResponse.class))
                .build());


    }


    public String uploadFile(String bucketName, String multipartFile) {
        String objectKey = UUID.randomUUID().toString();
            byte[] fileData = Base64.getDecoder().decode(multipartFile);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(fileData.length);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, new ByteArrayInputStream(fileData), objectMetadata);
            amazonS3.putObject(putObjectRequest);


        return AccountUtils.AWS_FILE_BASE_URL + "/" + objectKey;
    }
}
