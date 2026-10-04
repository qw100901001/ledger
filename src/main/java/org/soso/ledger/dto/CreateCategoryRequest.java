package org.soso.ledger.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "分类名不能为空")
    @Size(max = 64, message = "分类名不能超过64字")
    private String name;

    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "income|expense", message = "类型只能是 income 或 expense")
    private String type;

    @Size(max = 255, message = "图标地址过长")
    private String icon;

    private Integer sortOrder;

    // getter / setter（或用 @Data）
}