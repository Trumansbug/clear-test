-- 创建用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `status` tinyint(4) DEFAULT 1 COMMENT '状态 0：禁用，1：正常',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(50) NOT NULL COMMENT '角色名称',
    `code` varchar(50) NOT NULL COMMENT '角色编码',
    `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `role_id` bigint(20) NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 创建试卷表
CREATE TABLE IF NOT EXISTS `paper` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(200) NOT NULL COMMENT '试卷标题',
    `description` text DEFAULT NULL COMMENT '试卷描述',
    `status` tinyint(4) DEFAULT 0 COMMENT '状态 0：未发布，1：已发布',
    `total_score` int(11) DEFAULT 0 COMMENT '总分',
    `creator_id` bigint(20) NOT NULL COMMENT '创建者ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 创建题目表
CREATE TABLE IF NOT EXISTS `question` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `paper_id` bigint(20) NOT NULL COMMENT '试卷ID',
    `content` text NOT NULL COMMENT '题目内容',
    `type` tinyint(4) NOT NULL COMMENT '题目类型 1：单选，2：多选，3：填空',
    `options` text DEFAULT NULL COMMENT '选项，JSON格式',
    `answer` text NOT NULL COMMENT '正确答案',
    `score` int(11) NOT NULL COMMENT '分值',
    `order_num` int(11) DEFAULT 0 COMMENT '题目顺序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 插入默认角色
INSERT INTO `sys_role` (`name`, `code`, `description`) VALUES
('管理员', 'ROLE_ADMIN', '系统管理员'),
('普通用户', 'ROLE_USER', '普通用户');

-- 插入默认管理员账号（密码：admin123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$X/uMNuiis.fyO47cxbta3.zqYzMzVZZF5lGCGzPGhwB7P0C6dMOYi', '管理员', 1);

-- 给默认管理员分配管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT 
    (SELECT id FROM sys_user WHERE username = 'admin'),
    (SELECT id FROM sys_role WHERE code = 'ROLE_ADMIN'); 

-- 创建初始表结构
CREATE TABLE IF NOT EXISTS api_log_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_path VARCHAR(255),
    method_name VARCHAR(100),
    request_params TEXT,
    response_data TEXT,
    execution_time BIGINT,
    ip_address VARCHAR(50),
    create_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 