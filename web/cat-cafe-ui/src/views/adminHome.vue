<template>
  <el-container class="admin-layout">
    <!-- 左侧菜单 -->
    <el-aside width="220px">
      <div class="logo">猫咖管理后台</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户账户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotSquare /></el-icon>
          <span>评论审核管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/cats">
          <el-icon><Avatar /></el-icon>
          <span>猫咪管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="admin-header">
        <span class="header-title">猫咖管理系统</span>
        <div class="header-right">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="user-name">{{ userName }}</span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, ChatDotSquare, List, Goods, Avatar, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userName = computed(() => localStorage.getItem('userName') || '管理员')

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  overflow: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-size: 14px;
  color: #333;
}
</style>
