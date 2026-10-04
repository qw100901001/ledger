package org.soso.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ledgers")
public class Ledger {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long ownerId; // 创建人
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
