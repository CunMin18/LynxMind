# LynxMind

LynxMind 是一个基于 Fabric 框架开发的 Minecraft 模组，专注于提供智能化的游戏辅助功能，提升玩家的游戏体验。


## 项目信息

- **模组ID**：lynxmind
- **版本**：${version}（由构建系统动态生成）
- **支持的 Minecraft 版本**：${minecraft_version}（定义于 gradle.properties）
- **许可证**：All-Rights-Reserved
  - 版权声明：Copyright (c) 2025，保留所有权利。未经授权不得擅自复制、修改、分发或用于商业用途等。


## 功能特点

- **AI 服务集成**：通过 `AIServiceManager` 实现与外部 AI 服务的交互，支持消息发送与异步回复处理
- **配置管理系统**：
  - 由 `ConfigManager` 负责配置文件的加载、保存，支持 YAML 格式，自动创建父目录
  - `AIServiceConfig` 专门管理 AI 服务相关配置（如 API 地址、令牌等），提供 `save()` 方法快速保存配置
- **数据生成支持**：实现 `LynxMindDataGenerator` 集成 Fabric 数据生成系统，用于处理模组数据生成逻辑
- **模块化入口**：基于 Fabric 入口点机制，分别实现客户端（`LynxMindClient`）与服务端（`LynxMind`）初始化逻辑


## 开发环境

- **Java 版本**：
  - 编译目标版本：JDK 17
  - IDE 推荐版本：JDK 21（由 .idea/misc.xml 配置指定）
- **构建工具**：Gradle
- **依赖管理**：Fabric Loom 1.13-SNAPSHOT
- **开发工具**：IntelliJ IDEA（项目包含完整 IDE 配置文件）


## 依赖项

- **核心依赖**：
  - Minecraft: ${minecraft_version}
  - Yarn Mappings: ${yarn_mappings}
  - Fabric Loader: >=${loader_version}
  - Fabric API: ${fabric_version}
- **第三方库**：
  - Baritone API（fabric 适配版本，通过本地 libs 目录引入）
  - SnakeYAML 2.2（配置文件处理）
  - Jackson Databind 2.15.3（JSON 处理）
  - Nether Pathfinder 1.4.1
  - Lombok 1.18.42（注解处理，需启用注解处理器）


## 项目结构

- **主代码目录**：`src/main/java/org/ricey_yam/lynxmind`
  - 服务端主类：`LynxMind`（实现 `ModInitializer`）
- **客户端代码目录**：`src/client/java/org/ricey_yam/lynxmind/client`
  - `ai`：AI 服务管理与交互逻辑
  - `config`：配置管理类（`ConfigManager`、`AIServiceConfig` 等）
  - 客户端主类：`LynxMindClient`
  - 数据生成类：`LynxMindDataGenerator`（实现 `DataGeneratorEntrypoint`）
- **资源目录**：
  - `src/main/resources`：包含模组描述文件 {insert\_element\_0\_YGZhYnJpYy5tb2QuanNvbmA=}、主 mixin 配置等
  - 客户端专用资源：包含客户端 mixin 配置 {insert\_element\_1\_YGx5bnhtaW5kLmNsaWVudC5taXhpbnMuanNvbmA=}


## 构建与运行

### 构建项目
```bash
./gradlew build
```
构建完成后，模组 JAR 文件位于 `build/libs` 目录下

2. **运行调试**：
   - 项目包含 IntelliJ IDEA 预设运行配置：
     - `Minecraft Client`：启动客户端调试环境
     - `Minecraft Server`：启动服务端调试环境
     - `Data Generation`：运行数据生成任务，输出目录为 `src/main/generated`


## 配置文件说明

- 由 `ConfigManager` 自动管理，支持 YAML 格式的序列化与反序列化
- 保存逻辑：通过 `saveConfig` 方法实现，自动处理父目录创建与 UTF-8 编码
- 路径：默认存储于 Fabric 配置目录下的 `lynxmind` 子目录
- AI 服务配置：可通过 `AIServiceConfig.save()` 方法快速保存相关配置


## 入口点配置

Fabric 入口点定义于 {insert\_element\_0\_YGZhYnJpYy5tb2QuanNvbmA=}：
- `main`：`org.ricey_yam.lynxmind.LynxMind`（服务端初始化）
- `client`：`org.ricey_yam.lynxmind.client.LynxMindClient`（客户端初始化）
- `fabric-datagen`：`org.ricey_yam.lynxmind.client.LynxMindDataGenerator`（数据生成）
