package org.soso.ledger.controller;

import org.soso.ledger.common.Result; // 假设你的通用返回类
import org.soso.ledger.dto.VideoDTO;
import org.soso.ledger.service.VideoService;
import org.soso.ledger.common.UserContext; // 假设你的获取当前用户的工具
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 上传视频
     */
    @PostMapping("/upload")
    public Result<VideoDTO> upload(@RequestParam("file") MultipartFile file) {
        // 1. 获取当前登录用户ID
        Long userId = UserContext.getCurrentUserId();

        // 2. 调用 Service
        VideoDTO videoDTO = videoService.uploadVideo(file, userId);

        // 3. 返回前端指定的 {id, originalName, url, size} 结构
        return Result.success(videoDTO);
    }

    /**
     * 获取当前用户的视频列表
     */
    @GetMapping
    public Result<List<VideoDTO>> list() {
        Long userId = UserContext.getCurrentUserId();
        List<VideoDTO> list = videoService.getUserVideos(userId);
        return Result.success(list);
    }
}