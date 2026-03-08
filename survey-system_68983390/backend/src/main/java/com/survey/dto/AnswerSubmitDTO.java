package com.survey.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AnswerSubmitDTO {
    @NotNull(message = "问卷ID不能为空")
    private Long surveyId;
    private List<AnswerItemDTO> answers;
}
