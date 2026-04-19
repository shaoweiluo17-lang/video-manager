import request from './request'

export function getImageList(params) {
  return request.get('/images', { params })
}

export function getImage(id) {
  return request.get(`/images/${id}`)
}

export function toggleImageDislike(id) {
  return request.put(`/images/${id}/dislike`)
}

export function deleteImage(id) {
  return request.delete(`/images/${id}`)
}

export function deleteDislikedImages() {
  return request.delete('/images/dislikes')
}

export function getImageThumbUrl(id) {
  return `/api/images/thumb/${id}`
}

export function getImageRawUrl(id) {
  return `/api/images/raw/${id}`
}
