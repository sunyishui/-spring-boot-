package com.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.survey.dto.SurveyDTO;
import com.survey.entity.Question;
import com.survey.entity.QuestionOption;
import com.survey.entity.Survey;
import com.survey.mapper.QuestionMapper;
import com.survey.mapper.QuestionOptionMapper;
import com.survey.mapper.SurveyMapper;
import com.survey.service.SurveyService;
import com.survey.util.Result;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SurveyServiceImpl extends ServiceImpl<SurveyMapper, Survey> implements SurveyService {

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;

    public SurveyServiceImpl(QuestionMapper questionMapper, QuestionOptionMapper questionOptionMapper) {
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
    }

    @Override
    public Result<?> createSurvey(SurveyDTO dto, Long userId) {
        Survey survey = new Survey();
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setStartTime(dto.getStartTime());
        survey.setEndTime(dto.getEndTime());
        survey.setCreateBy(userId);
        survey.setStatus(0);
        this.save(survey);
        return Result.success("创建成功", survey);
    }

    @Override
    public Result<?> updateSurvey(SurveyDTO dto) {
        Survey survey = this.getById(dto.getId());
        if (survey == null) {
            return Result.error("问卷不存在");
        }
        if (survey.getStatus() == 1) {
            return Result.error("已发布的问卷不能修改");
        }
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setStartTime(dto.getStartTime());
        survey.setEndTime(dto.getEndTime());
        this.updateById(survey);
        return Result.success();
    }

    @Override
    public Result<?> deleteSurvey(Long id) {
        this.removeById(id);
        return Result.success();
    }

    @Override
    public Result<?> publishSurvey(Long id) {
        Survey survey = this.getById(id);
        if (survey == null) {
            return Result.error("问卷不存在");
        }
        long questionCount = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getSurveyId, id));
        if (questionCount == 0) {
            return Result.error("问卷没有题目，不能发布");
        }
        survey.setStatus(1);
        this.updateById(survey);
        return Result.success("发布成功", null);
    }

    @Override
    public Result<?> endSurvey(Long id) {
        Survey survey = this.getById(id);
        if (survey == null) {
            return Result.error("问卷不存在");
        }
        survey.setStatus(2);
        this.updateById(survey);
        return Result.success();
    }

    @Override
    public IPage<Survey> listSurveys(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Survey::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Survey::getStatus, status);
        }
        wrapper.orderByDesc(Survey::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<Survey> listPublishedSurveys(int page, int size, String keyword) {
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Survey::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Survey::getTitle, keyword);
        }
        wrapper.orderByDesc(Survey::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public Result<Map<String, Object>> getSurveyDetail(Long id) {
        Survey survey = this.getById(id);
        if (survey == null) {
            return Result.error("问卷不存在");
        }
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getSurveyId, id)
                        .orderByAsc(Question::getSortOrder));

        List<Map<String, Object>> questionList = questions.stream().map(q -> {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("id", q.getId());
            qMap.put("type", q.getType());
            qMap.put("title", q.getTitle());
            qMap.put("required", q.getRequired());
            qMap.put("sortOrder", q.getSortOrder());
            if (!"INPUT".equals(q.getType())) {
                List<QuestionOption> options = questionOptionMapper.selectList(
                        new LambdaQueryWrapper<QuestionOption>()
                                .eq(QuestionOption::getQuestionId, q.getId())
                                .orderByAsc(QuestionOption::getSortOrder));
                qMap.put("options", options);
            }
            return qMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("survey", survey);
        result.put("questions", questionList);
        return Result.success(result);
    }
}
