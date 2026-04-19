import request from './request'

export function getScanPaths() {
  return request.get('/scan/paths')
}

export function addScanPath(path, mediaType) {
  return request.post('/scan/paths', null, { params: { path, mediaType } })
}

export function updateScanPath(id, path, mediaType, enabled) {
  return request.put(`/scan/paths/${id}`, null, { params: { path, mediaType, enabled } })
}

export function deleteScanPath(id) {
  return request.delete(`/scan/paths/${id}`)
}

export function startScan() {
  return request.post('/scan/start')
}

export function startScanByPath(pathId) {
  return request.post(`/scan/start/${pathId}`)
}

export function stopScan() {
  return request.post('/scan/stop')
}

export function getScanStatus() {
  return request.get('/scan/status')
}

export function getFolders(type) {
  return request.get('/folders', { params: { type } })
}

export function deleteEmptyFolders() {
  return request.delete('/folders/empty')
}
