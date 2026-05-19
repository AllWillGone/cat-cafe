<template>
  <div class="page">
    <el-card>
      <template #header>
        <h3>个人信息</h3>
      </template>
      <el-form :model="form" label-width="80px" v-loading="loading" style="max-width: 440px">
        <el-form-item label="头像">
          <div class="avatar-section">
            <el-avatar :size="80" :src="form.userAvatar" v-if="form.userAvatar">
              <span style="font-size:30px">🐱</span>
            </el-avatar>
            <el-avatar :size="80" v-else>
              <span style="font-size:30px">🐱</span>
            </el-avatar>
            <div style="margin-top:8px">
              <el-input v-model="form.userAvatar" placeholder="输入头像URL" maxlength="255" style="width:260px" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input :model-value="profile.userId" disabled />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.userName" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.userPhone" maxlength="20" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
            <el-radio :value="null">未设置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="注册时间">
          <el-input :model-value="profile.registerTime" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header>
        <h3>修改密码</h3>
      </template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px" style="max-width:440px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="至少6位" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage } from 'element-plus'

const profile = ref({})
const loading = ref(false)
const saving = ref(false)
const form = reactive({
  userName: '',
  userPhone: '',
  gender: null,
  birthday: null,
  userAvatar: '',
})

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/user/me')
    profile.value = res.data
    form.userName = res.data.userName
    form.userPhone = res.data.userPhone || ''
    form.gender = res.data.gender
    form.birthday = res.data.birthday
    form.userAvatar = res.data.userAvatar || ''
  } catch {
    ElMessage.error('获取个人信息失败')
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    const body = {
      userName: form.userName,
      userPhone: form.userPhone || null,
      gender: form.gender,
      birthday: form.birthday || null,
      userAvatar: form.userAvatar || null,
    }
    const res = await api.put('/api/user/me', body)
    profile.value = res.data
    localStorage.setItem('userName', res.data.userName)
    if (res.data.userAvatar) localStorage.setItem('userAvatar', res.data.userAvatar)
    ElMessage.success('个人信息已更新')
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '更新失败')
  } finally {
    saving.value = false
  }
}

// ── 修改密码 ──
const pwdFormRef = ref(null)
const changingPwd = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validatePwdConfirm = (_rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const validateNewPassword = (_rule, value, callback) => {
  if (value && value === pwdForm.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validatePwdConfirm, trigger: 'blur' },
  ],
}

const changePassword = async () => {
  const ok = await pwdFormRef.value.validate().catch(() => false)
  if (!ok) return
  changingPwd.value = true
  try {
    await api.put('/api/user/me/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '密码修改失败')
  } finally {
    changingPwd.value = false
  }
}

onMounted(() => fetchProfile())
</script>

<style scoped>
.page { max-width: 600px; margin: 0 auto; }
.avatar-section { display: flex; flex-direction: column; align-items: center; }
</style>
