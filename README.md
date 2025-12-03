# 📱 Android LAN Chat System (局域网聊天系统)

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Architecture](https://img.shields.io/badge/Architecture-C%2FS-blue.svg)
![Protocol](https://img.shields.io/badge/Protocol-TCP%20Socket-red.svg)

基于 Java 原生 Socket (BIO) 和 Android 实现的简易局域网即时通讯系统。本项目涵盖了 Android 客户端 UI 开发、SQLite 数据库存储、服务端网络编程以及 JSON 通信协议设计。

> 这是一个全栈开发的练手项目，实现了真机与模拟器、真机与真机在同一 WiFi 下的即时通信。

---

## ✨ 项目演示 (Demo)

| 聊天界面 | 表情输入 | 服务端日志 |
| :---: | :---: | :---: |
| <img src="screenshot/chat_main.jpg" width="200"/> | <img src="screenshot/emoji_panel.jpg" width="200"/> | <img src="screenshot/server_log.png" width="200"/> |
*(注：请将你的运行截图放入项目 screenshot 文件夹中)*

---

## 🛠 功能特性 (Features)

* **即时通讯**: 基于 TCP 长连接，实现低延迟的消息收发。
* **多端互联**: 支持 Android 真机、模拟器与 PC 服务端互通。
* **消息持久化**: 采用 SQLite 数据库存储聊天记录，重启应用不丢失数据。
* **富文本支持**: 支持文本消息发送，内置简易 Emoji 表情面板。
* **UI 交互**: 
    * 使用 `RecyclerView` 展示消息列表。
    * 区分“发送方”与“接收方”的气泡布局。
    * 实时时间戳显示。

---

## 🏗 技术栈 (Tech Stack)

### Client (Android)
* **开发语言**: Java
* **UI 组件**: RecyclerView, Adapter, LinearLayout
* **数据存储**: SQLite (SQLiteOpenHelper)
* **网络通信**: `java.net.Socket`, `Thread` (子线程网络请求), `Handler` (UI 更新)
* **数据格式**: JSON (`org.json`)

### Server (Java PC)
* **核心架构**: Java SE (BIO 模型)
* **并发处理**: `ExecutorService` (线程池处理多客户端连接)
* **消息分发**: 维护 `ClientHandler` 列表实现消息广播

---

## 📝 通信协议 (Protocol)

系统使用 **JSON** 格式进行数据交互，具备良好的扩展性。

**消息模型 (ChatMessage):**

```json
{
  "sender": "User_171695",   // 发送者 ID
  "content": "你好，这是一条测试消息！", // 消息内容
  "time": "10:30",           // 发送时间
  "type": 1                  // (本地字段) 1:发送, 0:接收
}
---

🚀 快速开始 (Getting Started)
1. 环境准备
Android Studio Ladybug 或更高版本。

JDK 1.8+。

一部 Android 手机（需开启开发者模式）或 Android 模拟器。

关键: 手机和电脑必须连接同一个 WiFi（或手机开热点给电脑连）。

2. 启动服务端
找到 server 模块下的 ChatServer.java。

运行 main 方法。

控制台看到 >>> 聊天服务器启动，端口: 8888 即表示成功。

注意：如果 Windows 防火墙弹窗，请务必勾选“允许访问”。

3. 配置与运行客户端
打开 ChatActivity.java。

找到 getServerIp() 方法，将 else 分支的 IP 修改为你电脑当前的 IPv4 地址：

Java

return "192.168.x.x"; // <--- 修改这里
连接手机或启动模拟器，点击 Run 'app'。

📂 项目结构 (Structure)
Plaintext

ChatSystem
├── app (Android Client)
│   ├── src/main/java/com/example/chatsystem
│   │   ├── ChatActivity.java       // 主界面交互逻辑、网络连接
│   │   ├── ChatAdapter.java        // 消息列表适配器
│   │   ├── ChatMessage.java        // 消息实体类
│   │   └── DatabaseHelper.java     // 数据库操作类
│   └── src/main/res/layout
│       ├── activity_chat.xml       // 聊天主布局
│       └── item_message.xml        // 消息气泡布局
└── server (Java Library)
    └── src/main/java/com/example/chatsystem
        └── ChatServer.java         // 服务端核心代码
❓ 常见问题 (Troubleshooting)
Q: 模拟器能发消息，真机连不上？

检查电脑防火墙是否拦截了 8888 端口。尝试关闭防火墙测试。

确保真机和电脑在同一网段（推荐使用手机热点）。

检查代码中 getServerIp() 的真机 IP 分支是否填对。

Q: App 启动后报错 Value must be ≥ 0？

这是因为旧的数据库结构与新代码不匹配。请卸载手机上的 App，然后重新运行安装。

Q: Gradle 构建一直转圈？

请确保不要把 Server 和 App 放在同一个 Build 任务里。先独立运行 Server，再运行 App。

🔮 后期优化思路 (Roadmap)
[ ] 心跳机制: 增加 Heartbeat 包，实现断线自动重连。

[ ] 用户系统: 增加登录注册、头像上传功能。

[ ] 多媒体传输: 支持图片、语音发送 (需引入文件服务器)。

[ ] 加密通信: 将 Socket 升级为 SSLSocket，保障局域网通信安全。

[ ] NIO 升级: 服务端改用 Netty 框架，支持更高并发。
