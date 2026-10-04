package org.soso.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CreateLedgerRequest {
    @NotBlank(message = "账本名称不能为空")
    private String name;
}
