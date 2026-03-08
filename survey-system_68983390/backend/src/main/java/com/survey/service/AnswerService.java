package com.survey.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.survey.dto.AnswerSubmitDTO;
import com.survey.entity.AnswerSheet;
import com.survey.util.Result;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface AnswerService {
    Result<?> submitAnswer(AnswerSubmitDTO dto, Long userId);
    IPage<AnswerSheet> listAnswerSheets(Long surveyId, int page, int size);
    Result<List<Map<String, Object>>> getAnswerDetail(Long answerSheetId);
    Result<?> deleteAnswerSheet(Long id);
    Result<Map<String, Object>> getStatistics(Long surveyId);
    void exportExcel(Long surveyId, HttpServletResponse response);
    IPage<AnswerSheet> listMyAnswers(Long userId, int page, int size);
}
