package com.zeliafinance.identitymanagement.admin.controller;

import com.zeliafinance.identitymanagement.admin.dto.AcceptInviteRequest;
import com.zeliafinance.identitymanagement.admin.dto.InviteLinkRequest;
import com.zeliafinance.identitymanagement.admin.service.AdminService;
import com.zeliafinance.identitymanagement.dto.CustomResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin")
@AllArgsConstructor
public class AdminController {
    private AdminService service;

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @PostMapping("/sendAdminInvitation")
    public ResponseEntity<CustomResponse> sendInviteLink(@RequestBody InviteLinkRequest request){
        return service.sendInviteLinks(request);
    }

    @PostMapping("/acceptInvite")
    public ResponseEntity<CustomResponse> acceptInviteLink(@RequestParam String email, @RequestBody AcceptInviteRequest request){
        return service.acceptInvite(email, request);
    }
}
