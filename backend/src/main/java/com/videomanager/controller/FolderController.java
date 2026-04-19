package com.videomanager.controller;

import com.videomanager.dto.ApiResponse;
import com.videomanager.entity.Folder;
import com.videomanager.service.FolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件夹控制器
 */
@RestController
@RequestMapping("/api/folders")
public class FolderController {
    
    private final FolderService folderService;
    
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }
    
    /**
     * 获取文件夹树
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Folder>>> getFolders(
            @RequestParam(required = false) String type) {
        List<Folder> folders = folderService.getFolderTree(type);
        return ResponseEntity.ok(ApiResponse.success(folders));
    }
    
    /**
     * 获取根文件夹列表
     */
    @GetMapping("/roots")
    public ResponseEntity<ApiResponse<List<Folder>>> getRootFolders(
            @RequestParam(required = false) String type) {
        List<Folder> folders = folderService.getRootFolders(type);
        return ResponseEntity.ok(ApiResponse.success(folders));
    }
    
    /**
     * 获取子文件夹
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<Folder>>> getChildFolders(@PathVariable Integer id) {
        List<Folder> folders = folderService.getChildFolders(id);
        return ResponseEntity.ok(ApiResponse.success(folders));
    }
    
    /**
     * 获取文件夹详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Folder>> getFolder(@PathVariable Integer id) {
        Folder folder = folderService.getFolderById(id);
        if (folder == null) {
            return ResponseEntity.ok(ApiResponse.error("文件夹不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(folder));
    }
    
    /**
     * 删除所有空文件夹
     */
    @DeleteMapping("/empty")
    public ResponseEntity<ApiResponse<Integer>> deleteEmptyFolders() {
        try {
            int count = folderService.deleteEmptyFolders();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
