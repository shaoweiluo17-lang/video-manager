package com.videomanager.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件工具类
 */
@Component
public class FileUtil {
    
    @Value("${scan.video-extensions}")
    private String videoExtensions;
    
    @Value("${scan.image-extensions}")
    private String imageExtensions;
    
    private Set<String> videoExtSet;
    private Set<String> imageExtSet;
    
    /**
     * 格式化文件大小
     */
    public static String formatSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double sizeDouble = size.doubleValue();
        
        while (sizeDouble >= 1024 && unitIndex < units.length - 1) {
            sizeDouble /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", sizeDouble, units[unitIndex]);
    }
    
    /**
     * 格式化时长（秒转为 mm:ss 或 hh:mm:ss）
     */
    public static String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) {
            return "00:00";
        }
        
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
    
    /**
     * 获取文件扩展名（不含点）
     */
    public static String getExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 判断是否为视频文件
     */
    public boolean isVideo(File file) {
        if (videoExtSet == null) {
            // 处理扩展名，移除点号
            Set<String> exts = new HashSet<>();
            for (String ext : videoExtensions.toLowerCase().split(",")) {
                exts.add(ext.replace(".", "").trim());
            }
            videoExtSet = exts;
        }
        String ext = getExtension(file);
        return videoExtSet.contains(ext);
    }
    
    /**
     * 判断是否为图片文件
     */
    public boolean isImage(File file) {
        if (imageExtSet == null) {
            // 处理扩展名，移除点号
            Set<String> exts = new HashSet<>();
            for (String ext : imageExtensions.toLowerCase().split(",")) {
                exts.add(ext.replace(".", "").trim());
            }
            imageExtSet = exts;
        }
        String ext = getExtension(file);
        return imageExtSet.contains(ext);
    }
    
    /**
     * 获取不带扩展名的文件名
     */
    public static String getNameWithoutExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        return name;
    }
    
    /**
     * 检查文件是否存在且可读
     */
    public static boolean isReadable(File file) {
        return file.exists() && file.isFile() && file.canRead();
    }
    
    /**
     * 检查目录是否存在且可读
     */
    public static boolean isReadableDirectory(File dir) {
        return dir.exists() && dir.isDirectory() && dir.canRead();
    }
}
