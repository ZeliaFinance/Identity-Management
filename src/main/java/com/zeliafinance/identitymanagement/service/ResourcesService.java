package com.zeliafinance.identitymanagement.service;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.ResourcesDto;
import org.springframework.http.ResponseEntity;

public interface ResourcesService {
    ResponseEntity<CustomResponse> saveResource(ResourcesDto resourcesDto);
    ResponseEntity<CustomResponse> fetchAllResources(int pageNo, int pageSize);
    ResponseEntity<CustomResponse> fetchResourcesByLookupCode(int pageNo, int pageSize, String lookupCode);
    ResponseEntity<CustomResponse> fetchDistinctLookupCodes(int pageNo, int pageSize);
}
