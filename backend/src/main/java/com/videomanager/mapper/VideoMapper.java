package com.videomanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.videomanager.entity.Video;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频文件 Mapper
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}
