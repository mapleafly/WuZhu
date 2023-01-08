package org.lifxue.wuzhu.dto;

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
public class Tag {
    private String slug;
    private String name;
    private String category;
}
