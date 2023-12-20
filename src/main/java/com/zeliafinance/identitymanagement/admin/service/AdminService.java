package com.zeliafinance.identitymanagement.admin.service;

import com.zeliafinance.identitymanagement.admin.dto.AcceptInviteRequest;
import com.zeliafinance.identitymanagement.admin.dto.AdminResponse;
import com.zeliafinance.identitymanagement.admin.dto.InviteLinkRequest;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.EmailDetails;
import com.zeliafinance.identitymanagement.entity.Role;
import com.zeliafinance.identitymanagement.entity.UserCredential;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AdminService {
    private UserCredentialRepository userCredentialRepository;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;
    private AccountUtils accountUtils;
    private ModelMapper modelMapper;

    public ResponseEntity<CustomResponse> sendInviteLinks(InviteLinkRequest request) {
        String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserCredential> userCredentialExists = userCredentialRepository.findByEmail(request.getEmail());
        if (userCredentialExists.isPresent() && userCredentialExists.get().getInvitationLinkExpiry() != null) {
            if (userCredentialExists.get().getInvitationLinkExpiry().isBefore(LocalDateTime.now())) {
                userCredentialExists.get().setInvitationLinkExpiry(LocalDateTime.now().plusHours(24));
                userCredentialRepository.save(userCredentialExists.get());
                String encryptedEmail = accountUtils.encode(request.getEmail(), 5);
                String invitationLink = "https://sandbox.zeliafinance.com/?email=" + encryptedEmail;

                emailService.sendEmailAlert(EmailDetails.builder()
                        .subject("ADMIN INVITE")
                        .recipient(userCredentialExists.get().getEmail())
                        .messageBody("You have been invited to join your team on zelia finance admin dashboard. Click the link below to accept the invite." + invitationLink)
                        .build());
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(400)
                        .responseMessage("Invitation Link has expired!")
                        .build());
            }
            if (userCredentialExists.get().getInvitationLinkExpiry().isAfter(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                        .statusCode(400)
                        .responseMessage("The User is pending acceptance of a previous invitation link")
                        .build());
            }
            if (userCredentialExists.get().isInviteAccepted()){
                return ResponseEntity.badRequest().body(CustomResponse.builder()
                                .statusCode(400)
                                .responseMessage("This user has already accepted an invitation link")
                        .build());
            }

        }
        if (request.getRole().equalsIgnoreCase( "ROLE_USER")){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("User role cannot be applied here")
                    .build());
        }
        UserCredential userCredential = UserCredential.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .team(request.getTeam())
                .email(request.getEmail())
                .role(Role.valueOf(request.getRole()))
                .createdBy(loggedInUser)
                .modifiedBy(loggedInUser)
                .inviteAccepted(false)
                .invitationLinkExpiry(LocalDateTime.now().plusHours(24))
                .build();
            UserCredential savedAdminCredential = userCredentialRepository.save(userCredential);
            log.info("Saved user: {}", modelMapper.map(savedAdminCredential, AdminResponse.class));
            String encryptedEmail = accountUtils.encode(savedAdminCredential.getEmail(), 5);
            String invitationLink = "http://www.localhost:5173/?email=" + encryptedEmail;

            emailService.sendEmailAlert(EmailDetails.builder()
                    .subject("ADMIN INVITE")
                    .recipient(userCredential.getEmail())
                    .messageBody("You have been invited to join your team on zelia finance admin dashboard. Click the link below to accept the invite." + invitationLink)
                    .build());

            return ResponseEntity.ok(CustomResponse.builder()
                    .statusCode(200)
                    .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                    .responseBody(modelMapper.map(userCredential, AdminResponse.class))
                    .build());
    }

    public ResponseEntity<CustomResponse> acceptInvite(String email, AcceptInviteRequest request){
        if (!accountUtils.isPasswordValid(request.getPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.PASSWORD_INVALID_MESSAGE)
                    .build());
        }
        if (!request.getPassword().equals(request.getConfirmPassword())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage(AccountUtils.PASSWORD_INCORRECT_MESSAGE)
                    .build());
        }


        UserCredential userCredential = userCredentialRepository.findByEmail(accountUtils.decode(email, 5)).get();
        if (userCredential.getInvitationLinkExpiry().isBefore(LocalDateTime.now())){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("Your invite link has expired. Contact your admin.")
                    .build());
        }
        if (userCredential.isInviteAccepted()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(400)
                            .responseMessage("You cannot accept the invite link twice")
                    .build());
        }
        userCredential.setPassword(passwordEncoder.encode(request.getPassword()));
        userCredential.setInviteAccepted(true);

        userCredentialRepository.save(userCredential);

        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(userCredential, AdminResponse.class))
                .build());
    }
}
