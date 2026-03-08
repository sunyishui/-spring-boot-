package com.survey.controller;

import com.survey.dto.QuestionDTO;
import com.survey.service.QuestionService;
import com.survey.util.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> add(@Valid @RequestBody QuestionDTO dto) {
        return questionService.addQuestion(dto);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@Valid @RequestBody QuestionDTO dto) {
        return questionService.updateQuestion(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        return questionService.deleteQuestion(id);
    }

    @PutMapping("/reorder/{surveyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> reorder(@PathVariable Long surveyId, @RequestBody List<Long> questionIds) {
        return questionService.reorder(surveyId, questionIds);
    }
}
