# Spring Boot WebSocket 定时推送功能

## 功能概述

在原有Spring Boot WebSocket项目基础上，新增了定时推送消息功能，支持自动定时推送和手动触发推送。

## 新增功能

### 1. 自动定时推送
- **每30秒推送**：自动向所有在线用户推送当前时间和在线用户数
- **早安推送**：每天上午9点推送早安消息
- **下班提醒**：每天下午6点推送下班提醒

### 2. 手动推送功能
- 支持手动触发定时推送
- 支持发送自定义消息给所有在线用户
- 支持查看当前在线用户信息

## 新增文件

### 后端文件
1. `ScheduledMessageService.java` - 定时任务服务类
2. `ScheduledConfig.java` - 定时任务配置类
3. `ScheduledMessageDTO.java` - 定时推送消息DTO

### 修改文件
1. `WebSocketServer.java` - 添加静态方法支持定时任务
2. `TestController.java` - 添加定时推送相关接口
3. `index.html` - 更新前端页面，添加定时推送演示功能

## API接口

### 1. 手动触发定时推送
```
POST /test/triggerScheduled
```
立即触发定时推送功能

### 2. 发送自定义定时消息
```
POST /test/sendScheduledMessage
Content-Type: application/json

{
    "message": "自定义消息内容",
    "type": "MANUAL"
}
```

### 3. 获取在线用户信息
```
GET /test/onlineUsers
```
返回当前在线用户数量和用户名列表

### 4. 原有接口
```
POST /test/sendmsg
Content-Type: application/json

{
    "username": "用户名",
    "msg": "消息内容"
}
```

## 使用方法

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 访问测试页面
打开浏览器访问：`http://localhost:8086/index.html?username=你的用户名`

### 3. 测试定时推送
- 页面会自动连接到WebSocket
- 每30秒会自动收到定时推送消息
- 可以点击"立即触发定时推送"按钮测试
- 可以发送自定义定时消息

## 配置说明

### 定时推送时间配置
在 `ScheduledMessageService.java` 中可以修改定时推送的时间：

```java
// 每30秒推送一次（可修改）
@Scheduled(fixedRate = 30000)

// 每天上午9点推送（可修改）
@Scheduled(cron = "0 0 9 * * ?")

// 每天下午6点推送（可修改）
@Scheduled(cron = "0 0 18 * * ?")
```

### Cron表达式说明
- `0 0 9 * * ?` - 每天上午9点
- `0 0 18 * * ?` - 每天下午6点
- `0 */5 * * * ?` - 每5分钟
- `0 0 0 1 * ?` - 每月1号凌晨

## 技术特点

1. **线程安全**：使用静态方法确保多线程环境下的安全性
2. **异常处理**：完善的异常处理机制，避免单个用户连接异常影响整体推送
3. **日志记录**：详细的操作日志，便于调试和监控
4. **灵活配置**：支持多种定时推送策略
5. **用户友好**：提供直观的前端界面进行测试和演示

## 注意事项

1. 定时推送只对当前在线的用户有效
2. 如果用户连接断开，推送消息会失败但不会影响其他用户
3. 可以通过日志查看推送状态和错误信息
4. 建议在生产环境中根据实际需求调整推送频率
