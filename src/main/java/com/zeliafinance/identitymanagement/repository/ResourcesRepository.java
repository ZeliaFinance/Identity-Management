package com.zeliafinance.identitymanagement.repository;

import com.zeliafinance.identitymanagement.entity.Resources;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourcesRepository extends JpaRepository<Resources, Long> {
    Resources findByLookupCode(String lookupCode);

}
