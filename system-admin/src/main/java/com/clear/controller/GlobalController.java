package com.clear.controller;

import com.clear.annotation.LogRecord;
import com.clear.common.R;
import com.clear.entity.GlobalCount;
import com.clear.service.PaperService;
import com.clear.service.QuestionService;
import com.clear.service.ShareService;
import com.clear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/global")
public class GlobalController {

    @Autowired
    private PaperService paperService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ShareService shareService;

    @GetMapping("/count")
    @LogRecord("获取全局统计信息")
    public R<GlobalCount> count() {
        GlobalCount globalCount = GlobalCount.builder()
                .userCount(userService.count())
                .paperCount(paperService.count())
                .questionCount(questionService.count())
                .shareCount(shareService.count()).build();

        return R.success(globalCount);
    }
}
