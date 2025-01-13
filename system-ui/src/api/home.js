import request from '../utils/request'

export function getStatistics() {
  return request({
    url: '/statistics',
    method: 'get'
  })
}

export function getRecentPapers() {
  return request({
    url: '/papers/recent',
    method: 'get'
  })
}

export function globalCount() {
  return request({
    url: '/global/count',
    method: 'get'
  })
}