# quiz_online 在线答题平台

本项目是一个在线答题考试系统，起初用来给校内有奖答题活动使用，后逐步完善改良，并用于社团招新考试。

## 功能特性

### 考生功能
- 自主注册与登录
- 随机抽题生成个性化试卷（一人一卷）
- 限时答题，每道题后端计时
- 断点续答，支持意外退出后恢复进度
- 查看成绩与排名
- 查看题目解析及其他考生答卷
- 单终端登录限制，禁止多端同时答题

### 管理员功能
- 题库管理
- 考试开放与关闭控制（通过 Redis 动态配置）
- 查看考生敏感信息（如联系方式、部门等）
- 成绩统计与排行展示

### 系统安全与扩展性
- 权限隔离（基于 Spring Security 的角色控制）
- 防暴力破解（基于 IP 的登录/注册限流）
- 防 CSRF 攻击与 SQL 注入防护
- 邀请码注册机制
- 数据持久化
- 项目结构清晰，模块化设计，易于维护与扩展

## 技术栈

- **后端**：Spring Boot 3.x + Spring Security + Spring Data JPA
- **前端**：Thymeleaf + HTML/CSS/JavaScript
- **数据库**：MySQL + Redis
- **安全**：BCrypt 密码加密、CSRF 防护、IP 限流
- **工具**：Maven、Lombok、Gson

## 部署与运行

### 配置步骤
1. 克隆项目到本地
2. 在 `src/main/resources/` 目录下配置 `application.yml`，设置数据库和 Redis 连接信息
3. 创建数据库
4. 在项目根目录下创建 `passcode.txt` 和 `admin.txt`，分别存放注册邀请码和管理员账号列表
5. 在 `src/main/resource/data` 目录里配置题库，并修改 `src/main/java/asia/chatclient/quiz/loader/QuestionDataLoader.java` 和 `src/main/java/asia/chatclient/quiz/loader/QuestionDataLoader.java` 中的题库信息、抽题比例、每题限时。
6. 修改页面中的答题信息
7. 启动项目
8. 设置redis的`quiz_online:enabled`为true开放答题（设置为false则关闭答题）

## 测试情况

本系统已经用于真实社团招生考试与校内答题活动两次，圆满的完成了任务。
系统已完成基本功能测试，包括：
- 用户注册与登录流程
- 随机抽题与限时答题
- 断点续答与单终端限制
- 管理员权限控制
- 防暴力破解与防重复提交

## 联系
https://github.com/swsaf55555

如有问题或建议，欢迎通过 GitHub Issues 反馈。