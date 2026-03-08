package com.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.entity.Answer;
import com.survey.entity.AnswerSheet;
import com.survey.entity.Question;
import com.survey.entity.QuestionOption;
import com.survey.entity.Survey;
import com.survey.mapper.*;
import com.survey.service.AiService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.dashscope.api-key}")
    private String apiKey;

    @Value("${ai.dashscope.model}")
    private String model;

    @Value("${ai.dashscope.base-url}")
    private String baseUrl;

    private final SurveyMapper surveyMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final AnswerSheetMapper answerSheetMapper;
    private final AnswerMapper answerMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public AiServiceImpl(SurveyMapper surveyMapper, QuestionMapper questionMapper,
                         QuestionOptionMapper questionOptionMapper, AnswerSheetMapper answerSheetMapper,
                         AnswerMapper answerMapper) {
        this.surveyMapper = surveyMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.answerSheetMapper = answerSheetMapper;
        this.answerMapper = answerMapper;
    }

    // ==================== 1. AI生成问卷 ====================
    @Override
    public Map<String, Object> generateSurvey(String description) {
        String prompt = """
                你是一个专业的问卷设计师。请根据以下描述生成一份完整的调查问卷。

                描述: %s

                请严格按照以下JSON格式返回（不要返回其他任何内容，只返回纯JSON）:
                {
                  "title": "问卷标题",
                  "description": "问卷说明",
                  "questions": [
                    {
                      "type": "RADIO",
                      "title": "题目标题",
                      "required": 1,
                      "options": [
                        {"content": "选项1"},
                        {"content": "选项2"}
                      ]
                    },
                    {
                      "type": "CHECKBOX",
                      "title": "多选题标题",
                      "required": 1,
                      "options": [
                        {"content": "选项1"},
                        {"content": "选项2"},
                        {"content": "选项3"}
                      ]
                    },
                    {
                      "type": "INPUT",
                      "title": "填空题标题",
                      "required": 0,
                      "options": []
                    }
                  ]
                }

                要求:
                1. 生成8-15道题目
                2. 题型要合理搭配，单选题、多选题和填空题都要有
                3. 单选题和多选题至少提供3个选项
                4. 题目表述清晰专业
                5. type只能是RADIO/CHECKBOX/INPUT三种
                """.formatted(description);

        String response = callDashScope(prompt);
        try {
            // Extract JSON from response (handle markdown code blocks)
            String json = extractJson(response);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("AI生成问卷解析失败，请重试: " + e.getMessage());
        }
    }

    // ==================== 2. AI分析报告 ====================
    @Override
    public String analyzeReport(Long surveyId) {
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) throw new RuntimeException("问卷不存在");

        // Gather statistics data
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getSurveyId, surveyId)
                        .orderByAsc(Question::getSortOrder));

        Long totalAnswers = answerSheetMapper.selectCount(
                new LambdaQueryWrapper<AnswerSheet>().eq(AnswerSheet::getSurveyId, surveyId));

        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("问卷标题: ").append(survey.getTitle()).append("\n");
        dataBuilder.append("问卷说明: ").append(survey.getDescription()).append("\n");
        dataBuilder.append("总参与人数: ").append(totalAnswers).append("\n\n");

        for (Question q : questions) {
            dataBuilder.append("题目: ").append(q.getTitle()).append(" (类型: ").append(q.getType()).append(")\n");
            if ("INPUT".equals(q.getType())) {
                List<String> texts = answerMapper.getTextAnswers(q.getId());
                dataBuilder.append("填空回答: ").append(String.join("; ", texts.subList(0, Math.min(texts.size(), 50)))).append("\n");
            } else {
                List<Map<String, Object>> optionCounts = answerMapper.countByOption(q.getId());
                for (Map<String, Object> oc : optionCounts) {
                    dataBuilder.append("  - ").append(oc.get("content")).append(": ").append(oc.get("count")).append("人\n");
                }
            }
            dataBuilder.append("\n");
        }

        String prompt = """
                你是一个专业的数据分析师。请根据以下问卷调查数据，生成一份详细的分析报告。

                %s

                请生成一份包含以下内容的分析报告（使用Markdown格式）:
                1. **概述**: 简要描述调查的基本情况
                2. **数据分析**: 对每道题目的数据进行详细分析，指出关键发现
                3. **趋势与特征**: 总结数据中的主要趋势和特征
                4. **问题与建议**: 指出数据中可能存在的问题，并给出改进建议
                5. **总结**: 给出总体结论

                报告要求:
                - 分析要有深度，不要只是复述数据
                - 使用百分比来描述选项占比
                - 注意发现不同题目之间的关联性
                - 语言专业简洁
                """.formatted(dataBuilder);

        return callDashScope(prompt);
    }

    // ==================== 3. 填空题摘要 ====================
    @Override
    public String summarizeTextAnswers(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) throw new RuntimeException("题目不存在");
        if (!"INPUT".equals(question.getType())) throw new RuntimeException("只有填空题支持摘要分析");

        List<String> texts = answerMapper.getTextAnswers(questionId);
        if (texts.isEmpty()) throw new RuntimeException("该题目暂无填写数据");

        // Limit to 100 answers to avoid token overflow
        List<String> sample = texts.subList(0, Math.min(texts.size(), 100));

        String prompt = """
                你是一个专业的文本分析师。以下是一道填空题的所有回答内容。

                题目: %s
                总回答数: %d

                回答内容:
                %s

                请对这些回答进行分析和摘要（使用Markdown格式）:
                1. **核心观点归纳**: 将回答归类为3-5个主要观点，说明每个观点的提及频率
                2. **高频关键词**: 列出出现频率最高的10个关键词
                3. **情感倾向**: 分析回答的整体情感倾向（正面/中性/负面）
                4. **典型回答**: 挑选3-5条最具代表性的回答
                5. **总结**: 一段话总结所有回答的主要内容

                分析要客观准确，不要主观臆断。
                """.formatted(question.getTitle(), texts.size(), String.join("\n", sample));

        return callDashScope(prompt);
    }

    // ==================== 通义千问API调用 ====================
    private String callDashScope(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "你是一个专业的助手，请认真完成用户的要求。"));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4096);

            String json = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "unknown error";
                    throw new RuntimeException("AI接口调用失败: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                return root.path("choices").path(0).path("message").path("content").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException("AI服务连接失败: " + e.getMessage());
        }
    }

    /**
     * 从AI返回的文本中提取JSON（处理markdown代码块包裹的情况）
     */
    private String extractJson(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();
        // Remove markdown code block markers
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        trimmed = trimmed.trim();
        // Find the JSON object
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
