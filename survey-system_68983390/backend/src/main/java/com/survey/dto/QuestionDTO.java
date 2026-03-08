package com.survey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    @NotNull(message = "问卷ID不能为空")
    private Long surveyId;
    @NotBlank(message = "题目类型不能为空")
    private String type;
    @NotBlank(message = "题目标题不能为空")
    private String title;
    private Integer required = 1;
    private Integer sortOrder = 0;
    private List<OptionDTO> options;
}
