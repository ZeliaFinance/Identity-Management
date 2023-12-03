package com.zeliafinance.identitymanagement.thirdpartyapis.bani.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCustomerDto {
    private String customer_first_name;
    private String customer_last_name;
    private String customer_phone;
    private String customer_email;
    private  String customer_address;
    private String customer_state;
    private String customer_city;
    private String customer_note;
}
