<template>
  <div class="page">
    <h3>猫咪列表</h3>
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索猫名 / 品种 / 性格"
        clearable
        style="width: 280px"
        @keyup.enter="fetchCats"
        @clear="fetchCats"
      />
      <el-button type="primary" style="margin-left: 12px" @click="fetchCats">搜索</el-button>
    </div>
    <el-table
      :data="cats"
      border
      v-loading="loading"
      style="margin-top: 16px"
      @row-click="(row) => $router.push(`/home/cats/${row.catId}`)"
    >
      <el-table-column prop="catId" label="ID" width="60" />
      <el-table-column label="照片" width="90">
        <template #default="{ row }">
          <img
            :src="row.photoUrl"
            fit="cover"
            style="width:60px;height:60px;border-radius:6px;object-fit:cover;cursor:pointer"
            @click="previewImage(row.photoUrl)"
            @error="(e) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'inline'; }"
          />
          <span style="font-size:28px;display:none">🐱</span>
        </template>
      </el-table-column>
      <el-table-column prop="catName" label="名字" width="120" />
      <el-table-column prop="breed" label="品种" width="100" />
      <el-table-column prop="birthday" label="生日" width="120" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '在岗' : '休息中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="personality" label="性格" min-width="100" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-tooltip :content="row._liked ? '点击取消点赞' : ''" :disabled="!row._liked" placement="top">
            <el-button size="small" @click.stop="handleLikeCat(row)" :class="{ 'is-liked': row._liked }">
              <svg viewBox="0 0 24 24" width="14" height="14" :fill="row._liked ? '#f56c6c' : 'none'" :stroke="row._liked ? '#f56c6c' : '#606266'" stroke-width="2" style="margin-right:4px;vertical-align:middle">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              {{ row.likeCount || 0 }}
            </el-button>
          </el-tooltip>
          <el-button size="small" type="primary" @click.stop="openCommentDialog(row)">评论</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="commentDialogVisible" :title="`${currentCat.catName} 的评论`" width="600px">
      <div class="comment-form">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          maxlength="500"
          show-word-limit
        />
        <el-button type="primary" style="margin-top: 8px" :loading="submitting" @click="submitComment">
          发表评论
        </el-button>
      </div>

      <el-divider />

      <div v-if="comments.length === 0" style="color:#999;text-align:center;padding:20px 0">暂无评论</div>
      <div v-else class="comment-list">
        <div v-for="c in comments" :key="c.commentId" class="comment-item">
          <div class="comment-head">
            <span class="comment-user">{{ c.userName || '匿名用户' }}</span>
            <span class="comment-time">{{ c.publishTime }}</span>
          </div>
          <div class="comment-body">{{ c.content }}</div>
          <div class="comment-actions">
            <el-tooltip :content="c._liked ? '点击取消点赞' : ''" :disabled="!c._liked" placement="top">
              <el-button size="small" text @click="handleLikeComment(c)" :class="{ 'is-liked': c._liked }">
                <svg viewBox="0 0 24 24" width="13" height="13" :fill="c._liked ? '#f56c6c' : 'none'" :stroke="c._liked ? '#f56c6c' : '#909399'" stroke-width="2" style="margin-right:3px;vertical-align:middle">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                {{ c._liked ? '已赞' : '点赞' }} ({{ c._likeCount || 0 }})
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 图片预览遮罩层 -->
    <teleport to="body">
      <div
        v-if="imagePreviewVisible"
        class="image-preview-overlay"
        @click.self="imagePreviewVisible = false"
      >
        <img :src="imagePreviewUrl" class="image-preview-img" />
        <button class="image-preview-close" @click="imagePreviewVisible = false">✕</button>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import api from '../api/index'
import { ElMessage } from 'element-plus'

const cats = ref([])
const loading = ref(false)
const keyword = ref('')

