package org.soso.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import lombok.Data;

@TableName("categories")
@Data
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属账本 ID —— 校验“分类是否属于该账本”时要用 */
    private Long ledgerId;

    /** 分类名，如“工资”“餐饮” */
    private String name;

    /**
     * 类型：INCOME（收入）/ EXPENSE（支出）
     * 与 Transaction.type 保持一致，createTransaction 里要比对
     */
    private String type;
    private String icon;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
