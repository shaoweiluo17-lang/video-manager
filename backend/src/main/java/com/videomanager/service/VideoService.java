package com.videomanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.videomanager.dto.VideoQueryRequest;
import com.videomanager.entity.Video;
import com.videomanager.mapper.VideoMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 视频服务
 */
@Service
public class VideoService {
    
    private final VideoMapper videoMapper;
    
    public VideoService(VideoMapper videoMapper) {
        this.videoMapper = videoMapper;
    }
    
    /**
     * 分页查询视频列表
     */
    public Page<Video> listVideos(VideoQueryRequest request) {
        Page<Video> page = new Page<>(request.getPage(), request.getPageSize());
        
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getFolderId() != null) {
            wrapper.eq(Video::getFolderId, request.getFolderId());
        }
        
        if (request.getDislike() != null) {
            wrapper.eq(Video::getDislike, request.getDislike());
        }
        
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Video::getFileName, request.getKeyword());
        }
        
        wrapper.orderByDesc(Video::getCreatedAt);
        
        return videoMapper.selectPage(page, wrapper);
    }
    
    /**
     * 获取视频详情
     */
    public Video getVideoById(Integer id) {
        return videoMapper.selectById(id);
    }
    
    /**
     * 获取随机一个视频
     */
    public Video getRandomVideo() {
        List<Video> videos = videoMapper.selectList(
            new LambdaQueryWrapper<Video>()
                .eq(Video::getDislike, 0)
                .last("ORDER BY RANDOM() LIMIT 1")
        );
        return videos.isEmpty() ? null : videos.get(0);
    }
    
    /**
     * 标记/取消不喜欢
     */
    public Video toggleDislike(Integer id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }
        video.setDislike(video.getDislike() == 1 ? 0 : 1);
        videoMapper.updateById(video);
        return video;
    }
    
    /**
     * 删除视频记录
     */
    public void deleteVideo(Integer id) {
        videoMapper.deleteById(id);
    }
    
    /**
     * 批量删除不喜欢的视频
     */
    public int deleteDislikedVideos() {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getDislike, 1);
        return videoMapper.delete(wrapper);
    }
    
    /**
     * 获取所有不喜欢的视频数量
     */
    public long getDislikedCount() {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getDislike, 1);
        return videoMapper.selectCount(wrapper);
    }
    
    /**
     * 获取视频总数
     */
    public long getTotalCount() {
        return videoMapper.selectCount(null);
    }
    
    /**
     * 检查文件路径是否已存在
     */
    public boolean existsByFilePath(String filePath) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getFilePath, filePath);
        return videoMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 新增视频
     */
    public void insertVideo(Video video) {
        videoMapper.insert(video);
    }
    
    /**
     * 更新视频信息
     */
    public void updateVideo(Video video) {
        videoMapper.updateById(video);
    }
    
    /**
     * 根据哈希值查找重复视频
     */
    public List<Video> findByHash(String hash) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getHash, hash);
        return videoMapper.selectList(wrapper);
    }
}
