package org.soso.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.soso.ledger.dto.VideoDTO;
import org.soso.ledger.entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService extends IService<Video> {
    // 上传视频
    VideoDTO uploadVideo(MultipartFile file, Long userId);
    // 获取当前用户的视频列表
    List<VideoDTO> getUserVideos(Long userId);
}