package com.videomanager.service;

import com.videomanager.dto.*;
import com.videomanager.entity.User;
import com.videomanager.mapper.UserMapper;
import com.videomanager.util.JwtUtil;
import com.videomanager.util.PasswordUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证服务
 */
@Service
public class AuthService {
    
    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserMapper userMapper, PasswordUtil passwordUtil, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * 用户注册
     */
    public UserResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        List<User> existingUsers = userMapper.selectList(null);
        for (User user : existingUsers) {
            if (user.getUsername().equals(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordUtil.encode(request.getPassword()));
        userMapper.insert(user);
        
        return new UserResponse(user.getId(), user.getUsername());
    }
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        List<User> users = userMapper.selectList(null);
        User user = null;
        for (User u : users) {
            if (u.getUsername().equals(request.getUsername())) {
                user = u;
                break;
            }
        }
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        return new LoginResponse(token, new UserResponse(user.getId(), user.getUsername()));
    }
    
    /**
     * 获取当前用户
     */
    public UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Integer) {
            Integer userId = (Integer) principal;
            User user = userMapper.selectById(userId);
            if (user != null) {
                return new UserResponse(user.getId(), user.getUsername());
            }
        }
        throw new RuntimeException("用户未登录");
    }
    
    /**
     * 获取当前用户ID
     */
    public Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Integer) {
            return (Integer) principal;
        }
        return null;
    }
}
