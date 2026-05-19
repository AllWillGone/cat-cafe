<template>
  <div class="signup-container">
    <el-card class="signup-card">
      <template #header>
        <h2>注册账号</h2>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        @submit.prevent="handleSignup"
      >
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="验证码" prop="smsCode">
          <div class="sms-row">
            <el-input v-model="form.smsCode" placeholder="请输入验证码" />
            <el-button
              :disabled="countdown > 0"
              :loading="sendingCode"
              @click="sendSmsCode"
            >
              {{ countdown > 0 ? `${countdown}s 后重发` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="密码" prop="userPassword">
          <el-input
            v-model="form.userPassword"
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSignup">
            注册
          </el-button>
          <el-button @click="$router.push('/login')">
            已有账号？去登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/index'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let correctCode = ''

const form = reactive({
  userName: '',
  phone: '',
  smsCode: '',
  userPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== form.userPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 50, message: '用户名长度 1-50', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' },
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
    { max: 128, message: '密码不超过128个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const sendSmsCode = async () => {
  const valid = await formRef.value.validateField('phone').catch(() => false)
  if (!valid) return

  sendingCode.value = true
  try {
    const res = await api.post('/api/send-sms-code', {
      userPhone: form.phone,
    })
    correctCode = res.data.code
    ElMessage.success(`验证码已发送（演示：${correctCode}）`)
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

const handleSignup = async () => {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return

  if (form.smsCode !== correctCode) {
    ElMessage.error('验证码错误')
    return
  }

  loading.value = true
  try {
    const res = await api.post('/api/register', {
      userName: form.userName,
      userPassword: form.userPassword,
      userPhone: form.phone,
    })
    ElMessage.success(`注册成功！欢迎你，${res.data.userName}`)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userId', res.data.userId)
    localStorage.setItem('userName', res.data.userName)
    localStorage.setItem('userType', res.data.userType)
    router.push('/home')
  } catch (err) {
    const detail = err.response?.data?.detail
    if (detail) {
      ElMessage.error(detail)
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.signup-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg);
}

.signup-card {
  width: 440px;
}

.sms-row {
  display: flex;
  gap: 10px;
}

.sms-row .el-input {
  flex: 1;
}

.sms-row .el-button {
  white-space: nowrap;
  min-width: 130px;
}
</style>
