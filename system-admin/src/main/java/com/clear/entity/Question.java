package com.clear.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long paperId;
    
    private String content;
    
    private Integer type; // 1: 单选, 2: 多选, 3: 填空
    
    private String options; // JSON格式存储选项
    
    private String answer; // 正确答案，多选题用逗号分隔
    
    private Integer score;
    
    private Integer orderNum; // 题目顺序
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
} 