package org.soso.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ledger_members")
public class LedgerMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ledgerId;
    private Long userId;
    private String role; // owner / member
}
