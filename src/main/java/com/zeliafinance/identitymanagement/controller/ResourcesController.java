package com.zeliafinance.identitymanagement.controller;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.ResourcesDto;
import com.zeliafinance.identitymanagement.service.ResourcesService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/resources")
@AllArgsConstructor
public class ResourcesController {

    private ResourcesService service;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<CustomResponse> addResource(@RequestBody ResourcesDto resourcesDto){
        return service.saveResource(resourcesDto);
    }

    @GetMapping
    public ResponseEntity<CustomResponse> fetchAllResources(
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "100") int pageSize){
        return service.fetchAllResources(pageNo, pageSize);
    }

    @GetMapping("/lookupCode")
    public ResponseEntity<CustomResponse> fetchResourcesByLookupCode(
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "100") int pageSize,
            @RequestParam(value = "lookupCode") String lookupCode){

        return service.fetchResourcesByLookupCode(pageNo, pageSize, lookupCode);
    }
}
