package org.soso.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.Version;

import lombok.Data;

@Data
@TableName("transactions")
public class Transaction {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long ledgerId;
    private Long categoryId;
    /** 金额，对应数据库 decimal(12,2) */
    private BigDecimal amount;
    /** 类型：INCOME / EXPENSE，与 Category.type 保持一致 */
    private String type;
    private LocalDateTime recordTime;
    private String remark;

    @TableField("request_id")
    private String requestId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Version
    private Integer version; // 数据库记得加一个 version 字段，默认值 1
}
