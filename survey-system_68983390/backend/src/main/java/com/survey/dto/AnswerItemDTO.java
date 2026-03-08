package com.survey.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnswerItemDTO {
    private Long questionId;
    private List<Long> optionIds;
    private String content;
}
