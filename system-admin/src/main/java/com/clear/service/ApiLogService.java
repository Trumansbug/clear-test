package com.clear.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clear.dto.ApiLogQueryDTO;
import com.clear.entity.ApiLog;

public interface ApiLogService {
    /**
     * 分页查询日志
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<ApiLog> queryLogs(ApiLogQueryDTO queryDTO);
} 