package com.clear.controller;

import com.clear.annotation.LogRecord;
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
    @LogRecord("获取题目列表")
    public R<List<Question>> list(@PathVariable Long paperId) {
        return R.success(questionService.getQuestionsByPaperId(paperId));
    }

    @PostMapping("/add")
    @LogRecord("添加题目")
    public R<Void> add(@RequestBody Question question) {
        questionService.addQuestion(question);
        return R.success();
    }

    @PostMapping("/edit")
    @LogRecord("修改题目")
    public R<Void> editQuestions(@RequestBody Question question) {
        questionService.updateQuestion(question);
        return R.success();
    }

    @DeleteMapping("/delete/{id}")
    @LogRecord("删除题目")
    public R<List<Question>> delete(@PathVariable() Long id) {
        questionService.deleteQuestion(id);
        return R.success();
    }
} 