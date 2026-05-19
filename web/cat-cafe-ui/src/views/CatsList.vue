<template>
  <div style="padding: 20px">
    <h3>猫咪列表</h3>

    <el-table :data="cats" style="margin-top: 20px" border v-loading="loading">
      <el-table-column prop="catId" label="ID" width="80" />
      <el-table-column prop="catName" label="名字" />
      <el-table-column prop="breed" label="品种" />
      <el-table-column prop="personality" label="性格" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '在岗' : '休息中' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage } from 'element-plus'

const cats = ref([])
const loading = ref(false)

const fetchCats = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/cats')
    cats.value = res.data.items
  } catch {
    ElMessage.error('获取猫咪列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCats()
})
</script>
