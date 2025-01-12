import request from '../utils/request'

// 获取试卷列表
export function getPaperList(params) {
  return request({
    url: '/paper/list',
    method: 'get',
    params
  })
}

// 获取试卷详情
export function getPaper(id) {
  return request({
    url: `/paper/${id}`,
    method: 'get'
  })
}

// 创建试卷
export function createPaper(data) {
  return request({
    url: '/paper/add',
    method: 'post',
    data
  })
}

// 更新试卷
export function updatePaper(id, data) {
  return request({
    url: `/paper/${id}`,
    method: 'put',
    data
  })
}

// 删除试卷
export function deletePaper(id) {
  return request({
    url: `/paper/${id}`,
    method: 'delete'
  })
}

// 批量删除试卷
export function batchDeletePaper(ids) {
  return request({
    url: '/paper/batchDelete',
    method: 'post',
    data: ids
  })
}

// 发布试卷
export function publishPaper(id) {
  return request({
    url: `/paper/${id}/publish`,
    method: 'put'
  })
}

// 获取试卷的题目列表
export function getQuestionsByPaperId(paperId) {
  return request({
    url: `/questions/list/${paperId}`,
    method: 'get'
  })
}

// 添加题目到试卷
export function addQuestionToPaper(paperId, data) {
  return request({
    url: `/questions/add/${paperId}`,
    method: 'post',
    data
  })
}

// 更新试卷中的题目
export function updatePaperQuestion(paperId, questionId, data) {
  return request({
    url: `/questions/edit`,
    method: 'post',
    data
  })
}

// 从试卷中删除题目
export function deleteQuestionFromPaper(paperId, questionId) {
  return request({
    url: `/questions/delete/${questionId}`,
    method: 'delete'
  })
}

// 提交答案
export function submitAnswers(paperId, data) {
  return request({
    url: `/paper/${paperId}/submit`,
    method: 'post',
    data
  })
}

// 获取答题结果
export function getSubmissionResult(submissionId) {
  return request({
    url: `/paper/submission/${submissionId}`,
    method: 'get'
  })
} 