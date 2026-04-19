package com.videomanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.videomanager.entity.Folder;
import com.videomanager.entity.Image;
import com.videomanager.entity.ScanPath;
import com.videomanager.entity.Video;
import com.videomanager.mapper.FolderMapper;
import com.videomanager.mapper.ImageMapper;
import com.videomanager.mapper.ScanPathMapper;
import com.videomanager.mapper.VideoMapper;
import com.videomanager.util.FileUtil;
import com.videomanager.util.Md5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件扫描服务
 */
@Service
public class ScanService {
    
    private static final Logger log = LoggerFactory.getLogger(ScanService.class);
    
    private final ScanPathMapper scanPathMapper;
    private final FolderMapper folderMapper;
    private final VideoMapper videoMapper;
    private final ImageMapper imageMapper;
    private final FileUtil fileUtil;
    private final ThumbnailService thumbnailService;
    
    @Value("${scan.video-extensions}")
    private String videoExtensions;
    
    @Value("${scan.image-extensions}")
    private String imageExtensions;
    
    // 扫描状态
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private final AtomicInteger scanProgress = new AtomicInteger(0);
    private final AtomicInteger scanTotal = new AtomicInteger(0);
    private final AtomicInteger scanCurrent = new AtomicInteger(0);
    private volatile String currentScanPath = "";
    
    public ScanService(ScanPathMapper scanPathMapper, 
                       FolderMapper folderMapper,
                       VideoMapper videoMapper,
                       ImageMapper imageMapper,
                       FileUtil fileUtil,
                       ThumbnailService thumbnailService) {
        this.scanPathMapper = scanPathMapper;
        this.folderMapper = folderMapper;
        this.videoMapper = videoMapper;
        this.imageMapper = imageMapper;
        this.fileUtil = fileUtil;
        this.thumbnailService = thumbnailService;
    }
    
    /**
     * 获取扫描路径列表
     */
    public List<ScanPath> getScanPaths() {
        return scanPathMapper.selectList(null);
    }
    
