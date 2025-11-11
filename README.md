# 注：该MOD目前在开发中，大部分功能暂时缺失！

众所周知，现有的AI大多无法操控我们的电脑玩游戏，那么有没办法打破这一障碍呢？  
`LynxMind`或许可以实现这一愿望，它能够让LLM间接操控我们的Minecraft角色完成玩家所给的任务！  

# LynxMind

LynxMind 是一个基于 Fabric 框架开发的 Minecraft 模组，专注利用特定通信协议让AI返回指定Json内容，   
通过反序列化返回的内容即可调用Baritone API的相关功能，从而实现对MC角色的“控制”。


## 项目信息

- **模组ID**：lynx_mind
- **版本**：beta
- **支持的 Minecraft 版本**：1.20.4（定义于 gradle.properties）

## 功能特点

- **AI 服务集成**：通过 `AIServiceManager` 利用特定协议实现与 AI 服务的交互。（需自建AI API服务）

## 依赖项

- **核心依赖**：
  - Baritone API（fabric 适配版本，通过本地 libs 目录引入）

## 构建与运行

### 构建项目
```bash
./gradlew build
```
构建完成后，模组 JAR 文件位于 `build/libs` 目录下
