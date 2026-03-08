package com.survey.dto;

import lombok.Data;

@Data
public class OptionDTO {
    private Long id;
    private String content;
    private Integer sortOrder = 0;
}
