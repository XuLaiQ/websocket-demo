package com.jobs.config;

import org.springframework.context.annotation.Configuration;

/**
 * @Author: xulai
 * @Date: 2025/1/27
 * @Describe: 定时任务配置类（已改为手动控制模式）
 */
@Configuration
public class ScheduledConfig {
    // 已移除@EnableScheduling注解，改为手动控制定时任务
    // 定时任务现在通过ScheduledMessageService手动管理
}
