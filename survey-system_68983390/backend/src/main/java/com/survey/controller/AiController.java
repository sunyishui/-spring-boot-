package com.survey.controller;

import com.survey.service.AiService;
import com.survey.util.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * AI智能生成问卷
     * @param body 包含 description 字段
     */
    @PostMapping("/generate-survey")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> generateSurvey(@RequestBody Map<String, String> body) {
        String description = body.get("description");
        if (description == null || description.isBlank()) {
            return Result.error("请输入问卷描述");
        }
        Map<String, Object> survey = aiService.generateSurvey(description);
        return Result.success("AI问卷生成成功", survey);
    }

    /**
     * AI分析问卷数据，生成分析报告
     */
    @GetMapping("/analyze/{surveyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> analyzeReport(@PathVariable Long surveyId) {
        String report = aiService.analyzeReport(surveyId);
        return Result.success("分析报告生成成功", report);
    }

    /**
     * AI填空题答案摘要
     */
    @GetMapping("/summarize/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> summarizeText(@PathVariable Long questionId) {
        String summary = aiService.summarizeTextAnswers(questionId);
        return Result.success("摘要生成成功", summary);
    }
}
