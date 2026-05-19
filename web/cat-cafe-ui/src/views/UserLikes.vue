<template>
  <div class="page">
    <h3>我的点赞</h3>
    <div class="toolbar">
      <el-select v-model="likeType" style="width: 120px" @change="fetchLikes">
        <el-option label="全部类型" :value="null" />
        <el-option label="商品" :value="0" />
        <el-option label="评论" :value="1" />
        <el-option label="猫咪" :value="2" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索点赞对象"
        clearable
        style="width: 200px; margin-left: 12px"
        @keyup.enter="fetchLikes"
        @clear="fetchLikes"
      />
      <el-button type="primary" style="margin-left: 12px" @click="fetchLikes">搜索</el-button>
    </div>
    <el-table
      :data="likes"
      border
      v-loading="loading"
      style="margin-top: 16px"
      @row-click="goTarget"
    >
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ ['商品','评论','猫咪'][row.likeType] || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="objectName" label="点赞对象" min-width="200" />
      <el-table-column prop="createTime" label="点赞时间" width="170" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click.stop="handleUnlike(row.likeId)">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="#f56c6c" stroke="#f56c6c" stroke-width="2" style="margin-right:3px;vertical-align:middle">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/index'
import { ElMessage } from 'element-plus'

const router = useRouter()

const likes = ref([])
const loading = ref(false)
const likeType = ref(null)
const keyword = ref('')

const fetchLikes = async () => {
  loading.value = true
  try {
    const params = {}
    if (likeType.value !== null) params.likeType = likeType.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/api/likes', { params })
    likes.value = res.data.items
  } catch {
    ElMessage.error('获取点赞列表失败')
  } finally {
    loading.value = false
  }
}

const handleUnlike = async (likeId) => {
  try {
    await api.delete(`/api/likes/${likeId}`)
    ElMessage.success('已取消点赞')
    fetchLikes()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '取消失败')
  }
}

const goTarget = (row) => {
  if (row.likeType === 0) {
    router.push(`/home/products/${row.objectId}`)
  } else if (row.likeType === 1) {
    if (row.targetType === 0) router.push(`/home/products/${row.targetId}`)
    else if (row.targetType === 1) router.push(`/home/cats/${row.targetId}`)
  } else if (row.likeType === 2) {
    router.push(`/home/cats/${row.objectId}`)
  }
}

onMounted(() => fetchLikes())
</script>

<style scoped>
.page { max-width: 900px; margin: 0 auto; }
.toolbar { display: flex; align-items: center; margin-bottom: 0; }
</style>

<style>
.el-table__body .el-table__row { cursor: pointer; }
</style>
