<template>
  <div>
    <h3>用户账户管理</h3>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 手机号"
        clearable
        style="width: 260px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="sortBy" style="width: 140px; margin-left: 12px" @change="search">
        <el-option label="按ID排序" value="userId" />
        <el-option label="按用户名排序" value="userName" />
        <el-option label="按注册时间排序" value="registerTime" />
      </el-select>
      <el-button style="margin-left: 8px" @click="toggleOrder">
        {{ sortOrder === 'asc' ? '升序 ↑' : '降序 ↓' }}
      </el-button>
      <el-button type="primary" style="margin-left: 8px" @click="search">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="users" border v-loading="loading" style="margin-top: 16px">
      <el-table-column prop="userId" label="用户ID" width="80" sortable="custom" />
      <el-table-column prop="userName" label="用户名" width="140" />
      <el-table-column prop="userPhone" label="手机号" width="140">
        <template #default="{ row }">
          {{ row.userPhone || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="用户类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.userType === 1 ? 'danger' : 'success'" size="small">
            {{ row.userType === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="80">
        <template #default="{ row }">
          {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="registerTime" label="注册时间" width="180" />
      <el-table-column label="操作" min-width="160">
        <template #default="{ row }">
          <el-button
            v-if="row.userType === 0"
            size="small"
            type="primary"
            @click="openEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.userType === 0"
            size="small"
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchUsers"
      />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editVisible" title="编辑用户信息" width="460px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.userName" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.userPhone" maxlength="20" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
            <el-radio :value="null">未设置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const sortBy = ref('userId')
const sortOrder = ref('desc')

const editVisible = ref(false)
const saving = ref(false)
const editingUserId = ref(null)
const editForm = reactive({
  userName: '',
  userPhone: '',
  gender: null,
  birthday: null,
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      skip: (page.value - 1) * pageSize.value,
      limit: pageSize.value,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value,
    }
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/api/admin/users', { params })
    users.value = res.data.items
    total.value = res.data.total
  } catch {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  fetchUsers()
}

const toggleOrder = () => {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  search()
}

const openEdit = (row) => {
  editingUserId.value = row.userId
  editForm.userName = row.userName
  editForm.userPhone = row.userPhone || ''
  editForm.gender = row.gender
  editForm.birthday = row.birthday
  editVisible.value = true
}

const saveEdit = async () => {
  saving.value = true
  try {
    const body = {}
    if (editForm.userName) body.userName = editForm.userName
    if (editForm.gender !== undefined) body.gender = editForm.gender
    body.userPhone = editForm.userPhone || null
    body.birthday = editForm.birthday || null
    await api.put(`/api/admin/users/${editingUserId.value}`, body)
    ElMessage.success('用户信息已更新')
    editVisible.value = false
    fetchUsers()
  } catch (err) {
    const detail = err.response?.data?.detail
    ElMessage.error(detail || '更新失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除用户「${row.userName}」吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await api.delete(`/api/admin/users/${row.userId}`)
    ElMessage.success('用户已删除')
    fetchUsers()
  } catch (err) {
    const detail = err.response?.data?.detail
    ElMessage.error(detail || '删除失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
