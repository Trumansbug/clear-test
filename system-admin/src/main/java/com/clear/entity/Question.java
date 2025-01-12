package com.clear.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("question")
public class Question {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 试卷id
     */
    private Long paperId;
    /**
     * 题目内容
     */
    private String content;
    /**
     * 题目类型
     * 1: 单选, 2: 多选, 3: 填空
     */
    private Integer type;
    /**
     * 选项，JSON格式存储
     */
    private String options;
    /**
     * 正确答案，多选题用逗号分隔
     */
    private String answer;
    /**
     * 分数
     */
    private Integer score;
    /**
     * 题目顺序
     */
    private Integer orderNum;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 删除标识
     * 0: 未删除, 1: 已删除
     */
    @TableLogic
    private Integer deleted;
} 