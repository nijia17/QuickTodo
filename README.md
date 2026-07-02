# QuickTodo - 极简桌面待办助手

> 一款专注于桌面体验的轻量级待办事项应用，集成 AI 智能分析，让任务管理变得简单高效

## 解决的痛点

针对**忙碌的职场人士和学生群体**，解决以下核心问题：
- 打开APP才能管理任务，操作繁琐
- 紧急任务容易被忽略
- 任务多了不知道先做哪个
- 界面复杂，干扰专注

## 核心功能

| 功能 | 说明 |
|------|------|
| **桌面小组件** | 无需打开APP，直接在桌面查看、添加、完成任务 |
| **AI 智能分析** | 根据任务内容、截止时间、当前工作量自动评估优先级，提供处理建议 |
| **AI 智能排序** | 自动按优先级和截止时间排序任务列表 |
| **优先级管理** | 支持高/中/低三级优先级 + 紧急标记，重要任务一目了然 |
| **任务分类** | 学习、工作、生活、其他四大分类，清晰归类 |
| **截止时间** | 支持快捷选择（10分钟/30分钟/1小时/今晚）或自定义时间 |
| **重复任务** | 支持每日、工作日、每周重复，自动循环生成 |
| **到期提醒** | 根据任务优先级和截止时间智能推送通知提醒 |
| **统计面板** | 展示当日完成率、连续完成天数，附带 AI 洞察分析 |
| **快速添加** | 快捷输入，一键添加任务 |

## 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose + Material Design 3
- **架构**：MVVM + Repository 模式
- **本地存储**：Room 数据库 (KSP)
- **异步处理**：Kotlin Coroutines + WorkManager
- **生命周期**：Lifecycle ViewModel (Compose)
- **最低支持**：Android 7.0 (API 24)
- **目标版本**：Android 14 (API 34)

## 快速开始

```bash
# 克隆仓库
git clone https://github.com/nijia17/QuickTodo.git

# 使用 Android Studio 打开项目
# 构建并运行到设备或模拟器
```

## 使用说明

### 添加任务
1. 点击桌面小组件或APP内的 **+** 按钮
2. 输入任务内容
3. 选择分类（学习/工作/生活/其他）
4. 设置截止时间（可选）
5. 设置重复类型（可选）
6. AI 会自动分析并推荐优先级
7. 点击「保存」完成添加

### 完成任务
- **桌面小组件**：直接点击任务项即可切换完成状态
- **APP 内**：点击任务左侧圆形复选框

### 查看统计
1. 打开 APP，点击已完成统计图标
2. 查看当日完成率、连续完成天数
3. 阅读 AI 洞察建议

### 设置
1. 打开 APP，点击右上角设置图标
2. 可开启/关闭：桌面小组件、AI 排序、到期提醒


## 项目结构

```
app/src/main/java/com/example/quicktodo/
├── Task.kt                  # 任务实体（Room）
├── TaskDao.kt               # 数据访问层
├── TaskRepo.kt              # Repository 层
├── TaskViewModel.kt         # ViewModel 层
├── AppDatabase.kt           # Room 数据库配置
├── MainActivity.kt          # 主界面（任务列表）
├── AddTaskActivity.kt       # 添加/编辑任务
├── QuickAddActivity.kt      # 快速添加
├── QuickTodoWidget.kt       # 桌面小组件
├── TaskWidgetService.kt     # 小组件数据服务
├── TaskWidgetFactory.kt     # 小组件视图工厂
├── AIPriorityAnalyzer.kt    # AI 优先级分析引擎
├── AIAnalyzeActivity.kt     # AI 分析界面
├── StatisticsActivity.kt    # 统计面板
├── SettingsActivity.kt      # 设置页面
├── AlarmScheduler.kt        # 提醒调度器
├── AlarmReceiver.kt         # 提醒广播接收器
├── NotificationHelper.kt    # 通知管理
├── RecurringTaskHelper.kt   # 重复任务处理
└── TimePickerHelper.kt      # 时间选择器
```

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！