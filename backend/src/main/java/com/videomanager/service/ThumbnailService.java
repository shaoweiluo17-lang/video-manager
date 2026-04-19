package com.videomanager.service;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 缩略图生成服务
 */
@Service
public class ThumbnailService {
    
    private static final Logger log = LoggerFactory.getLogger(ThumbnailService.class);
    
    @Value("${thumbnail-dir:/app/data/thumbnails}")
    private String thumbnailDir;
    
    @Value("${thumbnail-width:320}")
    private int thumbnailWidth;
    
    @Value("${thumbnail-height:320}")
    private int thumbnailHeight;
    
    /**
     * 生成图片缩略图
     */
    public String generateImageThumbnail(File imageFile) {
        try {
            // 确保缩略图目录存在
            File thumbDir = new File(thumbnailDir + "/image");
            if (!thumbDir.exists()) {
                thumbDir.mkdirs();
            }
            
            // 生成缩略图文件名（使用 MD5）
            String hash = calculateMD5(imageFile);
            String thumbnailName = hash + ".jpg";
            File thumbnailFile = new File(thumbDir, thumbnailName);
            
            // 如果已存在，直接返回
            if (thumbnailFile.exists()) {
                return thumbnailFile.getAbsolutePath();
            }
            
            // 读取图片
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                log.warn("无法读取图片: {}", imageFile.getPath());
                return null;
            }
            
            // 计算缩略图尺寸（保持宽高比）
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            double ratio = Math.min(
                (double) thumbnailWidth / originalWidth,
                (double) thumbnailHeight / originalHeight
            );
            
            int newWidth = (int) (originalWidth * ratio);
            int newHeight = (int) (originalHeight * ratio);
            
            // 生成缩略图
            Thumbnails.of(imageFile)
                .size(newWidth, newHeight)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toFile(thumbnailFile);
            
            log.debug("生成缩略图: {} -> {}", imageFile.getName(), thumbnailFile.getName());
            
            return thumbnailFile.getAbsolutePath();
            
        } catch (IOException e) {
            log.error("生成缩略图失败: {}, error: {}", imageFile.getPath(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 生成视频缩略图（需要 FFmpeg）
     */
    public String generateVideoThumbnail(File videoFile, String ffmpegPath) {
        try {
            // 确保缩略图目录存在
            File thumbDir = new File(thumbnailDir + "/video");
            if (!thumbDir.exists()) {
                thumbDir.mkdirs();
            }
            
            // 生成缩略图文件名
            String hash = calculateMD5(videoFile);
            String thumbnailName = hash + ".jpg";
            File thumbnailFile = new File(thumbDir, thumbnailName);
            
            // 如果已存在，直接返回
            if (thumbnailFile.exists()) {
                return thumbnailFile.getAbsolutePath();
            }
            
            // 使用 FFmpeg 生成缩略图
            ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-y",                      // 覆盖输出文件
                "-i", videoFile.getPath(),  // 输入文件
                "-ss", "00:00:01",          // 截取时间点
                "-vframes", "1",            // 只取一帧
                "-q:v", "2",                // 输出质量
                "-vf", String.format("scale=%d:%d", thumbnailWidth, thumbnailHeight),
                thumbnailFile.getPath()     // 输出文件
            );
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && thumbnailFile.exists()) {
                log.debug("生成视频缩略图: {} -> {}", videoFile.getName(), thumbnailFile.getName());
                return thumbnailFile.getAbsolutePath();
            } else {
                log.warn("FFmpeg 生成视频缩略图失败: {}", videoFile.getPath());
                return null;
            }
            
        } catch (Exception e) {
            log.error("生成视频缩略图失败: {}, error: {}", videoFile.getPath(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 批量删除未使用的缩略图
     */
    public int cleanupUnusedThumbnails() {
        int deletedCount = 0;
        
        try {
            File thumbDir = new File(thumbnailDir);
            if (!thumbDir.exists()) {
                return 0;
            }
            
            File[] subDirs = thumbDir.listFiles();
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    if (subDir.isDirectory()) {
                        deletedCount += cleanupDirectory(subDir);
                    }
                }
            }
        } catch (Exception e) {
            log.error("清理未使用缩略图失败: {}", e.getMessage());
        }
        
        return deletedCount;
    }
    
    private int cleanupDirectory(File dir) {
        int deletedCount = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.lastModified() < System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) {
                    // 删除 30 天未访问的缩略图
                    if (file.delete()) {
                        deletedCount++;
                    }
                }
            }
        }
        return deletedCount;
    }
    
    /**
     * 计算文件 MD5
     */
    private String calculateMD5(File file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] digest = md.digest(fileBytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
