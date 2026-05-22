import axios from 'axios'

const api = axios.create({
  baseURL: '', // 生产用相对路径，开发走 vite proxy
  timeout: 15000,
})

// 请求拦截器（以后加 token 用）
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default api