const fetchCats = async () => {
  loading.value = true
  try {
    const params = {}
    if (keyword.value) params.keyword = keyword.value
    const [catRes, myLikesRes] = await Promise.all([
      api.get('/api/cats', { params }),
      api.get('/api/likes', { params: { likeType: 2 } }),
    ])
    const likeMap = new Map(myLikesRes.data.items.map(l => [l.objectId, l.likeId]))
    cats.value = catRes.data.items.map(item => ({
      ...item,
      _liked: likeMap.has(item.catId),
      _likeId: likeMap.get(item.catId) || null,
    }))
  } catch {
    ElMessage.error('获取猫咪列表失败')
  } finally {
    loading.value = false
  }
}

const imagePreviewVisible = ref(false)
const imagePreviewUrl = ref('')

const previewImage = (url) => {
  imagePreviewUrl.value = url
  imagePreviewVisible.value = true
}

const handleLikeCat = async (row) => {
  try {
    if (row._liked) {
      await api.delete(`/api/likes/${row._likeId}`)
      row._liked = false
      row._likeId = null
      row.likeCount = Math.max(0, (row.likeCount || 0) - 1)
      ElMessage.success('已取消点赞')
    } else {
      const res = await api.post('/api/likes', { likeType: 2, objectId: row.catId })
      row._liked = true
      row._likeId = res.data.likeId
      row.likeCount = (row.likeCount || 0) + 1
      ElMessage.success('点赞成功')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '操作失败')
  }
}

const commentDialogVisible = ref(false)
const currentCat = reactive({ catId: 0, catName: '' })
const comments = ref([])
const newComment = ref('')
const submitting = ref(false)

const openCommentDialog = async (row) => {
  currentCat.catId = row.catId
  currentCat.catName = row.catName
  newComment.value = ''
  commentDialogVisible.value = true
  await fetchComments()
}

const fetchComments = async () => {
  try {
    const [commentRes, myLikesRes] = await Promise.all([
      api.get('/api/comments', {
        params: { targetType: 1, targetId: currentCat.catId },
      }),
      api.get('/api/likes', { params: { likeType: 1 } }),
    ])
    const likeMap = new Map(myLikesRes.data.items.map(l => [l.objectId, l.likeId]))
    comments.value = commentRes.data.items.map(c => ({
      ...c,
      _liked: likeMap.has(c.commentId),
      _likeId: likeMap.get(c.commentId) || null,
      _likeCount: c.likeCount || 0,
    }))
  } catch {
    comments.value = []
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await api.post('/api/comments', {
      targetType: 1,
      targetId: currentCat.catId,
      content: newComment.value.trim(),
    })
    ElMessage.success('评论发表成功，等待审核')
    newComment.value = ''
    await fetchComments()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '评论发表失败')
  } finally {
    submitting.value = false
  }
}

const handleLikeComment = async (c) => {
  try {
    if (c._liked) {
      await api.delete(`/api/likes/${c._likeId}`)
      c._liked = false
      c._likeId = null
      c._likeCount = Math.max(0, (c._likeCount || 0) - 1)
      ElMessage.success('已取消点赞')
    } else {
      const res = await api.post('/api/likes', { likeType: 1, objectId: c.commentId })
      c._liked = true
      c._likeId = res.data.likeId
      c._likeCount = (c._likeCount || 0) + 1
      ElMessage.success('点赞成功')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '操作失败')
  }
}

onMounted(() => fetchCats())
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; }
.toolbar { display: flex; align-items: center; }
.comment-form { margin-bottom: 8px; }
.comment-list { max-height: 400px; overflow-y: auto; }
.comment-item { padding: 12px 0; border-bottom: 1px solid #eee; }
.comment-item:last-child { border-bottom: none; }
.comment-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.comment-user { font-weight: 500; color: #333; }
.comment-time { font-size: 12px; color: #999; }
.comment-body { color: #555; line-height: 1.6; }
.comment-actions { margin-top: 6px; }
.is-liked { color: #f56c6c; border-color: #f56c6c; }
</style>

<style>
.el-table__body .el-table__row { cursor: pointer; }
.image-preview-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
}
.image-preview-img {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
}
.image-preview-close {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 44px;
  height: 44px;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-size: 24px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}
.image-preview-close:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
