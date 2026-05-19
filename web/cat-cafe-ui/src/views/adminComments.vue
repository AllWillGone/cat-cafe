<template>
  <div>
    <h3>评论审核管理</h3>

    <div class="toolbar">
      <el-select v-model="auditStatus" style="width: 140px" @change="search">
        <el-option label="全部评论" :value="null" />
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 评论ID / 评论内容"
        clearable
        style="width: 300px; margin-left: 12px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button type="primary" style="margin-left: 12px" @click="search">搜索</el-button>
    </div>

    <el-table :data="comments" border v-loading="loading" style="margin-top: 16px">
      <el-table-column prop="commentId" label="ID" width="70" />
      <el-table-column label="评论对象" width="120">
        <template #default="{ row }">
          <el-tag :type="row.targetType === 0 ? 'warning' : ''" size="small">
            {{ row.targetType === 0 ? '商品' : '猫咪' }}
          </el-tag>
          <span style="margin-left: 4px">#{{ row.targetId }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" width="110" />
      <el-table-column prop="content" label="评论内容" min-width="220" show-overflow-tooltip />
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag
            :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'info'"
            size="small"
          >
            {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已拒绝' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.auditStatus !== 1"
            size="small"
            type="success"
            @click="audit(row.commentId, 1)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.auditStatus !== 2"
            size="small"
            type="danger"
            @click="audit(row.commentId, 2)"
          >
            拒绝
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="删除" width="80" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="danger"
            plain
            @click="handleDelete(row.commentId)"
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
        @current-change="fetchComments"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const comments = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const auditStatus = ref(null)
const keyword = ref('')

const fetchComments = async () => {
  loading.value = true
  try {
    const params = {
      skip: (page.value - 1) * pageSize.value,
      limit: pageSize.value,
    }
    if (auditStatus.value !== null) params.auditStatus = auditStatus.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/api/admin/comments', { params })
    comments.value = res.data.items
    total.value = res.data.total
  } catch {
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  fetchComments()
}

const audit = async (commentId, status) => {
  try {
    await api.put(`/api/admin/comments/${commentId}/audit`, { auditStatus: status })
    ElMessage.success(status === 1 ? '审核通过' : '已拒绝')
    fetchComments()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '审核失败')
  }
}

const handleDelete = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await api.delete(`/api/comments/${commentId}`)
    ElMessage.success('评论已删除')
    fetchComments()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '删除失败')
  }
}

onMounted(() => {
  fetchComments()
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
