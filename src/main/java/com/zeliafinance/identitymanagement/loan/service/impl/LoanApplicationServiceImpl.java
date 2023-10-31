package com.zeliafinance.identitymanagement.loan.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.PinSetupDto;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationRequest;
import com.zeliafinance.identitymanagement.loan.dto.LoanApplicationResponse;
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
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Comparator;
import java.util.List;
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
    private final ModelMapper modelMapper;

    @Override
    public ResponseEntity<CustomResponse> stageOne(LoanApplicationRequest request) {
        LoanApplication loanApplication = new LoanApplication();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).orElseThrow();
        String walletId = userCredential.getWalletId();
        boolean isLoanExists = loanApplicationRepository.existsByWalletId(walletId);
        if (isLoanExists && loanApplication.getLoanApplicationStatus().equals("SUBMITTED")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .responseMessage(AccountUtils.SUBMITTED_LOAN_UPDATE_ERROR)
                    .build());
        }
//        if (if loanApplication.gloanApplication.getLoanApplicationStatus().equalsIgnoreCase("SUBMITTED")){
//            return ResponseEntity.badRequest().body(CustomResponse.builder()
//                            .statusCode(400)
//                            .responseMessage(AccountUtils.SUBMITTED_LOAN_UPDATE_ERROR)
//                    .build());
//        }
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
            loanApplication.setLoanApplicationStatus("DRAFTS");
            loanApplication = loanApplicationRepository.save(loanApplication);

        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
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
                    .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                    .build());
        }
        if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && request.getApplicantCategory().equalsIgnoreCase(ApplicantCategory.PARENT.toString())){
            loanApplication.setWardLastName(request.getWardLastName());
            loanApplication.setWardFirstName(request.getWardFirstName());
            loanApplication.setWardInstitutionName(request.getWardInstitutionName());
            loanApplication.setApplicantCategory(ApplicantCategory.PARENT.toString());
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
                    .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
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

        if (loanApplication.getLoanType().equalsIgnoreCase("SME Loan") || loanApplication.getLoanType().equalsIgnoreCase("STUDENT_PERSONAL_LOAN")){
            CustomResponse customResponse = authService.verifyPin(PinSetupDto.builder()
                            .email(email)
                            .pin(loanApplicationRequest.getPin())
                            .confirmPin(loanApplicationRequest.getConfirmPin())
                    .build()).getBody();

            assert customResponse != null;
            if (customResponse.getPinVerificationStatus() && loanApplication.getLoanApplicationLevel() < 3){
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

        if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplication.getApplicantCategory().equals(ApplicantCategory.PARENT.toString())){
            if(loanApplicationRequest.getEmploymentType().equalsIgnoreCase("EMPLOYED")){
                loanApplication.setEmploymentType(EmploymentType.EMPLOYED.toString());
                loanApplication.setCompanyName(loanApplicationRequest.getCompanyName());
                loanApplication.setCompanyAddress(loanApplicationRequest.getCompanyAddress());
                loanApplication.setMonthlySalary(loanApplicationRequest.getMonthlySalary());
                loanApplication.setCompanyIdCard(companyIdCardName);
                loanApplication.setCompanyOfferLetter(companyOfferLetterName);
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
            if (loanApplication.getEmploymentType().equalsIgnoreCase(EmploymentType.SELF_EMPLOYED.toString())){
                loanApplication.setBusinessName(loanApplicationRequest.getBusinessName());
                loanApplication.setBusinessAddress(loanApplicationRequest.getBusinessAddress());
                loanApplication.setBusinessMonthlyEarnings(loanApplicationRequest.getBusinessMonthlyEarnings());
                loanApplication.setCacRegistration(loanApplicationRequest.getCacRegistration());
                loanApplication.setBusinessBankName(loanApplicationRequest.getBusinessBankName());
                loanApplication.setBusinessAccount(loanApplicationRequest.getBusinessAccountNumber());
                loanApplication.setBusinessAccountName(loanApplicationRequest.getBusinessAccountName());
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
        }
        if (loanApplication.getLoanStatus().equalsIgnoreCase("Tuition Advance") && loanApplication.getApplicantCategory().equals(ApplicantCategory.STUDENT.toString())){
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
    public ResponseEntity<CustomResponse> stageFour(Optional<MultipartFile> file1, Optional<MultipartFile> file2, String loanRefNo, LoanApplicationRequest request) throws Exception {
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

        if(loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplication.getCoSignerEmploymentType().equalsIgnoreCase("EMPLOYED")){
            loanApplication.setCompanyName(request.getCompanyName());
            loanApplication.setCompanyAddress(request.getCompanyAddress());
            loanApplication.setMonthlySalary(request.getMonthlySalary());
            loanApplication.setCompanyIdCard(companyIdCardName);
            loanApplication.setCompanyOfferLetter(companyOfferLetterName);
            loanApplication.setSalaryAccount(request.getSalaryBankName());
            loanApplication.setSalaryAccountNumber(request.getSalaryAccountNumber());
            loanApplication.setSalaryAccountName(request.getSalaryAccountName());
            if (loanApplication.getLoanApplicationLevel() < 4){
                loanApplication.setLoanApplicationLevel(4);
            }
            loanApplicationRepository.save(loanApplication);
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                            .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                    .build());
        }

        if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance") && loanApplication.getCoSignerEmploymentType().equalsIgnoreCase(EmploymentType.SELF_EMPLOYED.toString())){
            loanApplication.setBusinessName(request.getBusinessName());
            loanApplication.setBusinessAddress(request.getBusinessAddress());
            loanApplication.setBusinessMonthlyEarnings(request.getBusinessMonthlyEarnings());
            loanApplication.setCacRegistration(request.getCacRegistration());
            loanApplication.setBusinessBankName(request.getBusinessBankName());
            loanApplication.setBusinessAccount(request.getBusinessAccountNumber());
            loanApplication.setBusinessAccountName(request.getBusinessAccountName());
            if (loanApplication.getLoanApplicationLevel() < 4){
                loanApplication.setLoanApplicationLevel(4);
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
    public ResponseEntity<CustomResponse> stageFive(String loanRefNo, LoanApplicationRequest request) throws Exception {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanRefNo(loanRefNo).orElseThrow(Exception::new);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomResponse pinVerificationResponse = authService.verifyPin(PinSetupDto.builder()
                        .pin(request.getPin())
                        .confirmPin(request.getConfirmPin())
                .build()).getBody();
        if (loanApplication.getLoanType().equalsIgnoreCase("Tuition Advance")){
            if (loanApplication.getLoanApplicationLevel() < 5 && pinVerificationResponse.getPinVerificationStatus()){
                loanApplication.setLoanApplicationLevel(5);
                loanApplication.setLoanApplicationStatus("SUBMITTED");
                loanApplicationRepository.save(loanApplication);
                return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(loanApplication, LoanApplicationResponse.class))
                        .build());
            }
        }
        return ResponseEntity.badRequest().body(CustomResponse.builder()
                .statusCode(400)
                .responseMessage(AccountUtils.LOAN_NOT_FOUND)
                .build());


    }

    @Override
    public ResponseEntity<CustomResponse> fetchAllLoanApplications() {
        List<LoanApplicationResponse> loanApplicationList = loanApplicationRepository.findAll()
                .stream().map(loanApplication -> modelMapper.map(loanApplication, LoanApplicationResponse.class)).toList();
        if (loanApplicationList.isEmpty()){
            return ResponseEntity.ok(CustomResponse.builder()
                            .statusCode(200)
                            .responseMessage("You have no loan applications")
                    .build());
        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(loanApplicationList)
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> loanApplicationHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserCredential userCredential = userCredentialRepository.findByEmail(email).get();
        String walletId = userCredential.getWalletId();
        List<LoanApplicationResponse> loanApplicationList = loanApplicationRepository.findAll().stream()
                .filter(loanApplication -> loanApplication.getWalletId().equals(walletId))
                .sorted(Comparator.comparing(LoanApplication::getCreatedAt).reversed())
                .map(loanApplication -> modelMapper.map(loanApplication, LoanApplicationResponse.class))
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


}
