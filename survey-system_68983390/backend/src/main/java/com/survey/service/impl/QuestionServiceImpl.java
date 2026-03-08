package com.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.survey.dto.OptionDTO;
import com.survey.dto.QuestionDTO;
import com.survey.entity.Question;
import com.survey.entity.QuestionOption;
import com.survey.mapper.QuestionMapper;
import com.survey.mapper.QuestionOptionMapper;
import com.survey.service.QuestionService;
import com.survey.util.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final QuestionOptionMapper questionOptionMapper;

    public QuestionServiceImpl(QuestionOptionMapper questionOptionMapper) {
        this.questionOptionMapper = questionOptionMapper;
    }

    @Override
    @Transactional
    public Result<?> addQuestion(QuestionDTO dto) {
        Question question = new Question();
        question.setSurveyId(dto.getSurveyId());
        question.setType(dto.getType());
        question.setTitle(dto.getTitle());
        question.setRequired(dto.getRequired());
        question.setSortOrder(dto.getSortOrder());
        this.save(question);

        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            for (OptionDTO optDTO : dto.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(question.getId());
                option.setContent(optDTO.getContent());
                option.setSortOrder(optDTO.getSortOrder());
                questionOptionMapper.insert(option);
            }
        }
        return Result.success("添加成功", question);
    }

    @Override
    @Transactional
    public Result<?> updateQuestion(QuestionDTO dto) {
        Question question = this.getById(dto.getId());
        if (question == null) {
            return Result.error("题目不存在");
        }
        question.setType(dto.getType());
        question.setTitle(dto.getTitle());
        question.setRequired(dto.getRequired());
        question.setSortOrder(dto.getSortOrder());
        this.updateById(question);

        // Delete old options and re-insert
        questionOptionMapper.delete(
                new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, dto.getId()));
        if (dto.getOptions() != null) {
            for (OptionDTO optDTO : dto.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(question.getId());
                option.setContent(optDTO.getContent());
                option.setSortOrder(optDTO.getSortOrder());
                questionOptionMapper.insert(option);
            }
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result<?> deleteQuestion(Long id) {
        questionOptionMapper.delete(
                new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, id));
        this.removeById(id);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<?> reorder(Long surveyId, List<Long> questionIds) {
        for (int i = 0; i < questionIds.size(); i++) {
            Question q = new Question();
            q.setId(questionIds.get(i));
            q.setSortOrder(i);
            this.updateById(q);
        }
        return Result.success();
    }
}
