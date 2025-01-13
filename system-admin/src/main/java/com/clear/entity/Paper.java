package com.clear.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("paper")
public class Paper {
    /**
     * 唯一标识ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 试卷标题
     */
    private String title;
    /**
     * 试卷备注
     */
    private String remark;
    /**
     * 试卷解析
     */
    private String analysis;
    /**
     * 试卷描述
     */
    private String description;
    /**
     * 试卷状态
     * 0: 未发布, 1: 已发布
     */
    private Integer status;
    /**
     * 试卷总分
     */
    private Integer totalScore;
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
     * 删除标志
     * 0: 未删除, 1: 已删除
     */
    @TableLogic
    private Integer deleted;
    /**
     * 创建人ID
     */
    private Long creatorId;
    /**
     * 创建者
     */
    @TableField(exist = false)
    private String username;
    /**
     * 题目集合
     */
    @TableField(exist = false)
    private List<Question> questions;
} 