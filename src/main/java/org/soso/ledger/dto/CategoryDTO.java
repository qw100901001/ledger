package org.soso.ledger.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String type;
    private String icon;
    private Integer sortOrder;

    // getter / setter
}