package com.videomanager.controller;

import com.videomanager.dto.ApiResponse;
import com.videomanager.entity.ScanPath;
import com.videomanager.service.ScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 扫描控制器
 */
@RestController
@RequestMapping("/api/scan")
public class ScanController {
    
    private final ScanService scanService;
    
    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }
    
    /**
     * 获取扫描路径列表
     */
    @GetMapping("/paths")
    public ResponseEntity<ApiResponse<List<ScanPath>>> getScanPaths() {
        List<ScanPath> paths = scanService.getScanPaths();
        return ResponseEntity.ok(ApiResponse.success(paths));
    }
    
    /**
     * 添加扫描路径
     */
    @PostMapping("/paths")
    public ResponseEntity<ApiResponse<ScanPath>> addScanPath(
            @RequestParam String path,
            @RequestParam(required = false, defaultValue = "all") String mediaType) {
        try {
            ScanPath scanPath = scanService.addScanPath(path, mediaType);
            return ResponseEntity.ok(ApiResponse.success(scanPath));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新扫描路径
     */
    @PutMapping("/paths/{id}")
    public ResponseEntity<ApiResponse<Void>> updateScanPath(
            @PathVariable Integer id,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String mediaType,
            @RequestParam(required = false) Integer enabled) {
        try {
            scanService.updateScanPath(id, path, mediaType, enabled);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除扫描路径
     */
    @DeleteMapping("/paths/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScanPath(@PathVariable Integer id) {
        try {
            scanService.deleteScanPath(id);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 触发全量扫描
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<String>> startScan() {
        try {
            scanService.startScan();
            return ResponseEntity.ok(ApiResponse.success("扫描已启动"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 扫描指定路径
     */
    @PostMapping("/start/{pathId}")
    public ResponseEntity<ApiResponse<String>> startScan(@PathVariable Integer pathId) {
        try {
            scanService.startScan(pathId);
            return ResponseEntity.ok(ApiResponse.success("扫描已启动"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 停止扫描
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<String>> stopScan() {
        scanService.stopScan();
        return ResponseEntity.ok(ApiResponse.success("停止扫描请求已发送"));
    }
    
    /**
     * 获取扫描状态
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getScanStatus() {
        Map<String, Object> status = scanService.getScanStatus();
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
