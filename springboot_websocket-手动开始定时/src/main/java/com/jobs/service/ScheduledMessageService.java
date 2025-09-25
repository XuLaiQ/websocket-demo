package com.jobs.service;

import com.jobs.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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
     * 每30秒向所有启用了定时任务的用户推送一次消息
     */
    public void sendScheduledMessage() {
        int scheduledUserCount = WebSocketServer.getScheduledUserCount();
        
        if (scheduledUserCount > 0) {
            String currentTime = LocalDateTime.now().format(FORMATTER);
            String message = String.format("【定时推送】当前时间：%s，启用定时任务的用户数：%d", currentTime, scheduledUserCount);
            
            log.info("开始执行定时推送，启用定时任务的用户数：{}", scheduledUserCount);
            WebSocketServer.sendMessageToScheduledUsers(message);
        } else {
            log.info("当前无用户启用定时任务，跳过定时推送");
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
    
    // 用户级定时任务管理方法
    
    /**
     * 启动指定用户的定时任务
     */
    public void startUserScheduledTask(String username) {
        WebSocketServer.startUserScheduledTask(username);
        
        // 如果全局定时任务未启动，且有用户启用了定时任务，则启动全局定时任务
        if (!isScheduledRunning && WebSocketServer.getScheduledUserCount() > 0) {
            startScheduledTask();
        }
    }
    
    /**
     * 停止指定用户的定时任务
     */
    public void stopUserScheduledTask(String username) {
        WebSocketServer.stopUserScheduledTask(username);
        
        // 如果没有用户启用定时任务，停止全局定时任务
        if (isScheduledRunning && WebSocketServer.getScheduledUserCount() == 0) {
            stopScheduledTask();
        }
    }
    
    /**
     * 获取指定用户的定时任务状态
     */
    public boolean getUserScheduledTaskStatus(String username) {
        return WebSocketServer.getUserScheduledTaskStatus(username);
    }
    
    /**
     * 向指定用户发送定时消息
     */
    public void sendScheduledMessageToUser(String username, String message) {
        WebSocketServer.sendScheduledMessageToUser(username, message);
    }
    
    /**
     * 获取用户级定时任务状态信息
     */
    public Object getUserScheduledTaskInfo(String username) {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        String[] onlineUsers = WebSocketServer.getOnlineUsers();
        int scheduledUserCount = WebSocketServer.getScheduledUserCount();
        String[] scheduledUsers = WebSocketServer.getScheduledUsers();
        String currentTime = LocalDateTime.now().format(FORMATTER);
        boolean userScheduledStatus = WebSocketServer.getUserScheduledTaskStatus(username);
        String message = String.format("用户 %s 的定时任务状态：%s", username, userScheduledStatus ? "已启用" : "已停用");
        
        // 使用Map来避免匿名类的字段引用问题
        Map<String, Object> result = new HashMap<>();
        result.put("onlineCount", onlineCount);
        result.put("onlineUsers", onlineUsers);
        result.put("scheduledUserCount", scheduledUserCount);
        result.put("scheduledUsers", scheduledUsers);
        result.put("currentTime", currentTime);
        result.put("userScheduledStatus", userScheduledStatus);
        result.put("message", message);
        
        return result;
    }
}
