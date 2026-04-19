package com.videomanager.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.videomanager.dto.ApiResponse;
import com.videomanager.dto.VideoQueryRequest;
import com.videomanager.entity.Video;
import com.videomanager.service.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * 视频控制器
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    
    private final VideoService videoService;
    
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }
    
    /**
     * 获取视频列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Video>>> listVideos(VideoQueryRequest request) {
        Page<Video> page = videoService.listVideos(request);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
    
    /**
     * 获取视频详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Video>> getVideo(@PathVariable Integer id) {
        Video video = videoService.getVideoById(id);
        if (video == null) {
            return ResponseEntity.ok(ApiResponse.error("视频不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(video));
    }
    
    /**
     * 获取随机一个视频
     */
    @GetMapping("/random")
    public ResponseEntity<ApiResponse<Video>> getRandomVideo() {
        Video video = videoService.getRandomVideo();
        if (video == null) {
            return ResponseEntity.ok(ApiResponse.error("没有可用的视频"));
        }
        return ResponseEntity.ok(ApiResponse.success(video));
    }
    
    /**
     * 标记/取消不喜欢
     */
    @PutMapping("/{id}/dislike")
    public ResponseEntity<ApiResponse<Video>> toggleDislike(@PathVariable Integer id) {
        try {
            Video video = videoService.toggleDislike(id);
            return ResponseEntity.ok(ApiResponse.success(video));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除视频记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable Integer id) {
        try {
            videoService.deleteVideo(id);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 批量删除不喜欢的视频
     */
    @DeleteMapping("/dislikes")
    public ResponseEntity<ApiResponse<Integer>> deleteDislikedVideos() {
        try {
            int count = videoService.deleteDislikedVideos();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 视频流播放
     */
    @GetMapping("/stream/{id}")
    public ResponseEntity<Resource> streamVideo(@PathVariable Integer id) {
        Video video = videoService.getVideoById(id);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        
        File videoFile = new File(video.getFilePath());
        if (!videoFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(videoFile);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(videoFile.length()))
                .body(resource);
    }
    
    /**
     * 视频缩略图
     */
    @GetMapping("/thumb/{id}")
    public ResponseEntity<Resource> getVideoThumb(@PathVariable Integer id) {
        Video video = videoService.getVideoById(id);
        if (video == null || video.getThumbnailPath() == null) {
            return ResponseEntity.notFound().build();
        }
        
        File thumbFile = new File(video.getThumbnailPath());
        if (!thumbFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(thumbFile);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(resource);
    }
}
