package com.clear.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

@Data
public class ApiLogQueryDTO {
    private String operator;           // 操作人
    private String methodDesc;         // 方法描述
    private Integer status;           // 请求状态
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;            // 开始时间
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;              // 结束时间
    
    private Integer pageNum = 1;       // 页码
    private Integer pageSize = 10;     // 每页大小
} 