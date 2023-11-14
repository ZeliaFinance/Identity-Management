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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(modelMapper.map(savedResource, ResourcesDto.class))
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchAllResources(int pageNo, int pageSize) {
        List<Resources> resourcesList = resourcesRepository.findAll();
        List<ResourcesDto> resourcesDtos = resourcesList.stream()
                .map(resource -> modelMapper.map(resource, ResourcesDto.class))
                .skip(pageNo-1).limit(pageSize)
                .sorted(Comparator.comparing(ResourcesDto::getLookupCode))
                .toList();
        Map<String, List<ResourcesDto>> resourcesMap = resourcesDtos.stream().collect(Collectors.groupingBy(ResourcesDto::getLookupCode));
        int totalPages;
        if (resourcesList.size() <= 100){
            totalPages =1;
        } else {
            totalPages = resourcesList.size() / 100;
        }
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(resourcesMap)
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
        if (resource.isEmpty()){
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
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
                        .statusCode(HttpStatus.OK.value())
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(resourcesList)
                        .info(Info.builder()
                                .totalElements((long) resourcesList.size())
                                .pageSize(pageSize)
                                .totalPages(totalPages)
                                .build())
                .build());
    }

    @Override
    public ResponseEntity<CustomResponse> fetchDistinctLookupCodes(int pageNo, int pageSize) {
        //list findBy lookupValues, list of Strings, distinct
        List<String> lookupCodes = resourcesRepository.findAll()
                .stream().map(Resources::getLookupCode)
                .distinct()
                .skip(pageNo - 1)
                .limit(pageSize)
                .toList();
        return ResponseEntity.ok(CustomResponse.builder()
                        .statusCode(200)
                        .responseMessage(AccountUtils.SUCCESS_MESSAGE)
                        .responseBody(lookupCodes)
                .build());

    }
}
