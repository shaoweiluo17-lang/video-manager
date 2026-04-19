# 视频图片文件管理系统 (Video & Image Manager)

> 一个类抖音体验的视频+图片文件管理 Web 系统，支持 NAS 多目录分散管理

## 📖 项目简介

本系统旨在帮助用户管理分布在多个硬盘/文件夹中的视频和图片文件，提供类抖音的沉浸式视频播放体验，支持图片网格/列表浏览、键盘快捷操作标记/删除，实现批量清理和去重功能。

### 🎯 核心功能

#### 视频管理
- ✅ **类抖音播放** - 上下键切换视频，快进快退
- ✅ **快捷键操作** - X 标记不喜欢，D 删除视频
- ✅ **批量清理** - 一键删除不喜欢视频
- ✅ **文件去重** - 基于 MD5 哈希识别重复视频

#### 图片管理
- ✅ **网格浏览** - 缩略图网格展示
- ✅ **列表浏览** - 支持保留文件夹层级或平铺显示
- ✅ **点击放大** - 图片预览，支持左右切换
- ✅ **缩略图** - 后端自动生成缩略图
- ✅ **批量清理** - 标记不喜欢，批量删除
- ✅ **图片去重** - 基于 MD5 哈希识别重复图片

#### 系统功能
- ✅ **用户认证** - 支持多用户登录
- ✅ **多目录管理** - 视频/图片路径分开配置，支持多目录
- ✅ **数据库** - MySQL 存储，支持大数据量
- ✅ **一键部署** - 部署脚本，开机自启动

---

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Vue 3 + Vite + Element Plus |
| **视频播放** | video.js |
| **后端** | Java 17 + Spring Boot 3 |
| **ORM** | MyBatis-Plus |
| **数据库** | MySQL 8.0 |
| **缩略图** | Thumbnailator |
| **认证** | JWT |
| **部署** | Shell + Python |

---

## 🚀 快速开始

### 方式一：本地开发

```bash
# 克隆代码
git clone https://github.com/shaoweiluo17-lang/video-manager.git
cd video-manager

# 1. 启动 MySQL，创建数据库
mysql -u root -p < backend/src/main/resources/schema-mysql.sql

# 2. 启动后端
cd backend
mvn clean package -DskipTests
java -jar target/video-manager-1.0.0.jar

# 3. 启动前端（新终端）
cd frontend
npm install
npm run dev
```

访问 `http://localhost:3000`

### 方式二：部署到 NAS

详细部署步骤请参考 [DEPLOYMENT.md](DEPLOYMENT.md)

---

## 📁 项目结构

```
video-manager/
├── backend/                      # Java Spring Boot 后端
│   ├── src/main/java/com/videomanager/
│   │   ├── config/              # 配置类
│   │   ├── controller/           # REST API
│   │   ├── service/              # 业务逻辑
│   │   ├── entity/               # 数据实体
│   │   ├── mapper/               # 数据访问
│   │   ├── dto/                  # 数据传输对象
│   │   ├── filter/              # 过滤器
│   │   └── util/                 # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml      # 配置文件
│   │   └── schema-mysql.sql     # MySQL 初始化脚本
│   └── pom.xml
│
├── frontend/                     # Vue 3 前端
│   ├── src/
│   │   ├── api/                 # API 调用
│   │   ├── components/          # 组件
│   │   ├── views/               # 页面
│   │   ├── stores/              # 状态管理
│   │   └── router/              # 路由
│   ├── package.json
│   └── vite.config.js
│
├── deploy/                       # 部署脚本
│   ├── start-backend.sh         # 后端启动脚本
│   ├── start-frontend.py        # 前端启动脚本
│   ├── start-all.sh             # 一键启动
│   ├── stop-all.sh              # 停止脚本
│   └── config/                  # 配置文件
│
├── docs/                         # 项目文档
│   ├── ARCHITECTURE.md          # 架构设计
│   ├── PRD.md                   # 产品需求文档
│   ├── database-selection-analysis.md  # 数据库选型分析
│   └── mysql-setup-guide.md     # MySQL 安装指南
│
├── docker-compose.yml           # Docker 部署（可选）
└── README.md
```

