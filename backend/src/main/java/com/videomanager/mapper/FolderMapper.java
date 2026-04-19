package com.videomanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.videomanager.entity.Folder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件夹 Mapper
 */
@Mapper
public interface FolderMapper extends BaseMapper<Folder> {
}
