package com.videomanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 视频图片文件管理系统 - 启动类
 */
@SpringBootApplication
@EnableScheduling
public class VideoManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoManagerApplication.class, args);
    }
}
