<template>
  <div class="home-page">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <div class="logo">📁 文件管理器</div>
        <el-menu :default-active="activeMenu" router>
          <el-menu-item index="/">
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/videos">
            <span>🎬 视频</span>
          </el-menu-item>
          <el-menu-item index="/images">
            <span>🖼️ 图片</span>
          </el-menu-item>
          <el-menu-item index="/settings">
            <span>⚙️ 设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容 -->
      <el-container>
        <el-header>
          <div class="header-left">
            <span class="username">{{ userStore.user?.username }}</span>
          </div>
          <div class="header-right">
            <el-button @click="handleLogout">退出登录</el-button>
          </div>
        </el-header>
        
        <el-main>
          <!-- 统计卡片 -->
          <el-row :gutter="20" class="stats-row">
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-card">
                  <div class="stat-icon">🎬</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.videoCount }}</div>
                    <div class="stat-label">视频数量</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-card">
                  <div class="stat-icon">🖼️</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.imageCount }}</div>
                    <div class="stat-label">图片数量</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-card">
                  <div class="stat-icon">📁</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.dislikeCount }}</div>
                    <div class="stat-label">待清理</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 快捷操作 -->
          <el-card class="quick-actions">
            <template #header>
              <span>快捷操作</span>
            </template>
            <el-space wrap>
              <el-button type="primary" @click="router.push('/videos')">
                浏览视频
              </el-button>
              <el-button type="success" @click="router.push('/images')">
                浏览图片
              </el-button>
              <el-button type="warning" @click="router.push('/settings')">
                扫描管理
              </el-button>
              <el-button type="danger" @click="handleCleanup">
                批量清理
              </el-button>
            </el-space>
          </el-card>
          
          <!-- 最近扫描路径 -->
          <el-card class="scan-paths">
            <template #header>
              <span>扫描路径</span>
            </template>
            <el-empty v-if="scanPaths.length === 0" description="暂无扫描路径，请在设置中添加" />
            <el-list v-else>
              <el-list-item v-for="path in scanPaths" :key="path.id">
                <div class="path-item">
                  <span class="path-type">
                    {{ path.mediaType === 'video' ? '🎬' : path.mediaType === 'image' ? '🖼️' : '📁' }}
                  </span>
                  <span class="path-text">{{ path.path }}</span>
                  <span class="path-status">
                    {{ path.enabled ? '已启用' : '已禁用' }}
                  </span>
                </div>
              </el-list-item>
            </el-list>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getScanPaths } from '@/api/scan'
import { getVideoList } from '@/api/video'
import { getImageList } from '@/api/image'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('/')
const scanPaths = ref([])

const stats = reactive({
  videoCount: 0,
  imageCount: 0,
  dislikeCount: 0
})

const loadStats = async () => {
  try {
    const [videoRes, imageRes] = await Promise.all([
      getVideoList({ page: 1, pageSize: 1 }),
      getImageList({ page: 1, pageSize: 1 })
    ])
    stats.videoCount = videoRes.data.total
    stats.imageCount = imageRes.data.total
    
    // 获取不喜欢数量
    const [dislikedVideos, dislikedImages] = await Promise.all([
      getVideoList({ page: 1, pageSize: 1, dislike: 1 }),
      getImageList({ page: 1, pageSize: 1, dislike: 1 })
    ])
    stats.dislikeCount = dislikedVideos.data.total + dislikedImages.data.total
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

const loadScanPaths = async () => {
  try {
    const res = await getScanPaths()
    scanPaths.value = res.data
  } catch (error) {
    console.error('加载扫描路径失败', error)
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const handleCleanup = async () => {
  ElMessageBox.confirm(
    `确定要删除所有标记为不喜欢的媒体文件吗？`,
    '批量清理',
    { type: 'warning' }
  ).then(async () => {
    ElMessage.info('清理功能开发中...')
  }).catch(() => {})
}

onMounted(() => {
  loadStats()
  loadScanPaths()
})
</script>

<style scoped lang="scss">
.home-page {
  width: 100%;
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background: #304156;
  color: #fff;
  
  .logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    font-size: 18px;
    font-weight: bold;
    color: #fff;
  }
  
  .el-menu {
    border: none;
    background: transparent;
    
    .el-menu-item {
      color: #bfcbd9;
      
      &:hover, &.is-active {
        background: #263445;
        color: #409eff;
      }
    }
  }
}

.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.el-main {
  background: #f5f7fa;
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  
  .stat-icon {
    font-size: 48px;
    margin-right: 20px;
  }
  
  .stat-value {
    font-size: 32px;
    font-weight: bold;
    color: #303133;
  }
  
  .stat-label {
    font-size: 14px;
    color: #909399;
  }
}

.quick-actions, .scan-paths {
  margin-bottom: 20px;
}

.path-item {
  display: flex;
  align-items: center;
  width: 100%;
  
  .path-type {
    margin-right: 12px;
  }
  
  .path-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .path-status {
    margin-left: 12px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
