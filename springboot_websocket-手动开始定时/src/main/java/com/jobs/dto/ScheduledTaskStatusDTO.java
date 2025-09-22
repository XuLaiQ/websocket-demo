package com.jobs.dto;

/**
 * 定时任务状态DTO
 * @Author: xulai
 * @Date: 2025/1/27
 */
public class ScheduledTaskStatusDTO {
    private int onlineUserCount;
    private String[] onlineUsers;
    private String currentTime;
    private String message;

    public ScheduledTaskStatusDTO() {}

    public ScheduledTaskStatusDTO(int onlineUserCount, String[] onlineUsers, String currentTime, String message) {
        this.onlineUserCount = onlineUserCount;
        this.onlineUsers = onlineUsers;
        this.currentTime = currentTime;
        this.message = message;
    }

    public int getOnlineUserCount() {
        return onlineUserCount;
    }

    public void setOnlineUserCount(int onlineUserCount) {
        this.onlineUserCount = onlineUserCount;
    }

    public String[] getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(String[] onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