---

## ⌨️ 快捷键

### 视频播放

| 按键 | 功能 |
|------|------|
| `↑` | 播放上一个视频 |
| `↓` | 播放下一个视频 |
| `←` | 快退 10 秒 |
| `→` | 快进 10 秒 |
| `空格` | 播放/暂停 |
| `X` | 标记/取消不喜欢 |
| `D` | 删除当前视频 |
| `F` | 全屏切换 |
| `M` | 静音切换 |
| `ESC` | 退出全屏 |

### 图片预览

| 按键 | 功能 |
|------|------|
| `←` | 上一张图片 |
| `→` | 下一张图片 |
| `X` | 标记/取消不喜欢 |
| `D` | 删除当前图片 |
| `ESC` | 关闭预览 |

---

## 📝 媒体类型支持

### 视频格式

| 格式 | 扩展名 |
|------|--------|
| MP4 | .mp4, .m4v |
| AVI | .avi |
| MKV | .mkv |
| MOV | .mov |
| WMV | .wmv |
| FLV | .flv |
| WebM | .webm |

### 图片格式

| 格式 | 扩展名 |
|------|--------|
| JPEG | .jpg, .jpeg |
| PNG | .png |
| GIF | .gif |
| BMP | .bmp |
| WebP | .webp |
| SVG | .svg |

---

## 📝 API 接口

### 视频管理

| 方法 | 接口 | 说明 |
|------|------|------|
| GET | `/api/videos` | 视频列表（分页筛选） |
| GET | `/api/videos/{id}` | 视频详情 |
| GET | `/api/videos/random` | 随机一个视频 |
| PUT | `/api/videos/{id}/dislike` | 标记不喜欢 |
| DELETE | `/api/videos/dislikes` | 批量删除 |
| DELETE | `/api/videos/{id}` | 删除视频 |
| GET | `/api/videos/stream/{id}` | 视频流 |

### 图片管理

| 方法 | 接口 | 说明 |
|------|------|------|
| GET | `/api/images` | 图片列表（分页筛选） |
| GET | `/api/images/{id}` | 图片详情 |
| PUT | `/api/images/{id}/dislike` | 标记不喜欢 |
| DELETE | `/api/images/dislikes` | 批量删除 |
| DELETE | `/api/images/{id}` | 删除图片 |
| GET | `/api/images/thumb/{id}` | 缩略图 |
| GET | `/api/images/raw/{id}` | 原图 |

### 扫描管理

| 方法 | 接口 | 说明 |
|------|------|------|
| GET | `/api/scan/paths` | 路径列表 |
| POST | `/api/scan/paths` | 添加路径 |
| PUT | `/api/scan/paths/{id}` | 更新路径 |
| DELETE | `/api/scan/paths/{id}` | 删除路径 |
| POST | `/api/scan/start` | 触发扫描 |
| POST | `/api/scan/start/{pathId}` | 扫描指定路径 |
| POST | `/api/scan/stop` | 停止扫描 |
| GET | `/api/scan/status` | 扫描进度 |

### 文件夹管理

| 方法 | 接口 | 说明 |
|------|------|------|
| GET | `/api/folders` | 文件夹树 |
| DELETE | `/api/folders/empty` | 删除空文件夹 |

### 用户认证

| 方法 | 接口 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/register` | 注册 |
| GET | `/api/auth/me` | 当前用户信息 |

---

## 🐛 常见问题

### Q: 如何配置多个媒体目录？
A: 在「设置」→「扫描路径」中分别添加视频和图片路径，支持多目录。

### Q: 缩略图存储在哪里？
A: 缩略图存储在后端服务器的本地磁盘，可在 `application.yml` 中配置 `thumbnail-dir`。

### Q: 如何迁移数据？
A: 使用 MySQL 的 `mysqldump` 命令导出/导入数据库。

### Q: 支持 Docker 部署吗？
A: 支持，但推荐使用 `deploy/` 目录下的脚本直接部署，更适合 NAS 环境。

---

## 📄 License

MIT License
