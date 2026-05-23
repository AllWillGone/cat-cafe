<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>用户登录</h2>
      </template>

      <!-- 登录模式切换 -->
      <div class="login-tabs">
        <span
          :class="['tab-item', { active: loginMode === 'username' }]"
          @click="loginMode = 'username'"
        >用户名登录</span>
        <span
          :class="['tab-item', { active: loginMode === 'phone' }]"
          @click="loginMode = 'phone'"
        >手机号登录</span>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        @submit.prevent="handleLogin"
      >
        <el-form-item v-if="loginMode === 'username'" label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item v-if="loginMode === 'phone'" label="手机号" prop="userPhone">
          <el-input v-model="form.userPhone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="密码" prop="userPassword">
          <el-input
            v-model="form.userPassword"
            type="password"
            placeholder="请输入密码"
            show-password
          />
          <div style="margin-top:4px;text-align:right">
            <el-button link type="primary" @click="openResetDialog">忘记密码？</el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">
            登录
          </el-button>
          <el-button @click="$router.push('/signup')">
            没有账号？去注册
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 忘记密码弹窗 — 手机号 + 短信验证码 -->
    <el-dialog v-model="resetDialogVisible" title="手机号找回密码" width="420px">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="80px">
        <el-form-item label="手机号" prop="userPhone">
          <div class="sms-row">
            <el-input v-model="resetForm.userPhone" placeholder="请输入注册手机号" />
            <el-button
              :disabled="smsCountdown > 0"
              :loading="sendingSms"
              @click="sendResetSms"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <el-input v-model="resetForm.code" placeholder="6位验证码" maxlength="6" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="至少6位" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="handleResetPassword">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/index'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const loginMode = ref('username')

const form = reactive({
  userName: '',
  userPhone: '',
  userPassword: '',
})

const rules = computed(() => {
  if (loginMode.value === 'username') {
    return {
      userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    }
  } else {
    return {
      userPhone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' },
      ],
      userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    }
  }
})

const saveUserInfo = (data) => {
  localStorage.setItem('token', data.token)
  localStorage.setItem('userId', data.userId)
  localStorage.setItem('userName', data.userName)
  localStorage.setItem('userType', data.userType)
}

const navigateAfterLogin = (userType) => {
  if (userType === 1) {
    router.push('/admin')
  } else {
    router.push('/home')
  }
}

const handleLogin = async () => {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return

  loading.value = true
  try {
    const body = { userPassword: form.userPassword }
    if (loginMode.value === 'username') {
      body.userName = form.userName
    } else {
      body.userPhone = form.userPhone
    }
    const res = await api.post('/api/login', body)
    ElMessage.success(`登录成功！欢迎回来，${res.data.userName}`)
    saveUserInfo(res.data)
    navigateAfterLogin(res.data.userType)
  } catch (err) {
    const detail = err.response?.data?.detail
    ElMessage.error(detail || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// ── 忘记密码 ──
const resetDialogVisible = ref(false)
const resetFormRef = ref(null)
const resetting = ref(false)
const sendingSms = ref(false)
const smsCountdown = ref(0)
const resetForm = reactive({
  userPhone: '',
  code: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  userPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const openResetDialog = () => {
  resetForm.userPhone = ''
  resetForm.code = ''
  resetForm.newPassword = ''
  resetForm.confirmPassword = ''
  smsCountdown.value = 0
  resetDialogVisible.value = true
}

const sendResetSms = async () => {
  const valid = await resetFormRef.value.validateField('userPhone').catch(() => false)
  if (!valid) return

  sendingSms.value = true
  try {
    const res = await api.post('/api/send-sms-code', {
      userPhone: resetForm.userPhone,
    })
    // 演示模式：后端直接返回验证码
    ElMessage.success(`验证码已发送（演示：${res.data.code}）`)
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '发送失败')
  } finally {
    sendingSms.value = false
  }
}

const handleResetPassword = async () => {
  const ok = await resetFormRef.value.validate().catch(() => false)
  if (!ok) return
  resetting.value = true
  try {
    await api.post('/api/reset-password-by-phone', {
      userPhone: resetForm.userPhone,
      code: resetForm.code,
      newPassword: resetForm.newPassword,
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    resetDialogVisible.value = false
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '重置密码失败')
  } finally {
    resetting.value = false
  }
}

</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(243, 163, 92, 0.15), rgba(255, 207, 154, 0.3)),
              url('../assets/hero.png') center/cover no-repeat;
}

.login-card {
  width: 440px;
}

.login-tabs {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.tab-item {
  cursor: pointer;
  color: #909399;
  font-size: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid transparent;
  margin-bottom: -11px;
  transition: color 0.2s, border-color 0.2s;
}

.tab-item.active {
  color: #F3A35C;
  border-bottom-color: #F3A35C;
  font-weight: 500;
}

.tab-item:hover {
  color: #F3A35C;
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
  min-width: 110px;
}
</style>
