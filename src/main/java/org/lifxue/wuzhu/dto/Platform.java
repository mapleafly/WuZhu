package org.lifxue.wuzhu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Platform {
    private Integer id;
    private String name;
    private String symbol;
    private String slug;
    private String token_address;
}
