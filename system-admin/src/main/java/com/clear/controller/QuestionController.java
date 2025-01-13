package com.clear.controller;

import com.clear.common.R;
import com.clear.entity.Question;
import com.clear.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/list/{paperId}")
    public R<List<Question>> list(@PathVariable Long paperId) {
        return R.success(questionService.getQuestionsByPaperId(paperId));
    }

    @PostMapping("/add")
    public R<Void> add(@RequestBody Question question) {
        questionService.addQuestion(question);
        return R.success();
    }

    @PostMapping("/edit")
    public R<Void> editQuestions(@RequestBody Question question) {
        questionService.updateQuestion(question);
        return R.success();
    }

    @DeleteMapping("/delete/{id}")
    public R<List<Question>> delete(@PathVariable() Long id) {
        questionService.deleteQuestion(id);
        return R.success();
    }
} 