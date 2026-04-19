<template>
  <div class="settings-page">
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
          <div class="page-title">⚙️ 设置</div>
        </el-header>
        
        <el-main>
          <el-tabs v-model="activeTab" type="border-card">
            <!-- 扫描路径管理 -->
            <el-tab-pane label="扫描路径" name="paths">
              <div class="section-header">
                <span>管理扫描路径</span>
                <el-button type="primary" @click="showAddDialog = true">
                  添加路径
                </el-button>
              </div>
              
              <el-table :data="scanPaths" stripe>
                <el-table-column prop="path" label="路径" />
                <el-table-column prop="mediaType" label="类型" width="100">
                  <template #default="{ row }">
                    {{ row.mediaType === 'video' ? '🎬 视频' : '🖼️ 图片' }}
                  </template>
                </el-table-column>
                <el-table-column prop="enabled" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.enabled ? 'success' : 'info'">
                      {{ row.enabled ? '已启用' : '已禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200">
                  <template #default="{ row }">
                    <el-button size="small" @click="togglePath(row)">
                      {{ row.enabled ? '禁用' : '启用' }}
                    </el-button>
                    <el-button size="small" type="danger" @click="deletePath(row)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            
            <!-- 扫描操作 -->
            <el-tab-pane label="扫描操作" name="scan">
              <div class="scan-status">
                <el-card>
                  <template #header>
                    <span>扫描状态</span>
                  </template>
                  <div class="status-info">
                    <p>状态: 
                      <el-tag :type="scanStatus.running ? 'success' : 'info'">
                        {{ scanStatus.running ? '扫描中' : '空闲' }}
                      </el-tag>
                    </p>
                    <p v-if="scanStatus.running">
                      当前: {{ scanStatus.currentPath }}
                    </p>
                    <p v-if="scanStatus.running">
                      已扫描: {{ scanStatus.scannedFiles }} 个文件
                    </p>
                  </div>
                  <div class="scan-actions">
                    <el-button 
                      type="primary" 
                      :loading="scanStatus.running"
                      @click="startScan"
                    >
                      开始扫描
                    </el-button>
                    <el-button 
                      :disabled="!scanStatus.running"
                      @click="stopScan"
                    >
                      停止扫描
                    </el-button>
                  </div>
                </el-card>
              </div>
              
              <el-card class="cleanup-card">
                <template #header>
                  <span>清理操作</span>
                </template>
                <el-space wrap>
                  <el-button type="danger" @click="cleanupDisliked">
                    删除不喜欢的内容
                  </el-button>
                  <el-button @click="cleanupEmptyFolders">
                    清理空文件夹
                  </el-button>
                </el-space>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 添加路径对话框 -->
    <el-dialog v-model="showAddDialog" title="添加扫描路径" width="500px">
      <el-form :model="pathForm" label-width="80px">
        <el-form-item label="路径">
          <el-input v-model="pathForm.path" placeholder="/mnt/media/video" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="pathForm.mediaType" style="width: 100%">
            <el-option label="🎬 视频" value="video" />
            <el-option label="🖼️ 图片" value="image" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addPath">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getScanPaths, addScanPath, updateScanPath, deleteScanPath,
  startScan, stopScan, getScanStatus, deleteEmptyFolders,
  deleteDislikedVideos 
} from '@/api/scan'
import { deleteDislikedImages } from '@/api/image'

const activeMenu = ref('/settings')
const activeTab = ref('paths')

const scanPaths = ref([])
const showAddDialog = ref(false)
const pathForm = reactive({
  path: '',
  mediaType: 'video'
})

const scanStatus = reactive({
  running: false,
  currentPath: '',
  scannedFiles: 0
})

let statusTimer = null

const loadScanPaths = async () => {
  try {
    const res = await getScanPaths()
    scanPaths.value = res.data
  } catch (error) {
    console.error('加载扫描路径失败', error)
  }
}

const addPath = async () => {
  if (!pathForm.path) {
    ElMessage.warning('请输入路径')
    return
  }
  try {
    await addScanPath(pathForm.path, pathForm.mediaType)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    pathForm.path = ''
    loadScanPaths()
  } catch (error) {
    console.error('添加失败', error)
  }
}

const togglePath = async (path) => {
  try {
    await updateScanPath(path.id, path.path, path.mediaType, !path.enabled)
    loadScanPaths()
  } catch (error) {
    console.error('更新失败', error)
  }
}

const deletePath = async (path) => {
  try {
    await ElMessageBox.confirm('确定要删除这个路径吗？', '提示', { type: 'warning' })
    await deleteScanPath(path.id)
    ElMessage.success('删除成功')
    loadScanPaths()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const loadScanStatus = async () => {
  try {
    const res = await getScanStatus()
    scanStatus.running = res.data.running
    scanStatus.currentPath = res.data.currentPath
    scanStatus.scannedFiles = res.data.scannedFiles
  } catch (error) {
    console.error('加载扫描状态失败', error)
  }
}

const startScan = async () => {
  try {
    await startScan()
    ElMessage.success('扫描已开始')
    loadScanStatus()
  } catch (error) {
    console.error('启动扫描失败', error)
  }
}

const stopScan = async () => {
  try {
    await stopScan()
    ElMessage.info('扫描已停止')
    loadScanStatus()
  } catch (error) {
    console.error('停止扫描失败', error)
  }
}

const cleanupDisliked = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除所有标记为不喜欢的视频和图片吗？此操作不可恢复！',
      '警告',
      { type: 'warning' }
    )
    await deleteDislikedVideos()
    await deleteDislikedImages()
    ElMessage.success('清理完成')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理失败', error)
    }
  }
}

const cleanupEmptyFolders = async () => {
  try {
    await ElMessageBox.confirm('确定要删除所有空文件夹吗？', '提示', { type: 'warning' })
    await deleteEmptyFolders()
    ElMessage.success('清理完成')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理失败', error)
    }
  }
}

onMounted(() => {
  loadScanPaths()
  loadScanStatus()
  // 每 3 秒刷新一次扫描状态
  statusTimer = setInterval(loadScanStatus, 3000)
})

onUnmounted(() => {
  if (statusTimer) {
    clearInterval(statusTimer)
  }
})
</script>

<style scoped lang="scss">
.settings-page {
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
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.el-main {
  background: #f5f7fa;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.scan-status {
  margin-bottom: 20px;
  
  .status-info {
    p {
      margin: 8px 0;
      font-size: 14px;
    }
  }
  
  .scan-actions {
    margin-top: 16px;
  }
}

.cleanup-card {
  margin-top: 20px;
}
</style>