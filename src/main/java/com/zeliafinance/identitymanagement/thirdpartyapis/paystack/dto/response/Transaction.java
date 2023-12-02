package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    private Long id;
    private String domain;
    private String reference;
    private double amount;
    private String paid_at;
    private String channel;
    private String currency;
    private Authorisation authorization;
    private Customer customer;
    private Object plan;
    private Object subaccount;
    private Object split;
    private Object split_id;
    private String paidAt;
    private Object pos_transaction_data;
    private Object source;
    private Object fees_breakdown;
}
