<template>
  <div class="page" v-loading="loading">
    <div style="margin-bottom:16px">
      <el-button @click="$router.push('/home/cats')" text>
        &larr; 返回猫咪列表
      </el-button>
    </div>

    <el-card v-if="cat.catId">
      <div class="detail-layout">
        <div class="photo-section">
          <img
            :src="cat.photoUrl"
            class="detail-photo"
            @click="previewImage(cat.photoUrl)"
            @error="(e) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex'; }"
          />
          <span class="photo-fallback" style="font-size:80px;display:none">🐱</span>
        </div>
        <div class="info-section">
          <h2>{{ cat.catName }}</h2>
          <div class="info-row"><span class="label">品种</span><span>{{ cat.breed || '-' }}</span></div>
          <div class="info-row"><span class="label">生日</span><span>{{ cat.birthday || '-' }}</span></div>
          <div class="info-row">
            <span class="label">状态</span>
            <el-tag :type="cat.status === 1 ? 'success' : 'info'" size="small">
              {{ cat.status === 1 ? '在岗' : '休息中' }}
            </el-tag>
          </div>
          <div class="info-row"><span class="label">性格</span><span>{{ cat.personality || '-' }}</span></div>
          <div class="info-row" v-if="cat.notes"><span class="label">备注</span><span>{{ cat.notes }}</span></div>
          <div class="detail-actions">
            <el-tooltip :content="cat._liked ? '点击取消点赞' : ''" :disabled="!cat._liked" placement="top">
              <el-button @click="toggleLike" :class="{ 'is-liked': cat._liked }">
                <svg viewBox="0 0 24 24" width="16" height="16" :fill="cat._liked ? '#f56c6c' : 'none'" :stroke="cat._liked ? '#f56c6c' : '#606266'" stroke-width="2" style="margin-right:5px;vertical-align:middle">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                {{ cat.likeCount || 0 }}
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 评论区 -->
    <el-card style="margin-top:20px">
      <template #header>
        <h3>评论 ({{ comments.length }})</h3>
      </template>
      <div class="comment-form">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          maxlength="500"
          show-word-limit
        />
        <el-button type="primary" style="margin-top:8px" :loading="submitting" @click="submitComment">
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
    </el-card>

    <!-- 图片预览遮罩 -->
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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api/index'
import { ElMessage } from 'element-plus'

const route = useRoute()
const catId = route.params.id

const cat = ref({})
const loading = ref(false)

const fetchCat = async () => {
  loading.value = true
  try {
    const [catRes, myLikesRes] = await Promise.all([
      api.get(`/api/cats/${catId}`),
      api.get('/api/likes', { params: { likeType: 2 } }),
    ])
    const likeMap = new Map(myLikesRes.data.items.map(l => [l.objectId, l.likeId]))
    cat.value = {
      ...catRes.data,
      _liked: likeMap.has(catRes.data.catId),
      _likeId: likeMap.get(catRes.data.catId) || null,
    }
  } catch {
    ElMessage.error('获取猫咪详情失败')
  } finally {
    loading.value = false
  }
}

const toggleLike = async () => {
  try {
    if (cat.value._liked) {
      await api.delete(`/api/likes/${cat.value._likeId}`)
      cat.value._liked = false
      cat.value._likeId = null
      cat.value.likeCount = Math.max(0, (cat.value.likeCount || 0) - 1)
      ElMessage.success('已取消点赞')
    } else {
      const res = await api.post('/api/likes', { likeType: 2, objectId: cat.value.catId })
      cat.value._liked = true
      cat.value._likeId = res.data.likeId
      cat.value.likeCount = (cat.value.likeCount || 0) + 1
      ElMessage.success('点赞成功')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '操作失败')
  }
}

const imagePreviewVisible = ref(false)
const imagePreviewUrl = ref('')

const previewImage = (url) => {
  imagePreviewUrl.value = url
  imagePreviewVisible.value = true
}

// ── 评论 ──
const comments = ref([])
const newComment = ref('')
const submitting = ref(false)

const fetchComments = async () => {
  try {
    const [commentRes, myLikesRes] = await Promise.all([
      api.get('/api/comments', { params: { targetType: 1, targetId: catId } }),
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
      targetId: Number(catId),
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

onMounted(() => { fetchCat(); fetchComments() })
</script>

<style scoped>
.page { max-width: 860px; margin: 0 auto; }
.detail-layout { display: flex; gap: 32px; flex-wrap: wrap; }
.photo-section { flex-shrink: 0; }
.detail-photo {
  width: 100%;
  max-width: 360px;
  height: auto;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  display: block;
}
.photo-fallback {
  width: 360px;
  height: 280px;
  border-radius: 8px;
  background: #f5f5f5;
  align-items: center;
  justify-content: center;
}
.info-section { flex: 1; min-width: 260px; }
.info-section h2 { margin: 0 0 20px 0; font-size: 24px; }
.info-row { display: flex; align-items: center; margin-bottom: 12px; }
.info-row .label { width: 60px; color: #999; font-size: 14px; flex-shrink: 0; }
.info-row span:last-child { color: #333; font-size: 14px; }
.detail-actions { margin-top: 24px; }
.is-liked { color: #f56c6c; border-color: #f56c6c; }
.comment-form { margin-bottom: 8px; }
.comment-list { max-height: 500px; overflow-y: auto; }
.comment-item { padding: 12px 0; border-bottom: 1px solid #eee; }
.comment-item:last-child { border-bottom: none; }
.comment-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.comment-user { font-weight: 500; color: #333; }
.comment-time { font-size: 12px; color: #999; }
.comment-body { color: #555; line-height: 1.6; }
.comment-actions { margin-top: 6px; }
</style>

<style>
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
.image-preview-close:hover { background: rgba(255, 255, 255, 0.3); }
</style>
