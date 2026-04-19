<template>
  <div class="image-list-page">
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
          <div class="page-title">🖼️ 图片列表</div>
          <div class="header-right">
            <el-button-group>
              <el-button :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
                ▦
              </el-button>
              <el-button :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'">
                ☰
              </el-button>
            </el-button-group>
          </div>
        </el-header>
        
        <el-main>
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input 
                v-model="keyword" 
                placeholder="搜索图片..." 
                style="width: 200px"
                clearable
                @change="loadImages"
              />
              <el-select v-model="dislikeFilter" placeholder="筛选" style="width: 120px" @change="loadImages">
                <el-option label="全部" :value="null" />
                <el-option label="未标记" :value="0" />
                <el-option label="不喜欢" :value="1" />
              </el-select>
            </div>
            <div class="toolbar-right">
              <span class="total-count">共 {{ total }} 张图片</span>
            </div>
          </div>
          
          <!-- 网格视图 -->
          <div v-if="viewMode === 'grid' && images.length > 0" class="media-grid">
            <div 
              v-for="image in images" 
              :key="image.id" 
              class="media-card"
              @click="previewImage(image)"
            >
              <img 
                :src="getThumbUrl(image.id)" 
                class="thumbnail"
                @error="handleThumbError"
              />
              <div v-if="image.dislike === 1" class="dislike-badge">✗</div>
              <div class="info">
                <div class="name" :title="image.fileName">{{ image.fileName }}</div>
                <div class="meta">{{ formatSize(image.fileSize) }}</div>
              </div>
            </div>
          </div>
          
          <!-- 列表视图 -->
          <div v-else-if="viewMode === 'list' && images.length > 0" class="media-list">
            <div 
              v-for="image in images" 
              :key="image.id" 
              class="list-item"
              @click="previewImage(image)"
            >
              <img 
                :src="getThumbUrl(image.id)" 
                class="thumbnail"
                @error="handleThumbError"
              />
              <div class="info">
                <div class="name">{{ image.fileName }}</div>
                <div class="meta">{{ image.width }}x{{ image.height }} | {{ formatSize(image.fileSize) }}</div>
              </div>
              <div v-if="image.dislike === 1" class="dislike-tag">不喜欢</div>
            </div>
          </div>
          
          <el-empty v-else description="暂无图片" />
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="loadImages"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 图片预览 -->
    <el-dialog 
      v-model="previewVisible" 
      :title="currentImage?.fileName"
      width="80%"
      destroy-on-close
    >
      <div class="preview-container">
        <img :src="getRawUrl(currentImage?.id)" class="preview-image" />
      </div>
      <template #footer>
        <el-button @click="toggleDislike">标记不喜欢</el-button>
        <el-button @click="deleteImage">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getImageList, toggleImageDislike, deleteImage as delImage, getImageThumbUrl, getImageRawUrl } from '@/api/image'

const activeMenu = ref('/images')
const viewMode = ref('grid')
const images = ref([])
const keyword = ref('')
const dislikeFilter = ref(null)
const page = ref(1)
const pageSize = ref(40)
const total = ref(0)

const previewVisible = ref(false)
const currentImage = ref(null)

const loadImages = async () => {
  try {
    const res = await getImageList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || null,
      dislike: dislikeFilter.value
    })
    images.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载图片列表失败', error)
  }
}

const previewImage = (image) => {
  currentImage.value = image
  previewVisible.value = true
}

const toggleDislike = async () => {
  if (!currentImage.value) return
  try {
    await toggleImageDislike(currentImage.value.id)
    ElMessage.success('操作成功')
    loadImages()
  } catch (error) {
    console.error('标记失败', error)
  }
}

const deleteImage = async () => {
  if (!currentImage.value) return
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '提示', { type: 'warning' })
    await delImage(currentImage.value.id)
    ElMessage.success('删除成功')
    previewVisible.value = false
    loadImages()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const getThumbUrl = (id) => getImageThumbUrl(id)
const getRawUrl = (id) => getImageRawUrl(id)

const handleThumbError = (e) => {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 120"><rect fill="%23333" width="200" height="120"/><text fill="%23fff" x="50%" y="50%" text-anchor="middle" dy=".3em">🖼️</text></svg>'
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
  loadImages()
})
</script>

<style scoped lang="scss">
.image-list-page {
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
  overflow: auto;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

