package com.zeliafinance.identitymanagement.debitmandate.service;

import com.zeliafinance.identitymanagement.debitmandate.dto.InstitutionRequest;
import com.zeliafinance.identitymanagement.debitmandate.entity.Institution;
import com.zeliafinance.identitymanagement.debitmandate.repository.InstitutionRepository;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final ModelMapper modelMapper;

    public ResponseEntity<CustomResponse> saveInstitution(String loanRefNo, InstitutionRequest request){
        Institution institution = institutionRepository.save(Institution.builder()
                        .studentFirstName(request.getStudentFirstName())
                        .studentLastName(request.getStudentLastName())
                        .facultyName(request.getFacultyName())
                        .departmentName(request.getDepartmentName())
                        .matriculationNumber(request.getMatriculationNumber())
                        .institutionBankName(request.getInstitutionBankName())
                        .institutionAccountNumber(request.getInstitutionAccountNumber())
                        .beneficiaryName(request.getBeneficiaryName())
                        .loanRefNo(loanRefNo)
                        .createdBy(SecurityContextHolder.getContext().getAuthentication().getName())
                .build());

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(institution, InstitutionRequest.class))
                .build());
    }
}
