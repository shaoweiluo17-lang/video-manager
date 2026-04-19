import request from './request'

export function getVideoList(params) {
  return request.get('/videos', { params })
}

export function getVideo(id) {
  return request.get(`/videos/${id}`)
}

export function getRandomVideo() {
  return request.get('/videos/random')
}

export function toggleVideoDislike(id) {
  return request.put(`/videos/${id}/dislike`)
}

export function deleteVideo(id) {
  return request.delete(`/videos/${id}`)
}

export function deleteDislikedVideos() {
  return request.delete('/videos/dislikes')
}

/**
 * 获取视频流URL
 * 使用静态文件路径直接访问，不走后端代理
 * @param {string} filePath - 视频文件绝对路径
 * @returns {string} - 静态文件访问URL
 */
export function getVideoStreamUrl(filePath) {
  // 使用 /files 前缀直接访问文件系统
  return `/files${filePath}`
}

/**
 * 获取视频缩略图URL
 * @param {number} id - 视频ID
 * @returns {string} - 缩略图URL
 */
export function getVideoThumbUrl(id) {
  return `/api/videos/thumb/${id}`
}
