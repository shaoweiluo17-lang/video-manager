package com.videomanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户信息响应（不包含敏感信息）
 */
@Data
@AllArgsConstructor
public class UserResponse {
    
    private Integer id;
    private String username;
}
