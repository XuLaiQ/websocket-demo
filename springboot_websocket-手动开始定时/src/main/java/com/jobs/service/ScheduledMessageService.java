package com.jobs.service;

import com.jobs.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时消息推送服务
 * @Author: xulai
 * @Date: 2025/1/27
 */
@Service
@Slf4j
public class ScheduledMessageService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;
    private boolean isScheduledRunning = false;

    @PostConstruct
    public void init() {
        // 初始化任务调度器
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("scheduled-message-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    /**
     * 启动定时推送任务（当有用户连接时调用）
     */
    public void startScheduledTask() {
        if (isScheduledRunning) {
            log.info("定时推送任务已在运行中");
            return;
        }
        
        // 每30秒执行一次定时推送
        scheduledTask = taskScheduler.scheduleAtFixedRate(this::sendScheduledMessage, 3000);
        isScheduledRunning = true;
        log.info("定时推送任务已启动");
    }

    /**
     * 停止定时推送任务（当没有用户连接时调用）
     */
    public void stopScheduledTask() {
        if (!isScheduledRunning) {
            log.info("定时推送任务未在运行");
            return;
        }
        
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        isScheduledRunning = false;
        log.info("定时推送任务已停止");
    }

    /**
     * 检查并管理定时任务状态
     */
    public void checkAndManageScheduledTask() {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        
        if (onlineCount > 0 && !isScheduledRunning) {
            // 有用户在线但定时任务未启动，启动定时任务
            startScheduledTask();
        } else if (onlineCount == 0 && isScheduledRunning) {
            // 没有用户在线但定时任务在运行，停止定时任务
            stopScheduledTask();
        }
    }

    /**
     * 每30秒向所有在线用户推送一次消息
     */
    public void sendScheduledMessage() {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        
        if (onlineCount > 0) {
            String currentTime = LocalDateTime.now().format(FORMATTER);
            String message = String.format("【定时推送】当前时间：%s，在线用户数：%d", currentTime, onlineCount);
            
            log.info("开始执行定时推送，在线用户数：{}", onlineCount);
            WebSocketServer.sendMessageToAll(message);
        } else {
            log.info("当前无在线用户，跳过定时推送");
            // 如果没有用户在线，停止定时任务
            stopScheduledTask();
        }
    }

    /**
     * 每天上午9点推送早安消息
     */
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
