package com.videomanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 扫描路径配置实体类
 */
@Data
@TableName("scan_path")
public class ScanPath {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 扫描路径
     */
    private String path;
    
    /**
     * 媒体类型: video, image, all
     */
    private String mediaType;
    
    /**
     * 是否启用
     */
    private Integer enabled;
    
    /**
     * 最后扫描时间
     */
    private LocalDateTime lastScanAt;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
