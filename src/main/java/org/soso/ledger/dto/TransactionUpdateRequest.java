package org.soso.ledger.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class TransactionUpdateRequest {


    @NotNull(message = "分类ID不能为空")
    private Long categoryId;


    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;


    @NotBlank(message = "类型不能为空")
    private String type;

    @Size(max = 200, message = "备注不能超过200字")
    private String remark;


    @NotNull(message = "记账时间不能为空")
    private LocalDateTime recordTime;
}
