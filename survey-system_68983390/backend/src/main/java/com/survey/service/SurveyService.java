package com.survey.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.survey.dto.SurveyDTO;
import com.survey.entity.Survey;
import com.survey.util.Result;

import java.util.Map;

public interface SurveyService extends IService<Survey> {
    Result<?> createSurvey(SurveyDTO dto, Long userId);
    Result<?> updateSurvey(SurveyDTO dto);
    Result<?> deleteSurvey(Long id);
    Result<?> publishSurvey(Long id);
    Result<?> endSurvey(Long id);
    IPage<Survey> listSurveys(int page, int size, String keyword, Integer status);
    IPage<Survey> listPublishedSurveys(int page, int size, String keyword);
    Result<Map<String, Object>> getSurveyDetail(Long id);
}
