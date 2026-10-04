CREATE TABLE `videos` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '上传用户的ID',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `storage_name` VARCHAR(255) NOT NULL COMMENT '磁盘上的真实文件名(如UUID.mp4)',
  `size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频信息表';