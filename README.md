# LockTodo - 锁屏待办事项助手

一款基于 Android 平台的任务管理应用，支持在桌面小组件上直接查看和操作待办事项。

## ✨ 功能特性

- 📱 **桌面小组件**：无需打开APP，即可在桌面完成任务的查看、打勾等操作
- 🔄 **双向同步**：桌面与APP数据实时同步，确保信息一致性
- 🔝 **智能排序**：已完成任务自动置底，紧急任务优先置顶
- 🔴 **紧急标记**：紧急任务前显示红色"!"符号，一目了然
- 🌍 **多语言支持**：支持中英文切换

## 🛠️ 技术栈

- **语言**：Kotlin
- **框架**：Android Jetpack (Compose, ViewModel, Room)
- **UI**：Material Design 3
- **架构**：MVVM + Repository 模式

## 📦 安装

1. 克隆仓库：
   ```bash
   git clone https://github.com/nijia17/LockTodo.git
   ```

2. 使用 Android Studio 打开项目

3. 构建并运行到设备或模拟器

## 📱 使用说明

### 添加任务
1. 打开 LockTodo APP
2. 点击底部输入框输入任务内容
3. 点击"紧急"按钮标记为紧急任务
4. 点击"添加"按钮

### 完成任务
- **在APP中**：点击任务左侧的圆形复选框
- **在桌面小组件中**：直接点击任务项即可切换完成状态

### 切换语言
1. 打开 APP
2. 点击右上角齿轮图标
3. 选择中文或 English

## 📷 截图

| 主界面 | 桌面小组件 |
|--------|-----------|
| ![Main Screen](https://via.placeholder.com/300x600) | ![Widget](https://via.placeholder.com/300x200) |

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！