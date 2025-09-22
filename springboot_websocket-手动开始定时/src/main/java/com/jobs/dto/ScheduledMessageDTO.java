package com.jobs.dto;

import lombok.Data;

/**
 * 定时推送消息DTO
 * @Author: xulai
 * @Date: 2025/1/27
 */
@Data
public class ScheduledMessageDTO {
    
    /**
     * 自定义消息内容
     */
    private String message;
    
    /**
     * 推送类型：MANUAL-手动推送，AUTO-自动推送
     */
    private String type;
    
    /**
     * 推送时间（可选，用于记录）
     */
    private String pushTime;
}
