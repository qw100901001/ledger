package org.soso.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;


@Data
@TableName("videos")
public class Video {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String originalName;
    private String storageName;
    private Long size;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

 }
