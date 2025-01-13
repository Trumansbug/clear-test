package com.clear.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clear.dto.ApiLogQueryDTO;
import com.clear.entity.ApiLog;
import com.clear.mapper.ApiLogMapper;
import com.clear.service.ApiLogService;
import com.clear.util.TableNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ApiLogServiceImpl extends ServiceImpl<ApiLogMapper, ApiLog> implements ApiLogService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public Page<ApiLog> queryLogs(ApiLogQueryDTO queryDTO) {
        // 1. 获取需要查询的表名列表
        List<String> allTables = TableNameUtil.getTableNames(queryDTO.getStartTime(), queryDTO.getEndTime());
        
        // 2. 过滤出实际存在的表
        List<String> existingTables = TableNameUtil.filterExistingTables(jdbcTemplate, allTables);
        if (existingTables.isEmpty()) {
            return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        // 3. 构建基础查询条件
        String whereClause = TableNameUtil.generateConditions(
            queryDTO.getOperator(),
            queryDTO.getMethodDesc(),
            queryDTO.getStatus(),
            queryDTO.getStartTime(),
            queryDTO.getEndTime()
        );

        // 4. 构建分页查询
        try {
            return executePageQuery(existingTables, whereClause, queryDTO);
        } catch (Exception e) {
            log.error("分页查询失败，尝试逐表查询", e);
            return executeTableByTableQuery(existingTables, whereClause, queryDTO);
        }
    }

    /**
     * 使用UNION ALL方式执行分页查询
     */
    private Page<ApiLog> executePageQuery(List<String> tables, String whereClause, ApiLogQueryDTO queryDTO) {
        // 构建子查询
        List<String> subQueries = new ArrayList<>();
        for (String tableName : tables) {
            subQueries.add(String.format(
                "SELECT id, api_path, method_name, method_desc, request_params, response_data, " +
                "execution_time, ip_address, operator, status, error_message, create_time " +
                "FROM %s WHERE %s", 
                tableName, whereClause
            ));
        }

        // 构建完整的分页查询SQL
        String sql = String.format(
            "WITH ordered_logs AS (" +
            "SELECT *, ROW_NUMBER() OVER (ORDER BY create_time DESC) AS rn " +
            "FROM (%s) AS combined " +
            ") " +
            "SELECT * FROM ordered_logs WHERE rn BETWEEN ? AND ?",
            String.join(" UNION ALL ", subQueries)
        );

        // 计算总记录数
        String countSql = "SELECT COUNT(*) FROM (" + String.join(" UNION ALL ", subQueries) + ") t";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        if (total == null || total == 0) {
            return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        // 执行分页查询
        int start = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize() + 1;
        int end = start + queryDTO.getPageSize() - 1;

        List<ApiLog> records = jdbcTemplate.query(
            sql,
            new BeanPropertyRowMapper<>(ApiLog.class),
            start, end
        );

        // 构建分页结果
        Page<ApiLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /**
     * 逐表查询方式（备选方案）
     */
    private Page<ApiLog> executeTableByTableQuery(List<String> tables, String whereClause, ApiLogQueryDTO queryDTO) {
        List<ApiLog> allRecords = new ArrayList<>();
        long total = 0;

        // 逐表查询
        for (String tableName : tables) {
            try {
                // 查询当前表的记录数
                String countSql = String.format("SELECT COUNT(*) FROM %s WHERE %s", tableName, whereClause);
                Long tableTotal = jdbcTemplate.queryForObject(countSql, Long.class);
                if (tableTotal == null || tableTotal == 0) {
                    continue;
                }
                total += tableTotal;

                // 查询当前表的数据
                String querySql = String.format(
                    "SELECT id, api_path, method_name, method_desc, request_params, response_data, " +
                    "execution_time, ip_address, operator, status, error_message, create_time " +
                    "FROM %s WHERE %s ORDER BY create_time DESC",
                    tableName, whereClause
                );
                List<ApiLog> tableRecords = jdbcTemplate.query(
                    querySql,
                    new BeanPropertyRowMapper<>(ApiLog.class)
                );
                allRecords.addAll(tableRecords);
            } catch (Exception e) {
                log.error("查询表 {} 失败，继续查询下一个表", tableName, e);
            }
        }

        // 在内存中排序
        Collections.sort(allRecords, (a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        // 在内存中分页
        int fromIndex = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        int toIndex = Math.min(fromIndex + queryDTO.getPageSize(), allRecords.size());

        // 构建分页结果
        Page<ApiLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page.setTotal(total);
        if (fromIndex < allRecords.size()) {
            page.setRecords(allRecords.subList(fromIndex, toIndex));
        } else {
            page.setRecords(new ArrayList<>());
        }

        return page;
    }
} 