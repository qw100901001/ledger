package org.soso.ledger.dto;

import lombok.Data;

@Data
public class VideoDTO {
    private Long id;
    private String originalName;
    private String url;   // 前端 <video> 标签的 src
    private Long size;
}
