package com.clear.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("share")
public class Share {
    /**
     * 唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 分享码
     */
    private String code;
    /**
     * 试卷ID
     */
    private Long paperId;
    /**
     * 状态
     * 0:禁用 1:启用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 过期时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date expireTime;
    /**
     * 创建人
     */
    private Long creatorId;
    /**
     * 关联试卷标题
     */
    @TableField(exist = false)
    private String paperTitle;
    /**
     * 创建者
     */
    @TableField(exist = false)
    private String username;
    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleted;
}
