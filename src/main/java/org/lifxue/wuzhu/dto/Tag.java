package org.lifxue.wuzhu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @version 1.0
 * @classname Tag
 * @description
 * @auhthor lifxue
 * @date 2023/1/8 20:35
 */

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {
    private String slug;
    private String name;
    private String category;
}
