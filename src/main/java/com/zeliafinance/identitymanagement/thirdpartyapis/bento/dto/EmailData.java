package com.zeliafinance.identitymanagement.thirdpartyapis.bento.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmailData {
    private String to;
    private String from;
    private String subject;
    @JsonProperty("html_body")
    private String htmlBody;
    private boolean transactional;
    private PersonalizationData personalizations;
}
