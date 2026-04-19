package com.videomanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件夹实体类
 */
@Data
@TableName("folder")
public class Folder {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 文件夹路径（唯一）
     */
    private String path;
    
    /**
     * 文件夹名称
     */
    private String name;
    
    /**
     * 父文件夹ID（顶级为null）
     */
    private Integer parentId;
    
    /**
     * 媒体类型: video, image, all
     */
    private String mediaType;
    
    /**
     * 直接子文件数量
     */
    private Integer fileCount;
    
    /**
     * 直接子文件夹数量
     */
    private Integer folderCount;
    
    /**
     * 最后修改时间
     */
    private LocalDateTime lastModified;
    
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
