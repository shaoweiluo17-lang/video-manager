<template>
  <div class="video-list-page">
    <el-container>
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
      
      <el-container>
        <el-header>
          <div class="page-title">🎬 视频列表</div>
          <div class="header-right">
            <el-button @click="goRandom">随机播放</el-button>
          </div>
        </el-header>
        
        <el-main>
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input 
                v-model="keyword" 
                placeholder="搜索视频..." 
                style="width: 200px"
                clearable
                @change="loadVideos"
              />
              <el-select v-model="dislikeFilter" placeholder="筛选" style="width: 120px" @change="loadVideos">
                <el-option label="全部" :value="null" />
                <el-option label="未标记" :value="0" />
                <el-option label="不喜欢" :value="1" />
              </el-select>
            </div>
            <div class="toolbar-right">
              <span class="total-count">共 {{ total }} 个视频</span>
            </div>
          </div>
          
          <!-- 视频网格 -->
          <div v-if="videos.length > 0" class="media-grid">
            <div 
              v-for="video in videos" 
              :key="video.id" 
              class="media-card"
              @click="playVideo(video)"
            >
              <img 
                :src="getThumbUrl(video.id)" 
                class="thumbnail"
                @error="handleThumbError"
              />
              <div v-if="video.dislike === 1" class="dislike-badge">✗</div>
              <div class="info">
                <div class="name" :title="video.fileName">{{ video.fileName }}</div>
                <div class="meta">{{ formatSize(video.fileSize) }}</div>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无视频" />
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="loadVideos"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getVideoList, getRandomVideo, getVideoThumbUrl } from '@/api/video'

const router = useRouter()

const activeMenu = ref('/videos')
const videos = ref([])
const keyword = ref('')
const dislikeFilter = ref(null)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadVideos = async () => {
  try {
    const res = await getVideoList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || null,
      dislike: dislikeFilter.value
    })
    videos.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载视频列表失败', error)
  }
}

const playVideo = (video) => {
  router.push(`/videos/${video.id}/play`)
}

const goRandom = async () => {
  try {
    const res = await getRandomVideo()
    if (res.data) {
      router.push(`/videos/${res.data.id}/play`)
    }
  } catch (error) {
    console.error('获取随机视频失败', error)
  }
}

const getThumbUrl = (id) => getVideoThumbUrl(id)

const handleThumbError = (e) => {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 120"><rect fill="%23333" width="200" height="120"/><text fill="%23fff" x="50%" y="50%" text-anchor="middle" dy=".3em">🎬</text></svg>'
}

const formatSize = (size) => {
  if (!size) return '未知'
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  let s = size
  while (s >= 1024 && unitIndex < units.length - 1) {
    s /= 1024
    unitIndex++
  }
  return `${s.toFixed(1)} ${units[unitIndex]}`
}

onMounted(() => {
  loadVideos()
})
</script>

<style scoped lang="scss">
.video-list-page {
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
  padding: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #fff;
}

.total-count {
  color: #909399;
  font-size: 14px;
}
</style>
