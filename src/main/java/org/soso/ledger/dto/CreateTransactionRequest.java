    package org.soso.ledger.dto;

    import jakarta.validation.constraints.*;
    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import lombok.Data;

    @Data
    public class CreateTransactionRequest {
        @NotNull(message = "分类不能为空")
        private Long categoryId;

        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须大于0")
        private BigDecimal amount;

        @NotBlank(message = "类型不能为空")
        private String type;

        @NotNull(message = "记账时间不能为空")
        private LocalDateTime recordTime;

        @Size(max = 200, message = "备注不能超过200字")
        private String remark;

        @Size(max = 64, message = "requestId 长度不能超过64")
        private String requestId;
    }
