<template>
  <div class="page">
    <h3>我的点评</h3>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="我的点赞" name="likes">
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
          v-loading="loadingLikes"
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
      </el-tab-pane>

      <el-tab-pane label="我的评论" name="comments">
        <div class="toolbar">
          <el-select v-model="commentTargetType" style="width: 120px" @change="fetchMyComments">
            <el-option label="全部类型" :value="null" />
            <el-option label="商品" :value="0" />
            <el-option label="猫咪" :value="1" />
          </el-select>
          <el-button type="primary" style="margin-left: 12px" @click="fetchMyComments">刷新</el-button>
        </div>
        <el-table
          :data="myComments"
          border
          v-loading="loadingComments"
          style="margin-top: 16px"
          @row-click="goCommentTarget"
        >
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag size="small">{{ row.targetType === 0 ? '商品' : '猫咪' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="内容" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span :style="{ color: row.auditStatus !== 1 ? '#999' : '#333' }">{{ row.content }}</span>
            </template>
          </el-table-column>
          <el-table-column label="审核" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'info'">
                {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已拒绝' : '待审核' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="publishTime" label="评论时间" width="170" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click.stop="handleDeleteComment(row.commentId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const activeTab = ref('likes')

// ── 点赞 tab ──
const likes = ref([])
const loadingLikes = ref(false)
const likeType = ref(null)
const keyword = ref('')

const fetchLikes = async () => {
  loadingLikes.value = true
  try {
    const params = {}
    if (likeType.value !== null) params.likeType = likeType.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/api/likes', { params })
    likes.value = res.data.items
  } catch {
    ElMessage.error('获取点赞列表失败')
  } finally {
    loadingLikes.value = false
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

// ── 评论 tab ──
const myComments = ref([])
const loadingComments = ref(false)
const commentTargetType = ref(null)

const fetchMyComments = async () => {
  loadingComments.value = true
  try {
    const params = {}
    if (commentTargetType.value !== null) params.targetType = commentTargetType.value
    const res = await api.get('/api/comments/my', { params })
    myComments.value = res.data.items
  } catch {
    ElMessage.error('获取评论列表失败')
  } finally {
    loadingComments.value = false
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch { return }
  try {
    await api.delete(`/api/comments/${commentId}`)
    ElMessage.success('评论已删除')
    fetchMyComments()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '删除失败')
  }
}

const goCommentTarget = (row) => {
  if (row.targetType === 0) router.push(`/home/products/${row.targetId}`)
  else if (row.targetType === 1) router.push(`/home/cats/${row.targetId}`)
}

const onTabChange = (tab) => {
  if (tab === 'comments') fetchMyComments()
}

onMounted(() => fetchLikes())
</script>

<style scoped>
.page { max-width: 900px; margin: 0 auto; }
.toolbar { display: flex; align-items: center; }
</style>

<style>
.el-table__body .el-table__row { cursor: pointer; }
</style>
