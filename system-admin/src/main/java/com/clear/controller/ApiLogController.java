package com.clear.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clear.annotation.LogRecord;
import com.clear.common.R;
import com.clear.dto.ApiLogQueryDTO;
import com.clear.entity.ApiLog;
import com.clear.service.ApiLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/system/log")
public class ApiLogController {

    @Resource
    private ApiLogService apiLogService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")  // 只允许管理员查询日志
    @LogRecord("查询操作日志")
    public R<Page<ApiLog>> queryLogs(ApiLogQueryDTO queryDTO) {
        return R.success(apiLogService.queryLogs(queryDTO));
    }
} 