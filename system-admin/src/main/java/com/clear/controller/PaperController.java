package com.clear.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clear.annotation.LogRecord;
import com.clear.common.R;
import com.clear.entity.Paper;
import com.clear.entity.Question;
import com.clear.model.request.PageRequest;
import com.clear.service.PaperService;
import com.clear.service.QuestionService;
import com.clear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paper")
public class PaperController {

    @Autowired
    private PaperService paperService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @GetMapping("list")
    @LogRecord("获取试卷列表")
    public R<IPage<Paper>> list(PageRequest pageRequest) {
        Page<Paper> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        return R.success(paperService.getPaperList(page));
    }

    @GetMapping("/{id}")
    @LogRecord("获取试卷详情")
    public R<Paper> getById(@PathVariable Long id) {
        return R.success(paperService.getById(id));
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    @LogRecord("添加试卷")
    public R<Paper> add(@RequestBody Paper paper) {
        paperService.savePaper(paper);
        return R.success(paper);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogRecord("修改试卷")
    public R<Void> update(@PathVariable Long id, @RequestBody Paper paper) {
        paperService.checkPaperCanEdit(id);
        paper.setId(id);
        paperService.updateById(paper);
        return R.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogRecord("删除试卷")
    public R<Void> delete(@PathVariable Long id) {
        paperService.checkPaperCanEdit(id);
        paperService.removeById(id);
        return R.success();
    }

    @PostMapping("/batchDelete")
    @PreAuthorize("hasRole('ADMIN')")
    @LogRecord("批量删除试卷")
    public R<Void> batchDelete(@RequestBody Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                paperService.checkPaperCanEdit(id);
                paperService.removeById(id);
            }
        }

        return R.success();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    @LogRecord("发布试卷")
    public R<Void> publish(@PathVariable Long id) {
        paperService.publishPaper(id);
        return R.success();
    }

    @GetMapping("/{paperId}/questions/list")
    @PreAuthorize("hasRole('ADMIN')")
    @LogRecord("获取试卷试题列表")
    public R<List<Question>> getQuestions(@PathVariable Long paperId) {
        return R.success(questionService.getQuestionsByPaperId(paperId));
    }

    @GetMapping("/getPaperByShareCode/{shareCode}")
    @LogRecord("获取试卷分享链接")
    public R<Paper> getPaperByShareCode(@PathVariable String shareCode) {
        Paper paper = paperService.getPaperByShareCode(shareCode);
        return R.success(paper);
    }

} 