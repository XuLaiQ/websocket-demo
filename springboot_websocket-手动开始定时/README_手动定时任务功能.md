# 手动定时任务功能说明

## 功能概述

该项目已成功修改为手动触发模式，定时任务现在会根据用户连接状态自动管理：

- **有用户连接时**：自动启动定时推送任务
- **无用户连接时**：自动停止定时推送任务

## 主要修改

### 1. ScheduledMessageService.java
- 移除了 `@Scheduled` 注解
- 添加了手动任务调度器 `TaskScheduler`
- 新增方法：
  - `startScheduledTask()`: 启动定时任务
  - `stopScheduledTask()`: 停止定时任务
  - `checkAndManageScheduledTask()`: 检查并管理任务状态

### 2. WebSocketServer.java
- 在 `onOpen()` 方法中添加了定时任务检查
- 在 `onClose()` 方法中添加了定时任务检查
- 当用户连接/断开时自动管理定时任务状态

### 3. ScheduledConfig.java
- 移除了 `@EnableScheduling` 注解
- 改为手动控制模式

### 4. TestController.java
- 新增接口：
  - `POST /test/startScheduledTask`: 手动启动定时任务
  - `POST /test/stopScheduledTask`: 手动停止定时任务
  - `GET /test/scheduledTaskStatus`: 查看定时任务状态

### 5. 新增 ScheduledTaskStatusDTO.java
- 用于返回定时任务状态信息

## 测试方法

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 测试WebSocket连接
1. 打开浏览器访问 `http://localhost:8080`
2. 输入用户名连接WebSocket
3. 观察控制台日志，应该看到定时任务自动启动

### 3. 测试API接口

#### 查看定时任务状态
```bash
curl -X GET http://localhost:8080/test/scheduledTaskStatus
```

#### 手动启动定时任务
```bash
curl -X POST http://localhost:8080/test/startScheduledTask
```

#### 手动停止定时任务
```bash
curl -X POST http://localhost:8080/test/stopScheduledTask
```

#### 查看在线用户
```bash
curl -X GET http://localhost:8080/test/onlineUsers
```

### 4. 测试自动管理功能
1. 连接WebSocket用户 → 定时任务自动启动
2. 断开所有WebSocket连接 → 定时任务自动停止
3. 重新连接用户 → 定时任务重新启动

## 工作流程

1. **用户连接时**：
   - WebSocket `onOpen()` 被调用
   - 检查在线用户数量
   - 如果有用户且定时任务未启动，则启动定时任务

2. **用户断开时**：
   - WebSocket `onClose()` 被调用
   - 检查在线用户数量
   - 如果无用户且定时任务在运行，则停止定时任务

3. **定时推送**：
   - 每30秒执行一次
   - 检查在线用户数量
   - 有用户则推送消息，无用户则停止任务

## 优势

- **资源节约**：无用户时不运行定时任务，节省系统资源
- **自动管理**：无需手动干预，系统自动根据用户状态管理任务
- **灵活控制**：仍保留手动启动/停止接口，便于测试和特殊情况处理
- **实时响应**：用户连接/断开时立即响应，无需等待

## 注意事项

- 定时任务现在完全由系统自动管理
- 如果需要在无用户时也运行定时任务，可以调用手动启动接口
- 所有原有的手动推送功能保持不变
