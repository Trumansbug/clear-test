package com.clear.util;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TableNameUtil {
    
    private static final DateTimeFormatter TABLE_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM");
    
    /**
     * 获取日期范围内的所有表名
     */
    public static List<String> getTableNames(Date startTime, Date endTime) {
        List<String> tableNames = new ArrayList<>();
        
        // 如果没有时间范围，默认查询最近三天
        if (startTime == null && endTime == null) {
            startTime = Date.from(LocalDateTime.now().minusDays(3)
                .atZone(ZoneId.systemDefault()).toInstant());
            endTime = new Date();
        }
        
        LocalDateTime start = startTime == null ? 
            LocalDateTime.now().minusDays(3) :
            LocalDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault());
            
        LocalDateTime end = endTime == null ?
            LocalDateTime.now() :
            LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault());
            
        // 获取开始时间所在月份的第一天
        LocalDateTime current = start.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // 遍历所有月份
        while (!current.isAfter(end)) {
            tableNames.add("api_log_" + current.format(TABLE_SUFFIX_FORMATTER));
            current = current.plusMonths(1);
        }
        
        return tableNames;
    }
    
    /**
     * 检查表是否存在并返回存在的表名列表
     */
    public static List<String> filterExistingTables(JdbcTemplate jdbcTemplate, List<String> tableNames) {
        return tableNames.stream()
            .filter(tableName -> isTableExists(jdbcTemplate, tableName))
            .collect(Collectors.toList());
    }
    
    /**
     * 检查单个表是否存在
     */
    private static boolean isTableExists(JdbcTemplate jdbcTemplate, String tableName) {
        try {
            // 使用 information_schema 查询表是否存在
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = (SELECT DATABASE()) " +
                        "AND table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 生成跨表查询的SQL
     */
    public static String generateUnionQuery(List<String> tableNames, String conditions) {
        if (tableNames.isEmpty()) {
            return "";
        }
        
        return tableNames.stream()
            .map(tableName -> String.format("SELECT * FROM %s WHERE %s", tableName, conditions))
            .collect(Collectors.joining(" UNION ALL "));
    }
    
    /**
     * 生成查询条件
     */
    public static String generateConditions(String operator, String methodDesc, Integer status,
            Date startTime, Date endTime) {
        List<String> conditions = new ArrayList<>();
        
        if (operator != null && !operator.trim().isEmpty()) {
            conditions.add("operator LIKE '%" + operator + "%'");
        }
        
        if (methodDesc != null && !methodDesc.trim().isEmpty()) {
            conditions.add("method_desc LIKE '%" + methodDesc + "%'");
        }
        
        if (status != null) {
            conditions.add("status = " + status);
        }
        
        if (startTime != null) {
            conditions.add("create_time >= '" + new java.sql.Timestamp(startTime.getTime()) + "'");
        }
        
        if (endTime != null) {
            conditions.add("create_time <= '" + new java.sql.Timestamp(endTime.getTime()) + "'");
        }
        
        return conditions.isEmpty() ? "1=1" : String.join(" AND ", conditions);
    }
} 