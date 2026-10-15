# 个人记账本系统（Full-Stack Expense Tracker）

一个基于 SpringBoot + Vue3 的个人记账本全栈项目，支持多账本管理、收支记录、分类统计与可视化报表。

## 技术栈

**后端：**
- Java 17
- SpringBoot 4.x
- MyBatis-Plus
- MySQL 8.0
- Redis
- JWT（登录鉴权）
- Spring AOP（统一日志 + 全局异常处理）
- Nginx（部署 + 反向代理）

**前端：**
- react
- ant-design
- Axios
- ECharts
- Pinia

## 功能列表

- [x] 用户注册 / 登录（JWT 鉴权）
- [x] 多账本管理
- [x] 账单增删改查（按时间、分类筛选）
- [x] 分页查询
- [x] 统一异常处理与返回格式
- [x] Redis 缓存分类字典与高频统计数据
- [x] Nginx 部署前端静态资源 + 反向代理后端接口

