package com.videomanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.videomanager.dto.ImageQueryRequest;
import com.videomanager.entity.Image;
import com.videomanager.mapper.ImageMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 图片服务
 */
@Service
public class ImageService {
    
    private final ImageMapper imageMapper;
    
    public ImageService(ImageMapper imageMapper) {
        this.imageMapper = imageMapper;
    }
    
    /**
     * 分页查询图片列表
     */
    public Page<Image> listImages(ImageQueryRequest request) {
        Page<Image> page = new Page<>(request.getPage(), request.getPageSize());
        
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getFolderId() != null) {
            wrapper.eq(Image::getFolderId, request.getFolderId());
        }
        
        if (request.getDislike() != null) {
            wrapper.eq(Image::getDislike, request.getDislike());
        }
        
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Image::getFileName, request.getKeyword());
        }
        
        wrapper.orderByDesc(Image::getCreatedAt);
        
        return imageMapper.selectPage(page, wrapper);
    }
    
    /**
     * 获取图片详情
     */
    public Image getImageById(Integer id) {
        return imageMapper.selectById(id);
    }
    
    /**
     * 标记/取消不喜欢
     */
    public Image toggleDislike(Integer id) {
        Image image = imageMapper.selectById(id);
        if (image == null) {
            throw new RuntimeException("图片不存在");
        }
        image.setDislike(image.getDislike() == 1 ? 0 : 1);
        imageMapper.updateById(image);
        return image;
    }
    
    /**
     * 删除图片记录
     */
    public void deleteImage(Integer id) {
        imageMapper.deleteById(id);
    }
    
    /**
     * 批量删除不喜欢的图片
     */
    public int deleteDislikedImages() {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getDislike, 1);
        return imageMapper.delete(wrapper);
    }
    
    /**
     * 获取所有不喜欢的图片数量
     */
    public long getDislikedCount() {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getDislike, 1);
        return imageMapper.selectCount(wrapper);
    }
    
    /**
     * 获取图片总数
     */
    public long getTotalCount() {
        return imageMapper.selectCount(null);
    }
    
    /**
     * 检查文件路径是否已存在
     */
    public boolean existsByFilePath(String filePath) {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getFilePath, filePath);
        return imageMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 新增图片
     */
    public void insertImage(Image image) {
        imageMapper.insert(image);
    }
    
    /**
     * 更新图片信息
     */
    public void updateImage(Image image) {
        imageMapper.updateById(image);
    }
    
    /**
     * 根据哈希值查找重复图片
     */
    public List<Image> findByHash(String hash) {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getHash, hash);
        return imageMapper.selectList(wrapper);
    }
}
