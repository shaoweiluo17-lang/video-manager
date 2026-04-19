package com.videomanager.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.videomanager.dto.ApiResponse;
import com.videomanager.dto.ImageQueryRequest;
import com.videomanager.entity.Image;
import com.videomanager.service.ImageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * 图片控制器
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {
    
    private final ImageService imageService;
    
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }
    
    /**
     * 获取图片列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Image>>> listImages(ImageQueryRequest request) {
        Page<Image> page = imageService.listImages(request);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
    
    /**
     * 获取图片详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Image>> getImage(@PathVariable Integer id) {
        Image image = imageService.getImageById(id);
        if (image == null) {
            return ResponseEntity.ok(ApiResponse.error("图片不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(image));
    }
    
    /**
     * 标记/取消不喜欢
     */
    @PutMapping("/{id}/dislike")
    public ResponseEntity<ApiResponse<Image>> toggleDislike(@PathVariable Integer id) {
        try {
            Image image = imageService.toggleDislike(id);
            return ResponseEntity.ok(ApiResponse.success(image));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除图片记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Integer id) {
        try {
            imageService.deleteImage(id);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 批量删除不喜欢的图片
     */
    @DeleteMapping("/dislikes")
    public ResponseEntity<ApiResponse<Integer>> deleteDislikedImages() {
        try {
            int count = imageService.deleteDislikedImages();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取图片缩略图
     */
    @GetMapping("/thumb/{id}")
    public ResponseEntity<Resource> getImageThumb(@PathVariable Integer id) {
        Image image = imageService.getImageById(id);
        if (image == null || image.getThumbnailPath() == null) {
            return ResponseEntity.notFound().build();
        }
        
        File thumbFile = new File(image.getThumbnailPath());
        if (!thumbFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(thumbFile);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(resource);
    }
    
    /**
     * 获取原图
     */
    @GetMapping("/raw/{id}")
    public ResponseEntity<Resource> getImageRaw(@PathVariable Integer id) {
        Image image = imageService.getImageById(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        
        File imageFile = new File(image.getFilePath());
        if (!imageFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(imageFile);
        
        String contentType = image.getMimeType() != null ? image.getMimeType() : MediaType.IMAGE_JPEG_VALUE;
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }
}
