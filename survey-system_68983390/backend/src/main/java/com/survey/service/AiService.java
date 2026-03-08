package com.survey.service;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口 - 通义千问DashScope集成
 */
public interface AiService {

    /**
     * AI智能生成问卷
     * @param description 问卷描述，如"大学生消费习惯调查"
     * @return 生成的问卷结构 (title, description, questions)
     */
    Map<String, Object> generateSurvey(String description);

    /**
     * AI分析问卷数据，生成分析报告
     * @param surveyId 问卷ID
     * @return 分析报告文本
     */
    String analyzeReport(Long surveyId);

    /**
     * AI填空题答案摘要
     * @param questionId 题目ID
     * @return 摘要文本
     */
    String summarizeTextAnswers(Long questionId);
}
