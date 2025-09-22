package com.jobs.controller;

import com.jobs.dto.SendMsgDTO;
import com.jobs.dto.ScheduledMessageDTO;
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
}
