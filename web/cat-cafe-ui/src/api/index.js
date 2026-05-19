import axios from 'axios'

const api = axios.create({
  baseURL: 'http://127.0.0.1:8000', // 你的 FastAPI 地址
  timeout: 5000,
})

// 请求拦截器（以后加 token 用）
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default api