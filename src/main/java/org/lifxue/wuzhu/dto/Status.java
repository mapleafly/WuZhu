package org.lifxue.wuzhu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {
    private Date timestamp;
    @JsonProperty(value = "error_code")
    private int errorCode;
    @JsonProperty(value = "error_message")
    private String errorMessage;
    private int elapsed;
    @JsonProperty(value = "credit_count")
    private int creditCount;
    private String notice;
}
