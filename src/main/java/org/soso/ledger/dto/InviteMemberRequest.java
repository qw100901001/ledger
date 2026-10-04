package org.soso.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteMemberRequest {
    @NotBlank(message = "被邀请人的用户名不能为空")
    private String username;
}