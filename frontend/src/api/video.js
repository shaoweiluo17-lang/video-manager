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

export function getVideoStreamUrl(id) {
  return `/api/videos/stream/${id}`
}

export function getVideoThumbUrl(id) {
  return `/api/videos/thumb/${id}`
}
