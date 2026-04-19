# 视频图片文件管理系统 (Video & Image Manager)

> 一个类抖音体验的视频+图片文件管理 Web 系统

## 📖 项目简介

本系统旨在帮助用户管理分布在多个硬盘/文件夹中的视频和图片文件，提供类抖音的沉浸式视频播放体验，支持图片网格/列表浏览、键盘快捷操作去标记/删除，实现批量清理和去重功能。

### 🎯 核心功能

#### 视频管理
- ✅ **类抖音播放** - 上下键切换视频，快进快退
- ✅ **快捷键操作** - X 标记不喜欢，D 删除视频
- ✅ **批量清理** - 一键删除不喜欢视频
- ✅ **文件去重** - 基于 MD5 哈希识别重复视频

#### 图片管理
- ✅ **网格浏览** - 缩略图网格展示
- ✅ **列表浏览** - 支持保留文件夹层级 或 平铺显示
- ✅ **点击放大** - 图片预览，支持左右切换
- ✅ **缩略图** - 后端自动生成缩略图
- ✅ **批量清理** - 标记不喜欢，批量删除
- ✅ **图片去重** - 基于 MD5 哈希识别重复图片

#### 系统功能
- ✅ **用户认证** - 支持多用户登录
- ✅ **配置化管理** - 视频/图片路径分开配置
- ✅ **Docker 部署** - 一键部署到 NAS

---

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Vue 3 + Vite + Element Plus |
| **视频播放** | Vue3VideoPlayer |
| **图片预览** | VueEasyImage / v-viewer |
| **后端** | Java 17 + Spring Boot 3 |
| **ORM** | MyBatis-Plus |
| **数据库** | SQLite |
| **缩略图** | Thumbnailator |
| **认证** | JWT |
| **部署** | Docker + Docker Compose |

---

## 🚀 快速开始

### 1. 环境要求

- Docker & Docker Compose
- 8GB+ RAM (推荐)
- 视频/图片存储路径

### 2. 下载项目

```bash
git clone <repository-url>
cd video-manager
```

### 3. 配置媒体路径

编辑 `docker-compose.yml` 中的 volumes：

```yaml
volumes:
  # 数据持久化
  - ./data:/app/data
  
  # 视频文件夹（只读）
  - /your/video/path:/mnt/videos:ro
  
  # 图片文件夹（只读）
  - /your/image/path:/mnt/images:ro
```

### 4. 启动服务

```bash
docker-compose up -d
```

### 5. 访问系统

- 打开浏览器访问：`http://<your-nas-ip>:80`
- 首次使用需要注册账号
- 登录后：
  1. 在「设置」→「视频路径」添加视频文件夹
  2. 在「设置」→「图片路径」添加图片文件夹
  3. 触发扫描

---

## 📁 项目结构

```
video-manager/
├── backend/                 # Java Spring Boot 后端
│   ├── src/main/java/com/videomanager/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # REST API
│   │   │   ├── VideoController.java
│   │   │   ├── ImageController.java
│   │   │   └── ScanController.java
│   │   ├── service/        # 业务逻辑
│   │   │   ├── VideoService.java
│   │   │   ├── ImageService.java
│   │   │   └── ThumbnailService.java
│   │   └── entity/        # 数据实体
│   │       ├── Video.java
│   │       └── Image.java
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── api/           # API 调用
│   │   ├── components/    # 组件
│   │   │   ├── VideoPlayer.vue   # 抖音式播放器
│   │   │   ├── ImageGrid.vue     # 图片网格
│   │   │   ├── ImagePreview.vue  # 图片预览
│   │   │   └── FolderTree.vue    # 文件夹树
│   │   └── views/        # 页面
│   │       ├── VideoList.vue
│   │       ├── ImageGrid.vue
│   │       └── ImageList.vue
│   ├── package.json
│   └── Dockerfile
│
├── docs/                  # 项目文档
├── docker-compose.yml
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
| GET | `/api/videos/random` | 随机一个视频 |
| PUT | `/api/videos/{id}/dislike` | 标记不喜欢 |
| DELETE | `/api/videos/dislikes` | 批量删除 |
| GET | `/api/videos/stream/{id}` | 视频流 |

### 图片管理

| 方法 | 接口 | 说明 |
|------|------|------|
| GET | `/api/images` | 图片列表（分页筛选） |
| GET | `/api/images/{id}` | 图片详情 |
| PUT | `/api/images/{id}/dislike` | 标记不喜欢 |
| DELETE | `/api/images/dislikes` | 批量删除 |
| GET | `/api/images/thumb/{id}` | 缩略图 |
| GET | `/api/images/raw/{id}` | 原图 |

### 扫描管理

| 方法 | 接口 | 说明 |
|------|------|------|
| GET | `/api/scan/paths` | 路径列表 |
| POST | `/api/scan/paths` | 添加路径 |
| POST | `/api/scan/start` | 触发扫描 |
| GET | `/api/scan/status` | 扫描进度 |

### 文件夹管理

| 方法 | 接口 | 说明 |
|------|------|------|
| GET | `/api/folders` | 文件夹树 |
| DELETE | `/api/folders/empty` | 删除空文件夹 |

---

## 🐛 常见问题

### Q: 视频/图片扫描不到？
A: 检查 Docker 容器是否有读取权限，确认路径配置正确。

### Q: 缩略图不显示？
A: 缩略图由后端生成，首次扫描需要等待生成完成。

### Q: 如何区分视频和图片文件夹？
A: 在「设置」中分别配置视频路径和图片路径，系统会分开管理。

---

## 📄 License

MIT License
