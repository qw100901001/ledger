package org.soso.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.soso.ledger.dto.VideoDTO;
import org.soso.ledger.entity.Video;
import org.soso.ledger.mapper.VideoMapper;
import org.soso.ledger.service.VideoService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {
    // 磁盘存储的基础目录
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/videos/";

    @Override
    public VideoDTO uploadVideo(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        // 获取后缀名
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        // 生成磁盘真实文件名
        String storageName = UUID.randomUUID().toString().replace("-", "") + suffix;
        // 1. 落盘
        File dest = new File(UPLOAD_DIR + storageName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
        // 2. 落库
        Video video = new Video();
        video.setUserId(userId);
        video.setOriginalName(originalName);
        video.setStorageName(storageName);
        video.setSize(file.getSize());
        this.save(video); // MyBatis-Plus 自带保存
        // 3. 返回前端需要的结构
        return convertToDTO(video);

    }

    @Override
    public List<VideoDTO> getUserVideos(Long userId) {
        // 只查当前用户的记录，按时间倒序R
        List<Video> videos = this.lambdaQuery()
                .eq(Video::getUserId, userId)
                .orderByDesc(Video::getCreatedAt)
                .list();

        return videos.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    // 实体转 VO 的工具方法
    private VideoDTO convertToDTO(Video video) {
        VideoDTO dto = new VideoDTO();
        dto.setId(video.getId());
        dto.setOriginalName(video.getOriginalName());
        dto.setSize(video.getSize());
        // 拼装前端的访问 URL
        dto.setUrl("/uploads/videos/" + video.getStorageName());
        return dto;
    }
}