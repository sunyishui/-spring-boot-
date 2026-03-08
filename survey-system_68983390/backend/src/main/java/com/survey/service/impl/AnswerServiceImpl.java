package com.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.survey.dto.AnswerItemDTO;
import com.survey.dto.AnswerSubmitDTO;
import com.survey.entity.*;
import com.survey.mapper.*;
import com.survey.service.AnswerService;
import com.survey.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerSheetMapper answerSheetMapper;
    private final AnswerMapper answerMapper;
    private final SurveyMapper surveyMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;

    public AnswerServiceImpl(AnswerSheetMapper answerSheetMapper, AnswerMapper answerMapper,
                             SurveyMapper surveyMapper, QuestionMapper questionMapper,
                             QuestionOptionMapper questionOptionMapper) {
        this.answerSheetMapper = answerSheetMapper;
        this.answerMapper = answerMapper;
        this.surveyMapper = surveyMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
    }

    @Override
    @Transactional
    public Result<?> submitAnswer(AnswerSubmitDTO dto, Long userId) {
        Survey survey = surveyMapper.selectById(dto.getSurveyId());
        if (survey == null || survey.getStatus() != 1) {
            return Result.error("问卷不存在或未发布");
        }
        // Check duplicate submission
        Long count = answerSheetMapper.selectCount(
                new LambdaQueryWrapper<AnswerSheet>()
                        .eq(AnswerSheet::getSurveyId, dto.getSurveyId())
                        .eq(AnswerSheet::getUserId, userId));
        if (count > 0) {
            return Result.error("您已经填写过该问卷");
        }

        AnswerSheet sheet = new AnswerSheet();
        sheet.setSurveyId(dto.getSurveyId());
        sheet.setUserId(userId);
        answerSheetMapper.insert(sheet);

        if (dto.getAnswers() != null) {
            for (AnswerItemDTO item : dto.getAnswers()) {
                Answer answer = new Answer();
                answer.setAnswerSheetId(sheet.getId());
                answer.setQuestionId(item.getQuestionId());
                if (item.getOptionIds() != null && !item.getOptionIds().isEmpty()) {
                    answer.setOptionIds(item.getOptionIds().stream()
                            .map(String::valueOf).collect(Collectors.joining(",")));
                }
                answer.setContent(item.getContent());
                answerMapper.insert(answer);
            }
        }
        return Result.success("提交成功", null);
    }

    @Override
    public IPage<AnswerSheet> listAnswerSheets(Long surveyId, int page, int size) {
        LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerSheet::getSurveyId, surveyId).orderByDesc(AnswerSheet::getSubmitTime);
        return answerSheetMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Result<List<Map<String, Object>>> getAnswerDetail(Long answerSheetId) {
        List<Answer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<Answer>().eq(Answer::getAnswerSheetId, answerSheetId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Answer a : answers) {
            Map<String, Object> map = new HashMap<>();
            Question q = questionMapper.selectById(a.getQuestionId());
            map.put("questionTitle", q != null ? q.getTitle() : "");
            map.put("questionType", q != null ? q.getType() : "");
            map.put("content", a.getContent());
            if (a.getOptionIds() != null && !a.getOptionIds().isEmpty()) {
                String[] ids = a.getOptionIds().split(",");
                List<String> optContents = new ArrayList<>();
                for (String idStr : ids) {
                    QuestionOption opt = questionOptionMapper.selectById(Long.parseLong(idStr.trim()));
                    if (opt != null) optContents.add(opt.getContent());
                }
                map.put("selectedOptions", optContents);
            }
            result.add(map);
        }
        return Result.success(result);
    }

    @Override
    public Result<?> deleteAnswerSheet(Long id) {
        answerMapper.delete(new LambdaQueryWrapper<Answer>().eq(Answer::getAnswerSheetId, id));
        answerSheetMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getStatistics(Long surveyId) {
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) return Result.error("问卷不存在");

        Map<String, Object> result = new HashMap<>();
        result.put("surveyTitle", survey.getTitle());

        Long totalCount = answerSheetMapper.selectCount(
                new LambdaQueryWrapper<AnswerSheet>().eq(AnswerSheet::getSurveyId, surveyId));
        result.put("totalAnswers", totalCount);

        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getSurveyId, surveyId)
                        .orderByAsc(Question::getSortOrder));

        List<Map<String, Object>> questionStats = new ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> qStat = new HashMap<>();
            qStat.put("id", q.getId());
            qStat.put("title", q.getTitle());
            qStat.put("type", q.getType());

            if ("INPUT".equals(q.getType())) {
                List<String> texts = answerMapper.getTextAnswers(q.getId());
                qStat.put("textAnswers", texts);
            } else {
                List<Map<String, Object>> optionCounts = answerMapper.countByOption(q.getId());
                qStat.put("optionStats", optionCounts);
            }
            questionStats.add(qStat);
        }
        result.put("questions", questionStats);
        return Result.success(result);
    }

    @Override
    public void exportExcel(Long surveyId, HttpServletResponse response) {
        Survey survey = surveyMapper.selectById(surveyId);
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getSurveyId, surveyId)
                        .orderByAsc(Question::getSortOrder));
        List<AnswerSheet> sheets = answerSheetMapper.selectList(
                new LambdaQueryWrapper<AnswerSheet>()
                        .eq(AnswerSheet::getSurveyId, surveyId)
                        .orderByAsc(AnswerSheet::getSubmitTime));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("问卷数据");
            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("序号");
            headerRow.createCell(1).setCellValue("提交时间");
            for (int i = 0; i < questions.size(); i++) {
                headerRow.createCell(i + 2).setCellValue(questions.get(i).getTitle());
            }
            // Data rows
            for (int r = 0; r < sheets.size(); r++) {
                Row row = sheet.createRow(r + 1);
                row.createCell(0).setCellValue(r + 1);
                row.createCell(1).setCellValue(sheets.get(r).getSubmitTime().toString());
                List<Answer> answers = answerMapper.selectList(
                        new LambdaQueryWrapper<Answer>()
                                .eq(Answer::getAnswerSheetId, sheets.get(r).getId()));
                Map<Long, Answer> ansMap = answers.stream()
                        .collect(Collectors.toMap(Answer::getQuestionId, a -> a, (a, b) -> a));
                for (int c = 0; c < questions.size(); c++) {
                    Answer ans = ansMap.get(questions.get(c).getId());
                    String cellValue = "";
                    if (ans != null) {
                        if (ans.getContent() != null && !ans.getContent().isEmpty()) {
                            cellValue = ans.getContent();
                        } else if (ans.getOptionIds() != null && !ans.getOptionIds().isEmpty()) {
                            String[] ids = ans.getOptionIds().split(",");
                            List<String> names = new ArrayList<>();
                            for (String id : ids) {
                                QuestionOption opt = questionOptionMapper.selectById(Long.parseLong(id.trim()));
                                if (opt != null) names.add(opt.getContent());
                            }
                            cellValue = String.join(", ", names);
                        }
                    }
                    row.createCell(c + 2).setCellValue(cellValue);
                }
            }

            String fileName = URLEncoder.encode(survey.getTitle() + "_数据.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<AnswerSheet> listMyAnswers(Long userId, int page, int size) {
        LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerSheet::getUserId, userId).orderByDesc(AnswerSheet::getSubmitTime);
        return answerSheetMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
