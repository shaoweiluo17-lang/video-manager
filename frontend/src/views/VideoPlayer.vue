<template>
  <div class="video-player-page">
    <div class="player-header">
      <el-button @click="goBack">
        ← 返回
      </el-button>
      <div class="video-title">{{ video?.fileName }}</div>
      <div class="header-spacer"></div>
    </div>
    
    <div class="player-container">
      <video 
        ref="videoRef"
        controls
        autoplay
        preload="auto"
        @keydown="handleKeydown"
        @error="handleVideoError"
        @loadeddata="handleVideoLoaded"
        @canplay="handleCanPlay"
      >
        <source v-if="streamUrl" :src="streamUrl" type="video/mp4" />
        您的浏览器不支持视频播放
      </video>
    </div>
    
    <div class="player-controls">
      <div class="control-left">
        <el-button-group>
          <el-button @click="playPrev" title="上一个 (↑)">⬆️</el-button>
          <el-button @click="playNext" title="下一个 (↓)">⬇️</el-button>
        </el-button-group>
      </div>
      <div class="control-center">
        <span class="hint">按 ↑↓ 键切换视频 | 按 X 键标记不喜欢</span>
      </div>
      <div class="control-right">
        <el-button 
          :type="video?.dislike === 1 ? 'danger' : 'default'"
          @click="toggleDislike"
        >
          {{ video?.dislike === 1 ? '✓ 不喜欢' : '✗ 标记不喜欢' }}
        </el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>
    
    <!-- 播放列表 -->
    <div class="playlist">
      <div class="playlist-title">播放列表</div>
      <div class="playlist-items">
        <div 
          v-for="(item, index) in playlist" 
          :key="item.id"
          :class="['playlist-item', { active: item.id === video?.id }]"
          @click="playVideo(item)"
        >
          <span class="item-index">{{ index + 1 }}</span>
          <span class="item-name">{{ item.fileName }}</span>
          <span v-if="item.dislike === 1" class="item-dislike">✗</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVideo, getVideoList, toggleVideoDislike, deleteVideo as delVideo, getVideoStreamUrl } from '@/api/video'

const route = useRoute()
const router = useRouter()

const videoRef = ref(null)
const video = ref(null)
const playlist = ref([])
const currentIndex = ref(0)
const streamUrl = ref('')

const loadVideo = async (id) => {
  try {
    const res = await getVideo(id)
    video.value = res.data
    // 使用文件路径直接访问视频文件
    streamUrl.value = getVideoStreamUrl(res.data.filePath)
    console.log('视频文件路径:', res.data.filePath)
    console.log('视频流URL:', streamUrl.value)
  } catch (error) {
    console.error('加载视频失败', error)
    router.push('/videos')
  }
}

const loadPlaylist = async () => {
  try {
    const res = await getVideoList({ page: 1, pageSize: 100 })
    playlist.value = res.data.records
    const index = playlist.value.findIndex(v => v.id === video.value?.id)
    if (index !== -1) {
      currentIndex.value = index
    }
  } catch (error) {
    console.error('加载播放列表失败', error)
  }
}

const playVideo = (item) => {
  // 直接使用已有的视频信息，避免重复请求后端
  video.value = item
  streamUrl.value = getVideoStreamUrl(item.filePath)
  console.log('切换视频:', item.fileName)
  console.log('视频流URL:', streamUrl.value)
  
  // 更新URL（不重新加载）
  router.push(`/videos/${item.id}/play`)
  
  // 手动触发视频重新加载
  nextTick(() => {
    if (videoRef.value) {
      videoRef.value.load()
      videoRef.value.play().catch(e => console.log('自动播放失败:', e))
    }
  })
}

const playPrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    playVideo(playlist.value[currentIndex.value])
  } else {
    ElMessage.info('已经是第一个了')
  }
}

const playNext = () => {
  if (currentIndex.value < playlist.value.length - 1) {
    currentIndex.value++
    playVideo(playlist.value[currentIndex.value])
  } else {
    ElMessage.info('已经是最后一个了')
  }
}

const toggleDislike = async () => {
  if (!video.value) return
  try {
    await toggleVideoDislike(video.value.id)
    video.value.dislike = video.value.dislike === 1 ? 0 : 1
    ElMessage.success(video.value.dislike === 1 ? '已标记不喜欢' : '已取消标记')
    loadPlaylist()
  } catch (error) {
    console.error('标记失败', error)
  }
}

const handleDelete = async () => {
  if (!video.value) return
  try {
    await ElMessageBox.confirm('确定要删除这个视频吗？', '提示', { type: 'warning' })
    await delVideo(video.value.id)
    ElMessage.success('删除成功')
    playNext()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const goBack = () => {
  router.push('/videos')
}

// 视频事件处理
const handleVideoError = (e) => {
  console.error('视频加载错误:', e)
  const video = e.target
  if (video.error) {
    console.error('错误代码:', video.error.code)
    console.error('错误信息:', video.error.message)
    // 错误代码说明:
    // 1 = MEDIA_ERR_ABORTED - 用户中止
    // 2 = MEDIA_ERR_NETWORK - 网络错误
    // 3 = MEDIA_ERR_DECODE - 解码错误
    // 4 = MEDIA_ERR_SRC_NOT_SUPPORTED - 格式不支持
  }
}

const handleVideoLoaded = () => {
  console.log('视频数据已加载')
}

const handleCanPlay = () => {
  console.log('视频可以播放')
}

const handleKeydown = (e) => {
  switch(e.key) {
    case 'ArrowUp':
      e.preventDefault()
      playPrev()
      break
    case 'ArrowDown':
      e.preventDefault()
      playNext()
      break
    case 'x':
    case 'X':
      toggleDislike()
      break
  }
}

onMounted(() => {
  const id = route.params.id
  loadVideo(id)
  loadPlaylist()
  
  // 添加键盘监听
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
.video-player-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #000;
}

.player-header {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  
  .video-title {
    margin-left: 20px;
    font-size: 16px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .header-spacer {
    flex: 1;
  }
}

.player-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  min-height: 300px;
  
  video {
    width: 100%;
    height: 100%;
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }
}

.player-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  
  .hint {
    font-size: 12px;
    color: #909399;
  }
}

.playlist {
  width: 300px;
  background: rgba(0, 0, 0, 0.9);
  border-left: 1px solid #333;
  display: flex;
  flex-direction: column;
  
  .playlist-title {
    padding: 12px;
    font-size: 14px;
    font-weight: bold;
    border-bottom: 1px solid #333;
  }
  
  .playlist-items {
    flex: 1;
    overflow-y: auto;
  }
  
  .playlist-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    cursor: pointer;
    border-bottom: 1px solid #222;
    transition: background 0.2s;
    
    &:hover {
      background: #333;
    }
    
    &.active {
      background: #409eff;
      color: #fff;
    }
    
    .item-index {
      width: 30px;
      color: #666;
      font-size: 12px;
    }
    
    .item-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 13px;
    }
    
    .item-dislike {
      margin-left: 8px;
      color: #f56c6c;
    }
  }
}
</style>
