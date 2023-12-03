package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {

    private long id;
    private String first_name;
    private String last_name;
    private String email;
    private String customer_code;
    private String phone;
    private String metadata;
    private String risk_action;
    private String internation_format_phone;

//        "customer": {
//            "id": 14571,
//                    "first_name": null,
//                    "last_name": null,
//                    "email": "test@email.co",
//                    "customer_code": "CUS_hns72vhhtos0f0k",
//                    "phone": null,
//                    "metadata": null,
//                    "risk_action": "default"
//        },
//        "plan": null
}
