package com.clear.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ApiLogTableNameHandler implements TableNameHandler {
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public String dynamicTableName(String sql, String tableName) {
        if ("api_log".equals(tableName)) {
            String targetTable = tableName + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM"));
            createTableIfNotExists(targetTable);
            return targetTable;
        }
        return tableName;
    }
    
    private void createTableIfNotExists(String tableName) {
        String createTableSql = String.format(
            "CREATE TABLE IF NOT EXISTS %s LIKE api_log_template", tableName);
        jdbcTemplate.execute(createTableSql);
    }
} 