package com.clear.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName(value = "api_log", keepGlobalPrefix = true)
public class ApiLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String apiPath;        // 接口路径
    private String methodName;     // 方法名
    private String methodDesc;     // 方法描述
    private String requestParams;  // 请求参数
    private String responseData;   // 响应数据
    private Long executionTime;    // 执行时间(ms)
    private String ipAddress;      // 访问IP
    private String operator;       // 操作人
    private Integer status;       // 请求是否成功
    private String errorMessage;   // 错误信息

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;       // 创建时间
} 