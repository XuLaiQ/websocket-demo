package com.jobs.controller;

import com.jobs.dto.SendMsgDTO;
import com.jobs.dto.ScheduledMessageDTO;
import com.jobs.dto.ScheduledTaskStatusDTO;
import com.jobs.service.ScheduledMessageService;
import com.jobs.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private ScheduledMessageService scheduledMessageService;

    //通过后端代码，向指定用户发送信息，测试指定用户的前端页面是否可以收到信息
    @PostMapping("/sendmsg")
    public String send(@RequestBody SendMsgDTO sendMsgDTO) throws IOException {
        webSocketServer.sendMessage(sendMsgDTO.getUsername(), sendMsgDTO.getMsg());
        return "send success";
    }

    /**
     * 手动触发定时推送消息
     */
    @PostMapping("/sendScheduledMessage")
    public String sendScheduledMessage(@RequestBody ScheduledMessageDTO scheduledMessageDTO) {
        try {
            scheduledMessageService.sendManualMessage(scheduledMessageDTO.getMessage());
            return "定时推送消息发送成功";
        } catch (Exception e) {
            return "定时推送消息发送失败：" + e.getMessage();
        }
    }

    /**
     * 获取当前在线用户信息
     */
    @GetMapping("/onlineUsers")
    public Object getOnlineUsers() {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        String[] onlineUsers = WebSocketServer.getOnlineUsers();
        
        return new Object() {
            public final int count = onlineCount;
            public final String[] users = onlineUsers;
            public final String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        };
    }

    /**
     * 立即触发定时推送（测试用）
     */
    @PostMapping("/triggerScheduled")
    public String triggerScheduled() {
        try {
            scheduledMessageService.sendScheduledMessage();
            return "定时推送已触发";
        } catch (Exception e) {
            return "定时推送触发失败：" + e.getMessage();
        }
    }

    /**
     * 手动启动定时任务
     */
    @PostMapping("/startScheduledTask")
    public String startScheduledTask() {
        try {
            scheduledMessageService.startScheduledTask();
            return "定时任务已启动";
        } catch (Exception e) {
            return "定时任务启动失败：" + e.getMessage();
        }
    }

    /**
     * 手动停止定时任务
     */
    @PostMapping("/stopScheduledTask")
    public String stopScheduledTask() {
        try {
            scheduledMessageService.stopScheduledTask();
            return "定时任务已停止";
        } catch (Exception e) {
            return "定时任务停止失败：" + e.getMessage();
        }
    }

    /**
     * 检查定时任务状态
     */
    @GetMapping("/scheduledTaskStatus")
    public ScheduledTaskStatusDTO getScheduledTaskStatus() {
        int onlineCount = WebSocketServer.getOnlineUserCount();
        String[] onlineUsers = WebSocketServer.getOnlineUsers();
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String message = "定时任务现在由系统自动管理，当有用户连接时自动启动，无用户时自动停止";
        
        return new ScheduledTaskStatusDTO(onlineCount, onlineUsers, currentTime, message);
    }
    
    // 用户级定时任务管理API
    
    /**
     * 启动当前用户的定时任务
     */
    @PostMapping("/startUserScheduledTask")
    public String startUserScheduledTask(@RequestParam String username) {
        try {
            scheduledMessageService.startUserScheduledTask(username);
            return "用户 " + username + " 的定时任务已启动";
        } catch (Exception e) {
            return "启动用户定时任务失败：" + e.getMessage();
        }
    }
    
    /**
     * 停止当前用户的定时任务
     */
    @PostMapping("/stopUserScheduledTask")
    public String stopUserScheduledTask(@RequestParam String username) {
        try {
            scheduledMessageService.stopUserScheduledTask(username);
            return "用户 " + username + " 的定时任务已停止";
        } catch (Exception e) {
            return "停止用户定时任务失败：" + e.getMessage();
        }
    }
    
    /**
     * 获取当前用户的定时任务状态
     */
    @GetMapping("/userScheduledTaskStatus")
    public Object getUserScheduledTaskStatus(@RequestParam String username) {
        try {
            return scheduledMessageService.getUserScheduledTaskInfo(username);
        } catch (Exception e) {
            return new Object() {
                public final String error = "获取用户定时任务状态失败：" + e.getMessage();
            };
        }
    }
    
    /**
     * 向指定用户发送定时消息
     */
    @PostMapping("/sendScheduledMessageToUser")
    public String sendScheduledMessageToUser(@RequestParam String username, @RequestParam String message) {
        try {
            scheduledMessageService.sendScheduledMessageToUser(username, message);
            return "定时消息已发送给用户 " + username;
        } catch (Exception e) {
            return "发送定时消息失败：" + e.getMessage();
        }
    }
}