    /**
     * 获取指定类型的扫描路径
     */
    public List<ScanPath> getScanPaths(String mediaType) {
        LambdaQueryWrapper<ScanPath> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(mediaType) && !"all".equals(mediaType)) {
            wrapper.eq(ScanPath::getMediaType, mediaType)
                   .or()
                   .eq(ScanPath::getMediaType, "all");
        }
        return scanPathMapper.selectList(wrapper);
    }
    
    /**
     * 添加扫描路径
     */
    public ScanPath addScanPath(String path, String mediaType) {
        ScanPath scanPath = new ScanPath();
        scanPath.setPath(path);
        scanPath.setMediaType(mediaType != null ? mediaType : "all");
        scanPath.setEnabled(1);
        scanPathMapper.insert(scanPath);
        return scanPath;
    }
    
    /**
     * 更新扫描路径
     */
    public void updateScanPath(Integer id, String path, String mediaType, Integer enabled) {
        ScanPath scanPath = scanPathMapper.selectById(id);
        if (scanPath != null) {
            if (path != null) scanPath.setPath(path);
            if (mediaType != null) scanPath.setMediaType(mediaType);
            if (enabled != null) scanPath.setEnabled(enabled);
            scanPathMapper.updateById(scanPath);
        }
    }
    
    /**
     * 删除扫描路径
     */
    public void deleteScanPath(Integer id) {
        scanPathMapper.deleteById(id);
    }
    
    /**
     * 触发全量扫描
     */
    @Async("taskExecutor")
    public void startScan() {
        if (!isScanning.compareAndSet(false, true)) {
            log.warn("扫描已在进行中");
            return;
        }
        
        try {
            log.info("开始全量扫描...");
            scanProgress.set(0);
            scanCurrent.set(0);
            
            List<ScanPath> scanPaths = getScanPaths();
            int totalFiles = countFiles(scanPaths);
            scanTotal.set(totalFiles);
            
            for (ScanPath scanPath : scanPaths) {
                if (!isScanning.get()) {
                    log.info("扫描被停止");
                    break;
                }
                
                if (scanPath.getEnabled() == 1) {
                    currentScanPath = scanPath.getPath();
                    scanDirectory(scanPath);
                    
                    // 更新最后扫描时间
                    scanPath.setLastScanAt(LocalDateTime.now());
                    scanPathMapper.updateById(scanPath);
                }
            }
            
            log.info("扫描完成！共处理 {} 个文件", scanCurrent.get());
        } finally {
            isScanning.set(false);
            scanProgress.set(100);
        }
    }
    
    /**
     * 扫描指定路径
     */
    @Async("taskExecutor")
    public void startScan(Integer pathId) {
        ScanPath scanPath = scanPathMapper.selectById(pathId);
        if (scanPath == null || scanPath.getEnabled() != 1) {
            return;
        }
        
        if (!isScanning.compareAndSet(false, true)) {
            log.warn("扫描已在进行中");
            return;
        }
        
        try {
            log.info("开始扫描路径: {}", scanPath.getPath());
            scanProgress.set(0);
            scanCurrent.set(0);
            currentScanPath = scanPath.getPath();
            
            int totalFiles = countFiles(List.of(scanPath));
            scanTotal.set(totalFiles);
            
            scanDirectory(scanPath);
            
            scanPath.setLastScanAt(LocalDateTime.now());
            scanPathMapper.updateById(scanPath);
            
            log.info("扫描完成！共处理 {} 个文件", scanCurrent.get());
        } finally {
            isScanning.set(false);
            scanProgress.set(100);
        }
    }
    
    /**
     * 停止扫描
     */
    public void stopScan() {
        isScanning.set(false);
        log.info("扫描已请求停止");
    }
    
    /**
     * 获取扫描状态
     */
    public Map<String, Object> getScanStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isScanning", isScanning.get());
        status.put("progress", scanProgress.get());
        status.put("current", scanCurrent.get());
        status.put("total", scanTotal.get());
        status.put("currentPath", currentScanPath);
        return status;
    }
    
    /**
     * 统计文件数量
     */
    private int countFiles(List<ScanPath> scanPaths) {
        int count = 0;
        for (ScanPath sp : scanPaths) {
            File dir = new File(sp.getPath());
            if (dir.exists() && dir.isDirectory()) {
                count += countFilesInDir(dir, sp.getMediaType());
            }
        }
        return count;
    }
    
    private int countFilesInDir(File dir, String mediaType) {
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countFilesInDir(file, mediaType);
                } else if (isMediaFile(file, mediaType)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * 扫描目录
     */
    private void scanDirectory(ScanPath scanPath) {
        File dir = new File(scanPath.getPath());
        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("目录不存在或不是目录: {}", scanPath.getPath());
            return;
        }
        
        scanDirectoryRecursive(dir, scanPath.getMediaType());
    }
    
    private void scanDirectoryRecursive(File dir, String mediaType) {
        if (!isScanning.get()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (!isScanning.get()) {
                return;
            }
            
            if (file.isDirectory()) {
                scanDirectoryRecursive(file, mediaType);
            } else if (isMediaFile(file, mediaType)) {
                processMediaFile(file, mediaType);
                scanCurrent.incrementAndGet();
                scanProgress.set((int) (scanCurrent.get() * 100.0 / Math.max(scanTotal.get(), 1)));
            }
        }
    }
    
    /**
     * 处理媒体文件
     */
    private void processMediaFile(File file, String mediaType) {
        try {
            String filePath = file.getAbsolutePath();
            
            if ("video".equals(mediaType) || "all".equals(mediaType)) {
                if (fileUtil.isVideo(file)) {
                    processVideoFile(file);
                    return;
                }
            }
            
            if ("image".equals(mediaType) || "all".equals(mediaType)) {
                if (fileUtil.isImage(file)) {
                    processImageFile(file);
                }
            }
        } catch (Exception e) {
            log.error("处理文件失败: {}, error: {}", file.getPath(), e.getMessage());
        }
    }
    
    /**
     * 处理视频文件
     */
    private void processVideoFile(File file) throws Exception {
        // 检查是否已存在
        LambdaQueryWrapper<Video> videoWrapper = new LambdaQueryWrapper<>();
        videoWrapper.eq(Video::getFilePath, file.getAbsolutePath());
        if (videoMapper.selectCount(videoWrapper) > 0) {
            return;
        }
        
        Video video = new Video();
        video.setFilePath(file.getAbsolutePath());
        video.setFileName(file.getName());
        video.setFileSize(file.length());
        
        // 计算 MD5
        String hash = Md5Util.encodeFile(file);
        video.setHash(hash);
        
        // 检查重复
        videoWrapper.eq(Video::getHash, hash);
        if (videoMapper.selectCount(videoWrapper) > 0) {
            log.debug("跳过重复视频: {}", file.getName());
            return;
        }
        
        videoMapper.insert(video);
        log.debug("新增视频: {}", file.getName());
    }
    
    /**
     * 处理图片文件
     */
    private void processImageFile(File file) throws Exception {
        // 检查是否已存在
        LambdaQueryWrapper<Image> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(Image::getFilePath, file.getAbsolutePath());
        if (imageMapper.selectCount(imageWrapper) > 0) {
            return;
        }
        
        Image image = new Image();
        image.setFilePath(file.getAbsolutePath());
        image.setFileName(file.getName());
        image.setFileSize(file.length());
        image.setMimeType(getMimeType(file));
        
        // 计算 MD5
        String hash = Md5Util.encodeFile(file);
        image.setHash(hash);
        
        // 检查重复
        imageWrapper.eq(Image::getHash, hash);
        if (imageMapper.selectCount(imageWrapper) > 0) {
            log.debug("跳过重复图片: {}", file.getName());
            return;
        }
        
        // 生成缩略图
        String thumbnailPath = thumbnailService.generateImageThumbnail(file);
        image.setThumbnailPath(thumbnailPath);
        
        imageMapper.insert(image);
        log.debug("新增图片: {}", file.getName());
    }
    
    /**
     * 判断是否为媒体文件
     */
    private boolean isMediaFile(File file, String mediaType) {
        if ("video".equals(mediaType)) {
            return fileUtil.isVideo(file);
        } else if ("image".equals(mediaType)) {
            return fileUtil.isImage(file);
        } else {
            return fileUtil.isVideo(file) || fileUtil.isImage(file);
        }
    }
    
    /**
     * 获取 MIME 类型
     */
    private String getMimeType(File file) {
        String ext = FileUtil.getExtension(file).toLowerCase();
        switch (ext) {
            case "jpg": case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "svg":
                return "image/svg+xml";
            default:
                return "application/octet-stream";
        }
    }
}