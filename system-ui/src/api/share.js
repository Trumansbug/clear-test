import request from '@/utils/request'

// 获取试卷列表
export function getShareList(params) {
    return request({
        url: '/share/list',
        method: 'get',
        params
    })
}

// 删除试卷
export function deleteShare(id) {
    return request({
        url: `/share/${id}`,
        method: 'delete'
    })
}

// 批量删除试卷
export function batchDeleteShare(ids) {
    return request({
        url: '/share/batchDelete',
        method: 'post',
        data: ids
    })
}

// 创建试卷
export function createShare(data) {
    return request({
        url: '/share/add',
        method: 'post',
        data
    })
}

// 修改分享状态
export function changeShareStatus(params) {
    return request({
        url: '/share/changeStatus',
        method: 'get',
        params
    })
}