import request from '../utils/request'

// 获取角色列表
export function getRoleList(params) {
  return request({
    url: '/role/list',
    method: 'get',
    params
  })
}

// 获取角色详情
export function getRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(id, data) {
  return request({
    url: `/role/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}

// 获取所有角色（用于下拉选择）
export function getAllRoles() {
  return request({
    url: '/role/all',
    method: 'get'
  })
}

// 获取角色的权限
export function getRolePermissions(roleId) {
  return request({
    url: `/role/${roleId}/permissions`,
    method: 'get'
  })
}

// 更新角色的权限
export function updateRolePermissions(roleId, permissionIds) {
  return request({
    url: `/role/${roleId}/permissions`,
    method: 'put',
    data: { permissionIds }
  })
} 