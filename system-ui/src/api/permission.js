import request from '../utils/request'

// 获取权限树
export function getPermissionTree() {
  return request({
    url: '/permission/tree',
    method: 'get'
  })
}

// 获取所有权限（扁平结构）
export function getAllPermissions() {
  return request({
    url: '/permission/list',
    method: 'get'
  })
}

// 创建权限
export function createPermission(data) {
  return request({
    url: '/permission',
    method: 'post',
    data
  })
}

// 更新权限
export function updatePermission(id, data) {
  return request({
    url: `/permission/${id}`,
    method: 'put',
    data
  })
}

// 删除权限
export function deletePermission(id) {
  return request({
    url: `/permission/${id}`,
    method: 'delete'
  })
} 