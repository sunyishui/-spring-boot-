package com.survey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SurveyDTO {
    private Long id;
    @NotBlank(message = "问卷标题不能为空")
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
