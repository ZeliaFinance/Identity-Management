package com.zeliafinance.identitymanagement.config;

import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import com.zeliafinance.identitymanagement.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class MyRunner implements CommandLineRunner {

    private UserCredentialRepository userCredentialRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private ModelMapper modelMapper;


    @Override
    public void run(String... args) {
//        UserCredential superAdmin = UserCredential.builder()
//                .firstName("Hope")
//                .lastName("Onofe")
//                .email("hope.onofe@zeliafinance.com")
//                .role(Role.ROLE_SUPER_ADMIN)
//                .password(passwordEncoder.encode("Payload1."))
//                .build();
//
//        emailService.sendEmailAlert(EmailDetails.builder()
//                        .subject("LOGIN DETAILS!!!")
//                        .recipient(superAdmin.getEmail())
//                        .messageBody("Your zelia-finance profile has been created. Please login to update your password. Your default password is 'admin'")
//                .build());
//
//        userCredentialRepository.save(superAdmin);
//
//        log.info("Super Admin object is:\n {}", modelMapper.map(superAdmin, AdminDto.class));
    }
}
