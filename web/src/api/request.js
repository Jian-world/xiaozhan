import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

/** 请求拦截：注入 token */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('xz_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

let sessionExpiredNotified = false

/** 响应拦截：拆包 + 统一错误处理 */
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 文件流等非标准响应直接返回
    if (!res || typeof res.code === 'undefined') {
      return res
    }

    if (res.code === 200) {
      return res.data
    }

    // 401：登录态失效
    if (res.code === 401) {
      if (!sessionExpiredNotified) {
        sessionExpiredNotified = true
        ElMessageBox.alert('登录状态已过期，请重新登录', '提示', {
          type: 'warning',
          confirmButtonText: '去登录'
        })
          .then(() => {
            localStorage.removeItem('xz_token')
            localStorage.removeItem('xz_user')
            router.push('/login')
          })
          .finally(() => {
            sessionExpiredNotified = false
          })
      }
      return Promise.reject(new Error(res.message || '登录已失效'))
    }

    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('xz_token')
      localStorage.removeItem('xz_user')
      ElMessage.error('登录状态已过期，请重新登录')
      router.push('/login')
    } else if (status === 413) {
      ElMessage.error('上传的文件体积过大')
    } else if (status === 404) {
      ElMessage.error('请求的接口不存在')
    } else if (status >= 500) {
      ElMessage.error('服务端异常，请稍后再试')
    } else {
      ElMessage.error(error?.response?.data?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
