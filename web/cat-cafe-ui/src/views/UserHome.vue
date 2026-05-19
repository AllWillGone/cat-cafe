<template>
  <el-container class="user-layout">
    <el-header class="user-header">
      <span class="logo">🐱 猫咖</span>
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        router
        class="nav-menu"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/home/cats">
          <el-icon><Avatar /></el-icon>
          <span>猫咪</span>
        </el-menu-item>
        <el-menu-item index="/home/products">
          <el-icon><Goods /></el-icon>
          <span>商品</span>
        </el-menu-item>
        <el-menu-item index="/home/orders">
          <el-icon><List /></el-icon>
          <span>订单</span>
        </el-menu-item>
        <el-menu-item index="/home/likes">
          <el-icon><Star /></el-icon>
          <span>点赞</span>
        </el-menu-item>
        <el-menu-item index="/home/profile">
          <el-icon><User /></el-icon>
          <span>信息</span>
        </el-menu-item>
      </el-menu>
      <div class="header-right">
        <span class="profile-trigger" @click="goProfile">
          <el-avatar :size="32" :src="avatarUrl" v-if="avatarUrl" />
          <el-avatar :size="32" v-else><span>🐱</span></el-avatar>
          <span class="user-name">{{ userName }}</span>
        </span>
        <el-button text @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, Avatar, Goods, List, Star, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userName = computed(() => localStorage.getItem('userName') || '用户')
const avatarUrl = ref(localStorage.getItem('userAvatar') || '')

onMounted(async () => {
  try {
    const { default: api } = await import('../api/index')
    const res = await api.get('/api/user/me')
    const avatar = res.data.userAvatar
    if (avatar) {
      avatarUrl.value = avatar
      localStorage.setItem('userAvatar', avatar)
    }
  } catch {}
})

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/home/cats')) return '/home/cats'
  if (path.startsWith('/home/products')) return '/home/products'
  if (path.startsWith('/home/orders')) return '/home/orders'
  if (path.startsWith('/home/likes')) return '/home/likes'
  if (path.startsWith('/home/profile')) return '/home/profile'
  return '/home'
})

const goProfile = () => {
  router.push('/home/profile')
}

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.user-layout { min-height: 100vh; background: #f5f5f5; }
.user-header { display: flex; align-items: center; background: #fff; border-bottom: 1px solid #e6e6e6; padding: 0 20px; height: 60px; }
.logo { font-size: 18px; font-weight: bold; color: #409EFF; margin-right: 30px; white-space: nowrap; }
.nav-menu { flex: 1; border-bottom: none !important; }
.nav-menu .el-menu-item { height: 60px; line-height: 60px; }
.header-right { display: flex; align-items: center; gap: 8px; margin-left: 20px; }
.profile-trigger { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-name { font-size: 14px; color: #333; }
</style>
