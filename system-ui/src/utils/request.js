import axios from 'axios'
import { Message } from 'element-ui'
import router from '../router'
import store from '../store'

// 创建 axios 实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api', // API 的基础URL
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 如果返回的状态码不是 200，说明接口有问题，需要处理
    if (res.code !== 200) {
      Message({
        message: res.message || '系统错误',
        type: 'error',
        duration: 5 * 1000
      })

      // 401: 未登录或token过期
      // 403: 权限不足
      if (res.code === 401 || res.code === 403) {
        // 重新登录
        store.dispatch('logout').then(() => {
          router.push(`/login?redirect=${router.currentRoute.value.fullPath}`)
        })
      }
      return Promise.reject(new Error(res.message || '系统错误'))
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误:', error)
    const { response } = error
    return Promise.reject(new Error(response?.data?.message || '系统错误'))
  }
)

export default service 