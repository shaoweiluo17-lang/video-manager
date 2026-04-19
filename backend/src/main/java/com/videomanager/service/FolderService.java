package com.videomanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.videomanager.entity.Folder;
import com.videomanager.mapper.FolderMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件夹服务
 */
@Service
public class FolderService {
    
    private final FolderMapper folderMapper;
    
    public FolderService(FolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }
    
    /**
     * 获取文件夹树
     */
    public List<Folder> getFolderTree(String mediaType) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        
        if (mediaType != null && !mediaType.isEmpty() && !"all".equals(mediaType)) {
            wrapper.eq(Folder::getMediaType, mediaType)
                   .or()
                   .eq(Folder::getMediaType, "all");
        }
        
        wrapper.orderByAsc(Folder::getName);
        
        List<Folder> allFolders = folderMapper.selectList(wrapper);
        
        // 构建树形结构
        return buildTree(allFolders);
    }
    
    /**
     * 构建树形结构
     */
    private List<Folder> buildTree(List<Folder> folders) {
        // 按父ID分组
        List<Folder> rootFolders = new ArrayList<>();
        
        for (Folder folder : folders) {
            if (folder.getParentId() == null) {
                rootFolders.add(folder);
            }
        }
        
        // 递归添加子文件夹
        for (Folder folder : rootFolders) {
            addChildren(folder, folders);
        }
        
        return rootFolders;
    }
    
    /**
     * 递归添加子文件夹
     */
    private void addChildren(Folder parent, List<Folder> allFolders) {
        List<Folder> children = allFolders.stream()
            .filter(f -> parent.getId().equals(f.getParentId()))
            .collect(Collectors.toList());
        
        for (Folder child : children) {
            addChildren(child, allFolders);
        }
        
        parent.setFolderCount(children.size());
    }
    
    /**
     * 获取文件夹详情
     */
    public Folder getFolderById(Integer id) {
        return folderMapper.selectById(id);
    }
    
    /**
     * 获取根文件夹列表
     */
    public List<Folder> getRootFolders(String mediaType) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Folder::getParentId);
        
        if (mediaType != null && !mediaType.isEmpty() && !"all".equals(mediaType)) {
            wrapper.eq(Folder::getMediaType, mediaType)
                   .or()
                   .eq(Folder::getMediaType, "all");
        }
        
        wrapper.orderByAsc(Folder::getName);
        
        return folderMapper.selectList(wrapper);
    }
    
    /**
     * 获取子文件夹
     */
    public List<Folder> getChildFolders(Integer parentId) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getParentId, parentId);
        wrapper.orderByAsc(Folder::getName);
        return folderMapper.selectList(wrapper);
    }
    
    /**
     * 检查路径是否已存在
     */
    public boolean existsByPath(String path) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getPath, path);
        return folderMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 新增文件夹
     */
    public void insertFolder(Folder folder) {
        folderMapper.insert(folder);
    }
    
    /**
     * 更新文件夹信息
     */
    public void updateFolder(Folder folder) {
        folderMapper.updateById(folder);
    }
    
    /**
     * 删除文件夹
     */
    public void deleteFolder(Integer id) {
        folderMapper.deleteById(id);
    }
    
    /**
     * 删除所有空文件夹
     */
    public int deleteEmptyFolders() {
        List<Folder> folders = folderMapper.selectList(null);
        int deletedCount = 0;
        
        for (Folder folder : folders) {
            if (folder.getFileCount() == 0 && folder.getFolderCount() == 0) {
                folderMapper.deleteById(folder.getId());
                deletedCount++;
            }
        }
        
        return deletedCount;
    }
    
    /**
     * 更新文件夹统计
     */
    public void updateFolderStats(Integer folderId, int fileCount, int folderCount) {
        Folder folder = folderMapper.selectById(folderId);
        if (folder != null) {
            folder.setFileCount(fileCount);
            folder.setFolderCount(folderCount);
            folderMapper.updateById(folder);
        }
    }
}
