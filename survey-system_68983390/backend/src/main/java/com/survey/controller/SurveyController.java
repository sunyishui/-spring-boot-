package com.survey.controller;

import com.survey.dto.SurveyDTO;
import com.survey.security.SecurityUtils;
import com.survey.service.SurveyService;
import com.survey.util.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> create(@Valid @RequestBody SurveyDTO dto) {
        return surveyService.createSurvey(dto, SecurityUtils.getCurrentUserId());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@Valid @RequestBody SurveyDTO dto) {
        return surveyService.updateSurvey(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        return surveyService.deleteSurvey(id);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> publish(@PathVariable Long id) {
        return surveyService.publishSurvey(id);
    }

    @PutMapping("/{id}/end")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> end(@PathVariable Long id) {
        return surveyService.endSurvey(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer status) {
        return Result.success(surveyService.listSurveys(page, size, keyword, status));
    }

    @GetMapping("/published")
    public Result<?> listPublished(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String keyword) {
        return Result.success(surveyService.listPublishedSurveys(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return surveyService.getSurveyDetail(id);
    }
}
