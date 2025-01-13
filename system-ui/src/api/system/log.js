import request from '@/utils/request'

// 获取日志列表
export function getLogList(params) {
  return request({
    url: '/system/log/list',
    method: 'get',
    params
  })
}

// 删除日志
export function deleteLog(id) {
  return request({
    url: `/system/log/${id}`,
    method: 'delete'
  })
}

// 批量删除日志
export function batchDeleteLog(ids) {
  return request({
    url: '/system/log/batch',
    method: 'delete',
    data: ids
  })
}

// 清空日志
export function clearLog() {
  return request({
    url: '/system/log/clear',
    method: 'delete'
  })
} 