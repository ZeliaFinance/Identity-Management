package com.zeliafinance.identitymanagement.dashboard.service;

import com.zeliafinance.identitymanagement.dashboard.dto.DashboardResponse;
import com.zeliafinance.identitymanagement.dashboard.dto.UserInfo;
import com.zeliafinance.identitymanagement.entity.Role;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.loan.entity.LoanApplication;
import com.zeliafinance.identitymanagement.loan.repository.LoanApplicationRepository;
import com.zeliafinance.identitymanagement.loanDisbursal.entity.LoanDisbursal;
import com.zeliafinance.identitymanagement.loanDisbursal.repository.LoanDisbursalRepository;
import com.zeliafinance.identitymanagement.loanRepayment.entity.Repayments;
import com.zeliafinance.identitymanagement.loanRepayment.repository.RepaymentsRepository;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DashboardService {

    private final UserCredentialRepository userCredentialRepository;
    private final LoanDisbursalRepository loanDisbursalRepository;
    private final RepaymentsRepository repaymentsRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    public ResponseEntity<DashboardResponse> dashboardData(Integer year, Integer month, Integer day, Integer hour, String startDate, String endDate){
        DashboardResponse dashboardResponse = new DashboardResponse();
        UserInfo userInfo = UserInfo.builder()
                .numberOfUsers(countAllUsers())
                .nonVerifiedUsers(countUnVerifiedUsers())
                .verifiedUsers(countVerifiedUsers())
                .build();
        dashboardResponse.setUserInfo(userInfo);
        dashboardResponse.setAmountDisbursed(totalDisbursements());
        dashboardResponse.setTotalRepayments(totalRepayments());
        dashboardResponse.setInterest(totalInterest());
        dashboardResponse.setPrincipal(totalPrincipal());
        dashboardResponse.setLoanDisbursal(loanDisbursalList(year, month, day, hour, startDate, endDate));
        dashboardResponse.setLoanRepayments(loanRepayments(year, month, day, hour, startDate, endDate));

        return ResponseEntity.ok(dashboardResponse);

    }

    private long countAllUsers(){
        return usersWithoutAdminRights().size();
    }

    private List<UserCredential> usersWithoutAdminRights(){
        return userCredentialRepository.findAll().stream().filter(userCredential -> userCredential.getRole().equals(Role.ROLE_USER)).toList();
    }

    private long countVerifiedUsers(){
        return usersWithoutAdminRights().stream().filter(userCredential -> userCredential.getEmailVerifyStatus().equalsIgnoreCase("VERIFIED")).count();
    }

    private long countUnVerifiedUsers(){
        return usersWithoutAdminRights().stream().filter(userCredential -> userCredential.getEmailVerifyStatus().equalsIgnoreCase("UNVERIFIED")).count();
    }

    private double totalDisbursements(){
        return loanDisbursalRepository.findAll().stream().mapToDouble(LoanDisbursal::getAmountDisbursed).sum();
    }

    private double totalRepayments(){
        return loanDisbursalRepository.findAll().stream().mapToDouble(LoanDisbursal::getAmountToPayBack).sum();
    }

    private double totalInterest(){
        return totalRepayments() - totalDisbursements();
    }

    private double totalPrincipal(){
        return loanApplicationRepository.findAll().stream().mapToDouble(LoanApplication::getLoanAmount).sum();
    }

    private List<LoanDisbursal> loanDisbursalList (Integer year, Integer month, Integer day, Integer hour, String startDate, String endDate){
        List<LoanDisbursal> loanDisbursalList = new ArrayList<>();
        if (year != null){
            loanDisbursalList =  loanDisbursalRepository.findAll().stream().filter(loanDisbursal -> loanDisbursal.getDateDisbursed().getYear() == year).sorted(Comparator.comparing(LoanDisbursal::getDateDisbursed)).toList();
        }
        if (month != null){
            int startMonth = LocalDate.now().getMonthValue()-month;
            int endMonth = LocalDate.now().getMonthValue();
            log.info("Start Month: {}", startMonth);
            loanDisbursalList = loanDisbursalRepository.findAll().stream().filter(loanDisbursal -> loanDisbursal.getDateDisbursed().getMonthValue() >= startMonth && loanDisbursal.getDateDisbursed().getMonthValue() <= endMonth).sorted(Comparator.comparing(LoanDisbursal::getDateDisbursed)).toList();
        }

        if (day != null){
            int startDay = LocalDateTime.now().minusDays(day).getDayOfMonth();
            loanDisbursalList = loanDisbursalRepository.findAll().stream().filter(loanDisbursal -> loanDisbursal.getDateDisbursed().getDayOfMonth() >= startDay && loanDisbursal.getDateDisbursed().getDayOfMonth() <= LocalDateTime.now().getDayOfMonth()).sorted(Comparator.comparing(LoanDisbursal::getDateDisbursed)).toList();
        }

        if (hour != null){
            int startHour = LocalDateTime.now().minusHours(hour).getHour();
            int endHour = LocalDateTime.now().getHour();
            loanDisbursalList = loanDisbursalRepository.findAll().stream().filter(loanDisbursal -> loanDisbursal.getDateDisbursed().getHour() >= startHour && loanDisbursal.getDateDisbursed().getHour() <= endHour).toList();
        }

        if (startDate != null){
            LocalDate start = java.time.LocalDate.parse(startDate);
            log.info("Start Date String: {}\nConverted Start date: {}", startDate, start);
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now();
            log.info("End Date String: {}\nConverted End date: {}", endDate, end);
            loanDisbursalList = loanDisbursalRepository.findAll().stream().filter(loanDisbursal -> loanDisbursal.getDateDisbursed().toLocalDate().isAfter(start) && loanDisbursal.getDateDisbursed().isBefore(end.atStartOfDay())).sorted(Comparator.comparing(LoanDisbursal::getDateDisbursed)).toList();
        }

        return loanDisbursalList;

    }

    private List<Repayments> loanRepayments(Integer year, Integer month, Integer day, Integer hour, String startDate, String endDate){
        List<Repayments> repaymentsList = new ArrayList<>();
        double sumOfRepayments;
        if (year != null){
            repaymentsList = repaymentsRepository.findAll().stream().filter(repayments -> repayments.getRepaymentDate()!= null &&repayments.getRepaymentDate().getYear() == year).sorted(Comparator.comparing(Repayments::getRepaymentDate)).toList();
        }
        if (month != null){
            int startMonth = LocalDate.now().getMonthValue()-month;
            int endMonth = LocalDate.now().getDayOfMonth();
            log.info("Start Month: {}", startMonth);
            repaymentsList = repaymentsRepository.findAll().stream().filter(repayments -> repayments.getRepaymentDate() != null && repayments.getRepaymentDate().getMonthValue() >= startMonth && repayments.getRepaymentDate().getMonthValue() <= endMonth).sorted(Comparator.comparing(Repayments::getRepaymentDate)).toList();
        }

        if (day != null){
            int startDay = LocalDateTime.now().minusDays(day).getDayOfMonth();
            repaymentsList = repaymentsRepository.findAll().stream().filter(repayments -> repayments.getRepaymentDate() != null && repayments.getRepaymentDate().getDayOfMonth() >= startDay && repayments.getRepaymentDate().getDayOfMonth() <= LocalDateTime.now().getDayOfMonth()).sorted(Comparator.comparing(Repayments::getRepaymentDate)).toList();
        }

        if (hour != null){
            int startHour = LocalDateTime.now().minusHours(hour).getHour();
            int endHour = LocalDateTime.now().getHour();
            repaymentsList = repaymentsRepository.findAll().stream().filter(repayments -> repayments.getRepaymentDate() != null && repayments.getRepaymentDate().getHour() >= startHour && repayments.getRepaymentDate().getHour() <= endHour).sorted(Comparator.comparing(Repayments::getRepaymentDate)).toList();
        }

        if (startDate != null){
            LocalDate start = java.time.LocalDate.parse(startDate);
            log.info("Start Date String: {}\nConverted Start date: {}", startDate, start);
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now();
            log.info("End Date String: {}\nConverted End date: {}", endDate, end);
            repaymentsList = repaymentsRepository.findAll().stream().filter(repayments -> repayments.getRepaymentDate() != null && repayments.getRepaymentDate().isAfter(start.atStartOfDay()) && repayments.getRepaymentDate().isBefore(end.atStartOfDay())).sorted(Comparator.comparing(Repayments::getRepaymentDate)).toList();
        }

        return repaymentsList;
    }
}
