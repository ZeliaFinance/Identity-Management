package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.dto.CustomResponse;
import com.zeliafinance.identitymanagement.dto.Info;
import com.zeliafinance.identitymanagement.dto.ResourcesDto;
import com.zeliafinance.identitymanagement.entity.Resources;
import com.zeliafinance.identitymanagement.repository.ResourcesRepository;
import com.zeliafinance.identitymanagement.service.ResourcesService;
import com.zeliafinance.identitymanagement.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ResourcesServiceImpl implements ResourcesService {

    private ResourcesRepository resourcesRepository;
    private ModelMapper modelMapper;


    @Override
    public ResponseEntity<CustomResponse> saveResource(ResourcesDto resourcesDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Resources resource = Resources.builder()
                .lookupCode(resourcesDto.getLookupCode())
                .lookupValue(resourcesDto.getLookupValue())
                .description(resourcesDto.getDescription())
                .modifiedBy(username)
                .createdBy(username)
                .build();

        Resources savedResource = resourcesRepository.save(resource);

        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.SUCCESS_CODE)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(savedResource, ResourcesDto.class))
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchAllResources(int pageNo, int pageSize) {
        List<Resources> resourcesList = resourcesRepository.findAll();
        List<ResourcesDto> resourcesDtos = resourcesList.stream().map(resource -> modelMapper.map(resource, ResourcesDto.class)).skip(pageNo-1).limit(pageSize).toList();
        int totalPages;
        if (resourcesList.size() <= 100){
            totalPages =1;
        } else {
            totalPages = resourcesList.size() / 100;
        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.SUCCESS_CODE)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(resourcesDtos)
                        .info(Info.builder()
                                .totalPages(totalPages)
                                .pageSize(pageSize)
                                .totalElements((long) resourcesList.size())
                                .build())
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchResourcesByLookupCode(int pageNo, int pageSize, String lookupCode) {
        List<Resources> resource = resourcesRepository.findAll().stream().filter(item -> item.getLookupCode().equalsIgnoreCase(lookupCode)).toList();
        if (resource.size()<1){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .responseCode(AccountUtils.RESOURCE_NOT_FOUND_CODE)
                            .responseMessage(AccountUtils.RESOURCE_NOT_FOUND_MESSAGE)
                    .build());
        }
        List<ResourcesDto> resourcesList = resourcesRepository.findAll().stream().filter(resources -> resources.getLookupCode().equalsIgnoreCase(lookupCode))
                .map(res -> modelMapper.map(res, ResourcesDto.class))
                .toList();

        int totalPages;
        if (resourcesList.size() <= 100){
            totalPages =1;
        } else {
            totalPages = resourcesList.size() / 100;
        }

        return ResponseEntity.ok(CustomResponse.builder()
                        .responseCode(AccountUtils.SUCCESS_CODE)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(resourcesList)
                        .info(Info.builder()
                                .totalElements((long) resourcesList.size())
                                .pageSize(pageSize)
                                .totalPages(totalPages)
                                .build())
                .build());

    }
}
