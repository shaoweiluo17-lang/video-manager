package com.videomanager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频列表查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VideoQueryRequest extends PageRequest {
    
    /**
     * 文件夹ID筛选
     */
    private Integer folderId;
    
    /**
     * 不喜欢筛选: 0-未标记, 1-不喜欢, null-全部
     */
    private Integer dislike;
    
    /**
     * 关键词搜索（文件名）
     */
    private String keyword;
}
