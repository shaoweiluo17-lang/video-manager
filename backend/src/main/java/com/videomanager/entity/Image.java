package com.videomanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 图片文件实体类
 */
@Data
@TableName("image")
public class Image {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 文件路径（唯一）
     */
    private String filePath;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 图片宽度
     */
    private Integer width;
    
    /**
     * 图片高度
     */
    private Integer height;
    
    /**
     * 缩略图路径
     */
    private String thumbnailPath;
    
    /**
     * 文件MD5哈希值（用于去重）
     */
    private String hash;
    
    /**
     * 是否不喜欢: 0-未标记, 1-不喜欢
     */
    private Integer dislike;
    
    /**
     * 所属文件夹ID
     */
    private Integer folderId;
    
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
