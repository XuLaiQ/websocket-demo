package com.jobs.websocket;

import com.jobs.service.ScheduledMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置 WebSocket 服务的连接地址，每次连接都会实例化一个对象
 * @Author: xulai
 * @Date: 2025/1/27
 */
@ServerEndpoint(value = "/socket/{username}")
@Slf4j
@Component
public class WebSocketServer {

    //存储 username 与 Session 的对应关系，key 是 username
    private static Map<String, Session> sessionMap = new HashMap<String, Session>();

    //存储 Session 的 id 和 username 的对应关系
    private static Map<String, String> idnameMap = new HashMap<String, String>();
    
    // 存储每个用户的定时任务状态，key 是 username
    private static Map<String, Boolean> userScheduledTaskStatus = new HashMap<String, Boolean>();
    
    // 注入定时消息服务
    private static ScheduledMessageService scheduledMessageService;
    
    @Autowired
    public void setScheduledMessageService(ScheduledMessageService scheduledMessageService) {
        WebSocketServer.scheduledMessageService = scheduledMessageService;
    }


    //WebSocket 连接建立后调用该方法
    //注意：当前 Socket Session 属于长连接类型（有状态），因此不能持久化对象到数据库中
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        //根据 username 获取 Socket Session，如果一个用户重复连接，只保留该用户最后连接的 Socket Session
        Session userSession = sessionMap.get(username);
        if (userSession != null) {
            idnameMap.remove(userSession.getId());
            sessionMap.remove(username);
        }

        //存储 username 和 Socket Session 的对应关系
        sessionMap.put(username, session);
        //存储 Socket Session 的 ID 和 username 的对应关系
        idnameMap.put(session.getId(), username);
        
        log.info("用户 {} 已连接，当前在线用户数：{}", username, sessionMap.size());
        
        // 检查并管理定时任务
        if (scheduledMessageService != null) {
            scheduledMessageService.checkAndManageScheduledTask();
        }
    }

    //关闭链接
    @OnClose
    public void onClose(Session session) {
        //根据 Scocket Session 的 ID 获取 username
        String username = idnameMap.get(session.getId());
        //移除用户信息
        sessionMap.remove(username);
        idnameMap.remove(session.getId());
        
        log.info("用户 {} 已断开连接，当前在线用户数：{}", username, sessionMap.size());
        
        // 检查并管理定时任务
        if (scheduledMessageService != null) {
            scheduledMessageService.checkAndManageScheduledTask();
        }
    }

    //异常处理
    @OnError
    public void onError(Session session, Throwable throwable) {
        String username = idnameMap.get(session.getId());
        log.error("用户 " + username + " 的 WebSocket 通信发生了异常：" + throwable.getMessage());
    }

    //接收客户端发送过来的消息
    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String username = idnameMap.get(session.getId());
        log.info("用户 " + username + " 接收到客户端发来的消息是：" + message);
        //同步给客户端发送消息
        session.getBasicRemote().sendText("服务端收到消息：" + message);
        //异步给客户端发送消息
        //session.getAsyncRemote().sendText("收到的消息：" + message);
    }


    //封住的消息发送方法，用于其它地方的服务端代码进行调用，给客户端发送消息
    public void sendMessage(String username, String message) throws IOException {
        //获取用户的 Socket Session 对象
        Session session = sessionMap.get(username);

        if (session != null) {
            //给指定会话发送消息
            session.getBasicRemote().sendText(message);
        }
    }

    //静态方法，用于定时任务调用，向所有在线用户发送消息
    public static void sendMessageToAll(String message) {
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            try {
                Session session = entry.getValue();
                if (session != null && session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                    log.info("定时消息已发送给用户: {}", entry.getKey());
                }
            } catch (IOException e) {
                log.error("向用户 {} 发送定时消息失败: {}", entry.getKey(), e.getMessage());
            }
        }
    }

    //静态方法，获取当前在线用户数量
    public static int getOnlineUserCount() {
        return sessionMap.size();
    }

    //静态方法，获取所有在线用户名列表
    public static String[] getOnlineUsers() {
        return sessionMap.keySet().toArray(new String[0]);
    }
    
    // 用户级定时任务管理方法
    
    /**
     * 启动指定用户的定时任务
     */
    public static void startUserScheduledTask(String username) {
        if (sessionMap.containsKey(username)) {
            userScheduledTaskStatus.put(username, true);
            log.info("用户 {} 的定时任务已启动", username);
        } else {
            log.warn("用户 {} 不在线，无法启动定时任务", username);
        }
    }
    
    /**
     * 停止指定用户的定时任务
     */
    public static void stopUserScheduledTask(String username) {
        userScheduledTaskStatus.put(username, false);
        log.info("用户 {} 的定时任务已停止", username);
    }
    
    /**
     * 获取指定用户的定时任务状态
     */
    public static boolean getUserScheduledTaskStatus(String username) {
        return userScheduledTaskStatus.getOrDefault(username, false);
    }
    
    /**
     * 向指定用户发送定时消息
     */
    public static void sendScheduledMessageToUser(String username, String message) {
        Session session = sessionMap.get(username);
        if (session != null && session.isOpen() && getUserScheduledTaskStatus(username)) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("定时消息已发送给用户: {}", username);
            } catch (IOException e) {
                log.error("向用户 {} 发送定时消息失败: {}", username, e.getMessage());
            }
        }
    }
    
    /**
     * 向所有启用了定时任务的用户发送消息
     */
    public static void sendMessageToScheduledUsers(String message) {
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            String username = entry.getKey();
            if (getUserScheduledTaskStatus(username)) {
                sendScheduledMessageToUser(username, message);
            }
        }
    }
    
    /**
     * 获取启用了定时任务的用户数量
     */
    public static int getScheduledUserCount() {
        return (int) userScheduledTaskStatus.values().stream().filter(status -> status).count();
    }
    
    /**
     * 获取启用了定时任务的用户名列表
     */
    public static String[] getScheduledUsers() {
        return userScheduledTaskStatus.entrySet().stream()
                .filter(entry -> entry.getValue() && sessionMap.containsKey(entry.getKey()))
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }
}
