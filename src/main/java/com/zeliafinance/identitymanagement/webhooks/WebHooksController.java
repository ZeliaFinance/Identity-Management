package com.zeliafinance.identitymanagement.webhooks;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
public class WebHooksController {

    @PostMapping("/coreId")
    public void coreId(){

    }
}
