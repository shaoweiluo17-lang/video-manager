package com.videomanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.videomanager.entity.Image;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图片文件 Mapper
 */
@Mapper
public interface ImageMapper extends BaseMapper<Image> {
}
