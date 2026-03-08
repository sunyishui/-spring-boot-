package com.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.survey.dto.QuestionDTO;
import com.survey.entity.Question;
import com.survey.util.Result;

public interface QuestionService extends IService<Question> {
    Result<?> addQuestion(QuestionDTO dto);
    Result<?> updateQuestion(QuestionDTO dto);
    Result<?> deleteQuestion(Long id);
    Result<?> reorder(Long surveyId, java.util.List<Long> questionIds);
}
