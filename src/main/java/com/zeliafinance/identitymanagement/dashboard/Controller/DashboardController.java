package com.zeliafinance.identitymanagement.dashboard.Controller;

import com.zeliafinance.identitymanagement.dashboard.dto.DashboardResponse;
import com.zeliafinance.identitymanagement.dashboard.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> dashboardData(@RequestParam(required = false) Integer year,
                                                           @RequestParam(required = false) Integer month,
                                                           @RequestParam(required = false) Integer day,
                                                           @RequestParam(required = false) Integer hour,
                                                           @RequestParam(required = false) String startDate,
                                                           @RequestParam(required = false) String endDate){
        return dashboardService.dashboardData(year, month, day, hour, startDate, endDate);
    }
}
