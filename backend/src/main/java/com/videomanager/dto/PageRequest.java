package com.videomanager.dto;

import lombok.Data;

/**
 * 分页请求
 */
@Data
public class PageRequest {
    
    private Integer page = 1;
    
    private Integer pageSize = 20;
    
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}
