package com.survey.controller;

import com.survey.dto.AnswerSubmitDTO;
import com.survey.security.SecurityUtils;
import com.survey.service.AnswerService;
import com.survey.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("/submit")
    public Result<?> submit(@Valid @RequestBody AnswerSubmitDTO dto) {
        return answerService.submitAnswer(dto, SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/sheets/{surveyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> listSheets(@PathVariable Long surveyId,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return Result.success(answerService.listAnswerSheets(surveyId, page, size));
    }

    @GetMapping("/detail/{answerSheetId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> detail(@PathVariable Long answerSheetId) {
        return answerService.getAnswerDetail(answerSheetId);
    }

    @DeleteMapping("/sheets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteSheet(@PathVariable Long id) {
        return answerService.deleteAnswerSheet(id);
    }

    @GetMapping("/statistics/{surveyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> statistics(@PathVariable Long surveyId) {
        return answerService.getStatistics(surveyId);
    }

    @GetMapping("/export/{surveyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void export(@PathVariable Long surveyId, HttpServletResponse response) {
        answerService.exportExcel(surveyId, response);
    }

    @GetMapping("/my")
    public Result<?> myAnswers(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        return Result.success(answerService.listMyAnswers(SecurityUtils.getCurrentUserId(), page, size));
    }
}
