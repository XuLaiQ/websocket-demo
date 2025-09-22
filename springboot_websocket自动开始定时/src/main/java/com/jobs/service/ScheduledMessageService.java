package com.jobs.service;

import com.jobs.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 定时消息推送服务
 * @Author: xulai
 * @Date: 2025/1/27
 */
@Service
@Slf4j
public class ScheduledMessageService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 每30秒向所有在线用户推送一次消息
     * 可以通过cron表达式自定义推送时间
     */
    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void sendScheduledMessage() {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        
        if (onlineCount > 0) {
            String currentTime = LocalDateTime.now().format(FORMATTER);
            String message = String.format("【定时推送】当前时间：%s，在线用户数：%d", currentTime, onlineCount);
            
            log.info("开始执行定时推送，在线用户数：{}", onlineCount);
            WebSocketServer.sendMessageToAll(message);
        } else {
            log.info("当前无在线用户，跳过定时推送");
        }
    }

    /**
     * 每天上午9点推送早安消息
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendMorningMessage() {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        
        if (onlineCount > 0) {
            String message = "【早安推送】早上好！新的一天开始了，祝您工作愉快！";
            log.info("发送早安消息，在线用户数：{}", onlineCount);
            WebSocketServer.sendMessageToAll(message);
        }
    }

    /**
     * 每天下午6点推送下班提醒
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void sendEveningMessage() {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        
        if (onlineCount > 0) {
            String message = "【下班提醒】辛苦了！今天的工作即将结束，记得休息哦！";
            log.info("发送下班提醒，在线用户数：{}", onlineCount);
            WebSocketServer.sendMessageToAll(message);
        }
    }

    /**
     * 手动触发定时推送（用于测试）
     */
    public void sendManualMessage(String customMessage) {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        
        if (onlineCount > 0) {
            String message = "【手动推送】" + customMessage;
            log.info("发送手动推送消息，在线用户数：{}", onlineCount);
            WebSocketServer.sendMessageToAll(message);
        } else {
            log.warn("当前无在线用户，无法发送手动推送消息");
        }
    }
}
