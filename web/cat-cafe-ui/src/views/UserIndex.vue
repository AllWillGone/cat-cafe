<template>
  <div class="home-page">
    <el-card class="welcome-card">
      <div class="welcome-content">
        <el-avatar :size="64" :src="avatarUrl" v-if="avatarUrl" />
        <el-avatar :size="64" v-else><span style="font-size:28px">🐱</span></el-avatar>
        <div class="welcome-text">
          <h2>欢迎来到猫咖，{{ userName }}！</h2>
          <p>来看看今天有哪些可爱的小猫咪在等你</p>
        </div>
      </div>
    </el-card>

    <h3 style="margin-top: 28px; margin-bottom: 16px;">🐾 在岗猫咪</h3>

    <div class="cat-gallery" v-loading="loading">
      <el-card
        v-for="cat in cats"
        :key="cat.catId"
        class="cat-card"
        shadow="hover"
        @click="$router.push(`/home/cats/${cat.catId}`)"
        style="cursor:pointer"
      >
        <img :src="cat.photoUrl" :alt="cat.catName" class="cat-photo" @error="onImgError" @click.stop="previewImage(cat.photoUrl)" />
        <div class="cat-info">
          <h4>{{ cat.catName }}</h4>
          <p class="cat-breed">{{ cat.breed }}</p>
          <p class="cat-personality">{{ cat.personality }}</p>
        </div>
      </el-card>
      <div v-if="!loading && cats.length === 0" style="color:#999;text-align:center;padding:40px 0">
        暂时没有在岗猫咪
      </div>
    </div>

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
import api from '../api/index'
import catPlaceholder from '../assets/hero.png'

const userName = ref(localStorage.getItem('userName') || '')
const avatarUrl = ref(localStorage.getItem('userAvatar') || '')
const cats = ref([])
const loading = ref(false)

const fetchCats = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/cats')
    cats.value = res.data.items
  } catch {
    // silent
  } finally {
    loading.value = false
  }
}

const onImgError = (e) => {
  e.target.src = catPlaceholder
}

const imagePreviewVisible = ref(false)
const imagePreviewUrl = ref('')

const previewImage = (url) => {
  imagePreviewUrl.value = url
  imagePreviewVisible.value = true
}

onMounted(async () => {
  fetchCats()
  try {
    const res = await api.get('/api/user/me')
    if (res.data.userAvatar) {
      avatarUrl.value = res.data.userAvatar
      localStorage.setItem('userAvatar', res.data.userAvatar)
    }
  } catch {}
})
</script>

<style scoped>
.home-page { max-width: 960px; margin: 0 auto; }

.welcome-card { margin-bottom: 0; }
.welcome-content { display: flex; align-items: center; gap: 20px; }
.welcome-text h2 { margin: 0 0 4px 0; }
.welcome-text p { margin: 0; color: #999; }

.cat-gallery { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }

.cat-card { overflow: hidden; }
.cat-photo { width: 100%; height: 180px; object-fit: cover; display: block; cursor: pointer; }
.cat-info { padding: 12px 4px 4px; }
.cat-info h4 { margin: 0 0 4px 0; font-size: 16px; color: #333; }
.cat-breed { margin: 0 0 6px 0; font-size: 13px; color: #666; }
.cat-personality { margin: 0; font-size: 13px; color: #888; line-height: 1.5; }
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
.image-preview-close:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
