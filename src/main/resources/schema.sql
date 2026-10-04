-- 1.用户表(User)
CREATE TABLE `users`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `username`      VARCHAR(64)  NOT NULL COMMENT '登录名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `nickname`      VARCHAR(64)           DEFAULT NULL COMMENT '用户昵称',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 账本表 (Ledger)
CREATE TABLE `ledgers`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '账本主键',
    `name`       VARCHAR(64) NOT NULL COMMENT '账本名称（如：家庭开支）',
    `owner_id`   BIGINT UNSIGNED NOT NULL COMMENT '创建者/所有者ID',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY          `idx_owner_id` (`owner_id`),
    CONSTRAINT `fk_ledgers_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本表';
-- 3. 账本成员表 (Ledger Members) - 解决家庭共享与权限问题
CREATE TABLE `ledger_members` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ledger_id` BIGINT UNSIGNED NOT NULL COMMENT '账本ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '成员用户ID',
  `role` VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '角色（owner/admin/member）',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ledger_user` (`ledger_id`, `user_id`), -- 落实拍板：防止同一用户重复加入同一账本
  CONSTRAINT `fk_members_ledger` FOREIGN KEY (`ledger_id`) REFERENCES `ledgers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本成员表';

-- 4. 分类表 (Category) - 分类挂在账本下，保证共享账本时分类统一
CREATE TABLE `categories` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类主键',
  `ledger_id` BIGINT UNSIGNED NOT NULL COMMENT '所属账本ID',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名（如：餐饮、给娃买奶粉）',
  `type` TINYINT NOT NULL COMMENT '类型：1-收入，2-支出',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ledger_name_type` (`ledger_id`, `name`, `type`), -- 同账本下同名同类型分类不能重复
  CONSTRAINT `fk_categories_ledger` FOREIGN KEY (`ledger_id`) REFERENCES `ledgers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义分类表';

-- 5. 账目记录表 (Transaction) - 一笔账至少3个外键
CREATE TABLE `transactions`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水主键',
    `ledger_id`   BIGINT UNSIGNED NOT NULL COMMENT '所属账本ID（外键1）',
    `category_id` BIGINT UNSIGNED NOT NULL COMMENT '所属分类ID（外键2）',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '记账人/记录人ID（外键3）',
    `amount`      DECIMAL(12, 2) NOT NULL COMMENT '交易金额（落实拍板：decimal(12,2)）',
    `type`        VARCHAR(20)        NOT NULL COMMENT '类型：1-收入，2-支出（冗余字段，方便快速统计）',
    `record_time` DATETIME       NOT NULL COMMENT '消费发生时间（钱哪天花的）',
    `remark`      VARCHAR(255)            DEFAULT NULL COMMENT '备注（如：早饭）',
    `request_id`      VARCHAR(255)            DEFAULT NULL COMMENT '幂等唯一id',
    `created_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录录入时间',
    `updated_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (`id`),
    -- 索引优化：账本查流水、按时间排序是最高频操作
    KEY           `idx_ledger_record_time` (`ledger_id`, `record_time`),
    KEY           `idx_category_id` (`category_id`),
    KEY           `idx_user_id` (`user_id`),
    CONSTRAINT `fk_trans_ledger` FOREIGN KEY (`ledger_id`) REFERENCES `ledgers` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_trans_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_trans_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账目记录表';
