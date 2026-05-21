<template>
  <div>
    <h3>猫咪管理</h3>

    <div class="toolbar">
      <el-select v-model="status" style="width: 140px" @change="search">
        <el-option label="全部状态" :value="null" />
        <el-option label="在岗中" :value="1" />
        <el-option label="休息中" :value="0" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索猫名 / 品种 / 性格"
        clearable
        style="width: 260px; margin-left: 12px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button type="primary" style="margin-left: 12px" @click="search">搜索</el-button>
      <el-button type="success" style="margin-left: auto" @click="openAdd">新增猫咪</el-button>
    </div>

    <el-table :data="cats" border v-loading="loading" style="margin-top: 16px">
      <el-table-column prop="catId" label="ID" width="60" />
      <el-table-column prop="catName" label="名字" width="120" />
      <el-table-column prop="breed" label="品种" width="100" />
      <el-table-column prop="birthday" label="生日" width="120" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '在岗中' : '休息中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="personality" label="性格" min-width="140" show-overflow-tooltip />
      <el-table-column prop="photoUrl" label="照片URL" width="180" show-overflow-tooltip />
      <el-table-column prop="notes" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '设为休息' : '设为在岗' }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="删除" width="80" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="danger"
            plain
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchCats"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑猫咪' : '新增猫咪'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名字">
          <el-input v-model="form.catName" maxlength="50" />
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="form.breed" maxlength="50" />
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="在岗中"
            inactive-text="休息中"
          />
        </el-form-item>
        <el-form-item label="性格">
          <el-input v-model="form.personality" type="textarea" maxlength="500" />
        </el-form-item>
        <el-form-item label="照片">
          <div style="display: flex; gap: 8px; align-items: center; width: 100%">
            <el-input v-model="form.photoUrl" placeholder="上传或手动输入路径" maxlength="255" style="flex:1" />
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="doUpload"
              accept="image/*"
            >
              <el-button :loading="uploadingPhoto">选择文件</el-button>
            </el-upload>
          </div>
          <img v-if="form.photoUrl" :src="form.photoUrl" class="upload-preview" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCat">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const uploadingPhoto = ref(false)

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const doUpload = async (options) => {
  uploadingPhoto.value = true
  try {
    const fd = new FormData()
    fd.append('file', options.file)
    fd.append('type', 'cat')
    const res = await api.post('/api/admin/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    form.photoUrl = res.data.url
    ElMessage.success('上传成功')
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '上传失败')
  } finally {
    uploadingPhoto.value = false
  }
}

const cats = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const status = ref(null)
const keyword = ref('')

const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const form = reactive({
  catName: '',
  breed: '',
  birthday: null,
  status: 1,
  personality: '',
  photoUrl: '',
  notes: '',
})

const fetchCats = async () => {
  loading.value = true
  try {
    const params = {
      skip: (page.value - 1) * pageSize.value,
      limit: pageSize.value,
    }
    if (status.value !== null) params.status = status.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/api/admin/cats', { params })
    cats.value = res.data.items
    total.value = res.data.total
  } catch {
    ElMessage.error('获取猫咪列表失败')
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  fetchCats()
}

const resetForm = () => {
  form.catName = ''
  form.breed = ''
  form.birthday = null
  form.status = 1
  form.personality = ''
  form.photoUrl = ''
  form.notes = ''
}

const openAdd = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  editingId.value = row.catId
  form.catName = row.catName
  form.breed = row.breed
  form.birthday = row.birthday
  form.status = row.status
  form.personality = row.personality
  form.photoUrl = row.photoUrl
  form.notes = row.notes
  dialogVisible.value = true
}

const saveCat = async () => {
  saving.value = true
  try {
    const body = {
      catName: form.catName,
      breed: form.breed,
      birthday: form.birthday,
      status: form.status,
      personality: form.personality,
      photoUrl: form.photoUrl,
      notes: form.notes,
    }
    if (isEdit.value) {
      await api.put(`/api/admin/cats/${editingId.value}`, body)
      ElMessage.success('猫咪信息已更新')
    } else {
      await api.post('/api/admin/cats', body)
      ElMessage.success('猫咪已添加')
    }
    dialogVisible.value = false
    fetchCats()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '保存失败')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await api.put(`/api/admin/cats/${row.catId}`, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '已设为在岗中' : '已设为休息中')
    fetchCats()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '更新失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除猫咪「${row.catName}」吗？`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await api.delete(`/api/admin/cats/${row.catId}`)
    ElMessage.success('猫咪已删除')
    fetchCats()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '删除失败')
  }
}

onMounted(() => {
  fetchCats()
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

.upload-preview {
  max-width: 200px;
  max-height: 120px;
  margin-top: 8px;
  border-radius: 4px;
  display: block;
}
</style>
