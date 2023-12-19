package com.zeliafinance.identitymanagement.loan.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.zeliafinance.identitymanagement.debitmandate.entity.Card;
import com.zeliafinance.identitymanagement.dto.*;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationResponse;
import com.zeliafinance.identitymanagement.loan.dto.LoanCalculatorRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanOfferingResponse;
import com.zeliafinance.identitymanagement.loan.entity.ApplicantCategory;
import com.zeliafinance.identitymanagement.loan.entity.EmploymentType;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.entity.LoanProduct;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loan.repository.LoanProductRepository;
import com.zeliafinance.identitymanagement.loan.service.LoanApplicationService;
import com.zeliafinance.identitymanagement.loan.service.LoanCalculatorService;
import com.zeliafinance.identitymanagement.loan.service.LoanOfferingService;
import com.zeliafinance.identitymanagement.loanDisbursal.dto.DisbursalRequest;
import com.zeliafinance.identitymanagement.loanDisbursal.entity.LoanDisbursal;
import com.zeliafinance.identitymanagement.loanDisbursal.repository.LoanDisbursalRepository;
import com.zeliafinance.identitymanagement.loanDisbursal.service.LoanDisbursalService;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentData;
import com.zeliafinance.identitymanagement.loanRepayment.dto.RepaymentResponse;
import com.zeliafinance.identitymanagement.loanRepayment.repository.RepaymentsRepository;
import com.zeliafinance.identitymanagement.mappings.CustomMapper;
import com.zeliafinance.identitymanagement.otp.dto.OtpRequest;
import com.zeliafinance.identitymanagement.otp.dto.OtpValidationRequest;
import com.zeliafinance.identitymanagement.otp.service.OtpService;
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
    private CustomMapper customMapper;
    private LoanOfferingService loanOfferingService;
    private final OtpService otpService;

    private boolean checkIfUserHasApprovedLoan(List<LoanApplication> loanApplications){
        List<String> loanStatuses = loanApplications.stream()
                .map(LoanApplication::getLoanApplicationStatus).toList();
        return loanStatuses.contains("APPROVED");
    }

    private List<LoanProduct> isLoanProductRestricted(List<LoanProduct> loanProducts){
        return loanProducts.stream().filter(loanProduct -> loanProduct.getStatus().equalsIgnoreCase("INACTIVE")).toList();
    }

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
            isLastLoanRepaid = loanApplications.get(loanApplications.size()-1).getLoanApplicationStatus().equalsIgnoreCase("PAID") ||
            loanApplications.get(loanApplications.size()-1).getLoanApplicationStatus().equals("REJECTED")
            || loanApplications.get(loanApplications.size()-1).getLoanApplicationStatus().equalsIgnoreCase("CANCELED")
            ;
        }
        //get Number of loansRepaid
        int numberOfLoansRepaid = repaymentsRepository.findByWalletId(walletId).stream().filter(repayments -> repayments.getRepaymentStatus().equalsIgnoreCase("PAID")).toList().size();
        log.info("Number loans repaid: {}", numberOfLoansRepaid);
        log.info("Loan Amount: {}", request.getLoanAmount());
        log.info("Loan Type: {}", request.getLoanType());
        if((numberOfLoansRepaid < 1 && request.getLoanType().equalsIgnoreCase("Student Personal Loan") && request.getLoanAmount() >= 12001)
        || (numberOfLoansRepaid < 2 && request.getLoanType().equalsIgnoreCase("Student Personal Loan") && request.getLoanAmount() >= 30001)
        ){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Improve your credit rating to unlock more loans")
                            .canApplyForLoan(false)
                    .build());
        }


        //check if user has an approved loan
        if (checkIfUserHasApprovedLoan(loanApplications)){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.APPROVED_LOAN_CHECKS)
                    .build());
        }


        boolean isLoanExists = loanApplicationRepository.existsByWalletId(walletId);
        if (isLoanExists  && !isLastLoanRepaid){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage("You can't apply for a new loan at this time!")
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

        if (checkIfUserHasApprovedLoan(loanApplicationRepository.findByWalletId(loanApplication.getWalletId()).get())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.APPROVED_LOAN_CHECKS)
                    .build());
        }

        if(loanApplication.getLoanApplicationStatus().equalsIgnoreCase("APPROVED") ||
        loanApplication.getLoanApplicationStatus().equalsIgnoreCase("DISBURSED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.LOAN_APPROVED_ERROR)
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
                loanApplication.setMatriculationNumber(request.getMatriculationNumber());

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
                loanApplication.setMatriculationNumber(request.getMatriculationNumber());

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
                loanApplication.setMatriculationNumber(request.getMatriculationNumber());
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
        if(checkIfUserHasApprovedLoan(loanApplicationRepository.findByWalletId(loanApplication.getWalletId()).get())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.APPROVED_LOAN_CHECKS)
                    .build());
        }
        if(loanApplication.getLoanApplicationStatus().equalsIgnoreCase("APPROVED") ||
                loanApplication.getLoanApplicationStatus().equalsIgnoreCase("DISBURSED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage(AccountUtils.LOAN_APPROVED_ERROR)
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
                            .responseMessage(AccountUtils.LOAN_SUBMITTED_ERROR)
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
                loanApplication.setCoSignerEmail(loanApplicationRequest.getCoSignerEmail());
                loanApplication.setCoSignerEmailVerificationStatus("UNVERIFIED");
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
        if(checkIfUserHasApprovedLoan(loanApplicationRepository.findByWalletId(loanApplication.getWalletId()).get())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.APPROVED_LOAN_CHECKS)
                    .build());
        }
        if(loanApplication.getLoanApplicationStatus().equalsIgnoreCase("APPROVED") ||
                loanApplication.getLoanApplicationStatus().equalsIgnoreCase("DISBURSED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage(AccountUtils.LOAN_APPROVED_ERROR)
                    .build());
        }
        if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("SUBMITTED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.LOAN_SUBMITTED_ERROR)
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
            if (!customResponse.getPinVerificationStatus()) {
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

        if(checkIfUserHasApprovedLoan(loanApplicationRepository.findByWalletId(loanApplication.getWalletId()).get())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.APPROVED_LOAN_CHECKS)
                    .build());
        }
        if(loanApplication.getLoanApplicationStatus().equalsIgnoreCase("APPROVED") ||
                loanApplication.getLoanApplicationStatus().equalsIgnoreCase("DISBURSED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage(AccountUtils.LOAN_APPROVED_ERROR)
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
    public ResponseEntity<CustomResponse> initiateCosignerVerification(String loanRefNo) {
//        Fetch Loan Object
        //return error message if loan is not found
        //Send otp
        //return object

        Optional<LoanApplication> loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo);
        if(loanApplication.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.LOAN_NOT_FOUND)
                    .build());
        }
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

        if(checkIfUserHasApprovedLoan(loanApplicationRepository.findByWalletId(loanApplication.get().getWalletId()).get())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage(AccountUtils.APPROVED_LOAN_CHECKS)
                    .build());
        }
        if(loanApplication.get().getLoanApplicationStatus().equalsIgnoreCase("APPROVED") ||
                loanApplication.get().getLoanApplicationStatus().equalsIgnoreCase("DISBURSED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(400)
                    .responseMessage(AccountUtils.LOAN_APPROVED_ERROR)
                    .build());
        }

        otpService.sendOtp(OtpRequest.builder()
                        .email(loanApplication.get().getCoSignerEmail())
                .build());

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.OTP_SENT_MESSAGE)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> validateCosignerEmail(String loanRefNo, OtpValidationRequest otpValidationRequest) {
        Optional<LoanApplication> loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo);
        if (loanApplication.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.LOAN_NOT_FOUND)
                    .build());
        }

        CustomResponse otpResponse = otpService.validateOtp(OtpValidationRequest.builder()
                        .email(loanApplication.get().getCoSignerEmail())
                        .otp(otpValidationRequest.getOtp())
                .build()).getBody();

        assert otpResponse != null;

        if (otpResponse.getOtpStatus()){
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage(AccountUtils.OTP_SENT_MESSAGE)
                    .build());
        }
        else {
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                            .responseBody(otpResponse)
                    .build());
        }
    }

    @Override
    public ResponseEntity<CustomResponse> fetchAllLoanApplications(int pageNo, int pageSize) {
        List<LoanApplication> allLoans = loanApplicationRepository.findAll();
        List<LoanApplication> loanApplications = loanApplicationRepository.findAll()
                .stream().skip(pageNo-1).limit(pageSize)
                .sorted(Comparator.comparing(LoanApplication::getCreatedAt).reversed())
                .toList();

        List<LoanApplicationResponse> loanApplicationList = loanApplications
                .stream().map(loanApplication -> {

                    LoanApplicationResponse loanApplicationResponse = customMapper.mapLoanApplicationToUserCredential(loanApplication);
                    log.info("Loan Application Response: {}", loanApplicationResponse);
                    if(repaymentsRepository.findByLoanRefNo(loanApplication.getLoanRefNo()) != null){
                        loanApplicationResponse.setRepaymentsList(customMapper.mapLoanApplicationToRepayment(loanApplication));
                    } else {
                        loanApplicationResponse.setRepaymentsList(null);
                    }


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
                        .info(Info.builder()
                                .totalElements((long)allLoans.size())
                                .pageSize(pageSize)
                                .totalPages(allLoans.size()/pageSize)
                                .build())

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
    public ResponseEntity<CustomResponse> loanApplicationHistory(int pageNo, int pageSize) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
            String walletId = userCredential.getWalletId();
            List<LoanApplicationResponse> loanApplicationList = loanApplicationRepository.findAll().stream()
                    .filter(loanApplication -> loanApplication.getWalletId().equals(walletId))
                    .skip(pageNo - 1).limit(pageSize).sorted(Comparator.comparing(LoanApplication::getCreatedAt).reversed())
                    .map(loanApplication -> {

                        List<LoanProduct> loanProduct = loanProductRepository.findAll().stream().filter(loanProduct1 -> loanProduct1.getLoanProductName().equalsIgnoreCase(loanApplication.getLoanType())
                                && loanProduct1.getMaxAmount() >= loanApplication.getLoanAmount() && loanProduct1.getMinAmount() <= loanApplication.getLoanAmount() && loanProduct1.getMinDuration() <= loanApplication.getLoanTenor()
                                && loanProduct1.getMaxDuration() >= loanApplication.getLoanTenor() && loanProduct1.getInterestRate() == (loanApplication.getInterestRate())
                        ).toList();

                        log.info("Interest Rate: {}", loanApplication.getInterestRate());
                        LoanApplicationResponse response = modelMapper.map(loanApplication, LoanApplicationResponse.class);
                        response.setLoanProduct(loanProduct);
                        response.setUserDetails(modelMapper.map(userCredential, UserCredentialResponse.class));
                        List<LoanOfferingResponse> loanOfferingResponses = loanOfferingService.fetchLoanOfferingByProductName(response.getLoanType());
                        response.setLoanOfferingResponses(loanOfferingResponses);

                        //List of Disbursals, List of repayments
                        List<LoanDisbursal> disbursalRequests = loanDisbursalRepository.findByWalletId(loanApplication.getWalletId());
                        List<DisbursalRequest> requestList = disbursalRequests.stream().map(loanDisbursal -> modelMapper.map(loanDisbursal, DisbursalRequest.class)).toList();
                        response.setDisbursalList(requestList);

                        response.setRepaymentsList(customMapper.mapLoanApplicationToRepayment(loanApplication));
                        return response;
                    })
                    .toList();


            return ResponseEntity.ok(CustomResponse.builder()
                    .info(Info.builder()
                            .totalElements((long) loanApplicationList.size())
                            .pageSize(pageSize)
                            .totalPages(loanApplicationList.size() / pageSize)
                            .build())
                    .statusCode(200)
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(loanApplicationList)

                    .build());
        } catch (Exception e) {
            log.error("Error in loanApplicationHistory endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomResponse.builder()
                            .statusCode(500)
                            .responseMessage("Internal Server Error")
                            .build());
        }
    }

    @Override
    public ResponseEntity<CustomResponse> viewLoanApplicationsByStatus(String loanApplicationStatus, int pageNo, int pageSize) {
        List<LoanApplicationResponse> loanApplicationList = loanApplicationRepository.findAll().stream().filter(loanApplication -> loanApplication.getLoanApplicationStatus().equalsIgnoreCase(loanApplicationStatus))
                .sorted(Comparator.comparing(LoanApplication::getCreatedAt))
                .skip(pageNo-1).limit(pageSize).map(loanApplication ->{
                    List<RepaymentResponse> repaymentResponses = customMapper.mapLoanApplicationToRepayment(loanApplication);
                            LoanApplicationResponse loanApplicationResponse = modelMapper.map(loanApplication, LoanApplicationResponse.class);
                            loanApplicationResponse.setRepaymentsList(repaymentResponses);
                            return loanApplicationResponse;
                        }
                        ).toList();

        return ResponseEntity.ok(CustomResponse.builder()
                        .info(Info.builder()
                                .totalElements((long)loanApplicationList.size())
                                .totalPages(loanApplicationList.size()/pageSize)
                                .pageSize(pageSize)
                                .build())
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(loanApplicationList)
                .build());
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
        if (loanApplication.getLoanApplicationStatus().equalsIgnoreCase("APPROVED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.CANCEL_APPROVED_LOAN_ERROR)
                    .build());
        }

        if(loanApplication.getLoanApplicationStatus().equalsIgnoreCase("DISBURSED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.CANCEL_DISBURSED_LOAN_ERROR)
                    .build());
        }
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
        UserCredentialResponse userCredentialResponse = modelMapper.map(userCredential, UserCredentialResponse.class);
        Card card = customMapper.mapUserToCard(userCredentialResponse);
        userCredentialResponse.setCardDetails(card);
        loanApplicationResponse.setUserDetails(userCredentialResponse);


        List<RepaymentResponse> repayments = repaymentsRepository.findByLoanRefNo(loanRefNo).stream().map(repayment -> {
            RepaymentResponse repaymentResponse = modelMapper.map(repayment, RepaymentResponse.class);
            List<RepaymentData> repaymentData = new ArrayList<>();
            int monthCount = 1;
            if (loanApplicationResponse.getLoanTenor() <= 30){
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
        if (loanToApprove.getLoanApplicationStatus().equalsIgnoreCase("CANCELED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Error Approving a canceled loan")
                    .build());
        }
        if (loanToApprove.getLoanApplicationStatus().equalsIgnoreCase("DISBURSED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("This loan has already been disbursed")
                    .build());
        }
        loanToApprove.setLoanApplicationStatus("APPROVED");
        loanToApprove.setModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        loanToApprove = loanApplicationRepository.save(loanToApprove);

        String walletId = loanToApprove.getWalletId();
        log.info("Wallet Id: {}", walletId);
        UserCredential userCredential = userCredentialRepository.findByWalletId(walletId).get();
        UserCredentialResponse userCredentialResponse = modelMapper.map(userCredential, UserCredentialResponse.class);
        String email = userCredential.getEmail();

        if (userCredentialResponse.isCardExists()){
            emailService.sendEmailAlert(EmailDetails.builder()
                            .messageBody("Congratulations. Your loan has been approved. Your wallet will be credited shortly")
                            .recipient(email)
                            .subject("LOAN OFFER APPROVED")
                    .build());
        }


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
