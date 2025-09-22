package com.jobs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author: xulai
 * @Date: 2025/1/27
 * @Describe: 定时任务配置类
 */
@Configuration
@EnableScheduling
public class ScheduledConfig {
    // 启用Spring的定时任务功能
    // 通过@EnableScheduling注解启用定时任务支持
}
