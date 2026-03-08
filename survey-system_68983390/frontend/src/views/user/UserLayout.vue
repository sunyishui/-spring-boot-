<template>
  <el-container style="height: 100vh">
    <el-header style="display: flex; align-items: center; background: #409eff; padding: 0 20px">
      <div class="header-title">电子问卷系统</div>
      <el-menu :default-active="activeMenu" mode="horizontal" router background-color="#409eff" text-color="#fff" active-text-color="#fff" style="border: none; flex: 1; margin-left: 40px">
        <el-menu-item index="/surveys">问卷列表</el-menu-item>
        <el-menu-item index="/my-answers">我的答卷</el-menu-item>
      </el-menu>
      <div class="header-right">
        <span style="color: #fff; margin-right: 16px">{{ userStore.nickname }}</span>
        <el-button type="warning" plain size="small" @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-main style="background: #f5f7fa; padding: 20px">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.header-title {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  white-space: nowrap;
}
.header-right {
  display: flex;
  align-items: center;
  white-space: nowrap;
}
</style>
