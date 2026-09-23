<template>
  <div class="public-layout">
    <header class="site-header" :class="{ 'is-scrolled': scrolled }">
      <div class="xz-container header-inner">
        <BrandLogo @click="goHome" />

        <nav class="site-nav">
          <router-link to="/square" class="nav-link">项目广场</router-link>
          <router-link to="/experts" class="nav-link">导师与工程师</router-link>
          <a class="nav-link" @click.prevent="scrollToAbout">关于校栈</a>
        </nav>

        <div class="header-actions">
          <template v-if="userStore.isLogin">
            <el-dropdown @command="onCommand">
              <div class="user-chip">
                <el-avatar :size="30" :src="userStore.avatar">
                  {{ userStore.displayName.slice(0, 1) }}
                </el-avatar>
                <span class="user-chip-name">{{ userStore.displayName }}</span>
                <el-tag size="small" effect="plain">{{ roleText }}</el-tag>
                <el-icon class="chip-arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="console">
                    <el-icon><HomeFilled /></el-icon> 进入我的工作台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">免费注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="site-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="site-footer">
      <div class="xz-container footer-inner">
        <div class="footer-brand">
          <BrandLogo />
          <p class="footer-desc">
            校栈把「简历上说不清的能力」变成「看得见的项目」。上传源码、文档与演示视频，
            获得导师与企业工程师的逐项点评，让招聘方直接看见你的动手能力。
          </p>
        </div>
        <div class="footer-links">
          <div class="link-group">
            <h4>产品</h4>
            <router-link to="/square">项目广场</router-link>
            <router-link to="/experts">导师与工程师</router-link>
            <router-link to="/register">学生入驻</router-link>
          </div>
          <div class="link-group">
            <h4>面向角色</h4>
            <span>在校学生 · 作品集沉淀</span>
            <span>高校导师 / 企业工程师 · 点评背书</span>
            <span>招聘企业 · 人才查验</span>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <div class="xz-container">
          <span>校栈 · 应届生项目作品集与能力背书平台</span>
          <span class="footer-note">Demo 演示环境 · 数据仅用于功能展示</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BrandLogo from '@/components/BrandLogo.vue'
import { useUserStore } from '@/stores/user'
import { ROLE_HOME, ROLE_TEXT } from '@/utils/dict'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const scrolled = ref(false)
const roleText = computed(() => ROLE_TEXT[userStore.role] || '用户')

const onScroll = () => {
  scrolled.value = window.scrollY > 10
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

const goHome = () => router.push('/')

const scrollToAbout = () => {
  if (route.path !== '/') {
    router.push('/').then(() => {
      setTimeout(() => document.getElementById('about')?.scrollIntoView({ behavior: 'smooth' }), 200)
    })
  } else {
    document.getElementById('about')?.scrollIntoView({ behavior: 'smooth' })
  }
}

const onCommand = async (command) => {
  if (command === 'console') {
    router.push(ROLE_HOME[userStore.role] || '/')
  } else if (command === 'logout') {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  }
}
</script>

<style scoped>
.public-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid transparent;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

.site-header.is-scrolled {
  border-bottom-color: var(--xz-border-light);
  box-shadow: 0 2px 12px rgba(28, 43, 51, 0.05);
}

.header-inner {
  height: var(--xz-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.site-nav {
  display: flex;
  align-items: center;
  gap: 28px;
  flex: 1;
  margin-left: 32px;
}

.nav-link {
  font-size: 14px;
  color: var(--xz-text-secondary);
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 4px 0;
  position: relative;
}

.nav-link:hover {
  color: var(--xz-primary);
}

.nav-link.router-link-active {
  color: var(--xz-primary);
  font-weight: 500;
}

.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  border-radius: 2px;
  background: var(--xz-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: 24px;
  background: var(--xz-bg-hover);
  cursor: pointer;
  outline: none;
  transition: background 0.2s ease;
}

.user-chip:hover {
  background: var(--xz-primary-lighter);
}

.user-chip-name {
  font-size: 13px;
  color: var(--xz-text-primary);
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chip-arrow {
  color: var(--xz-text-placeholder);
  font-size: 12px;
}

.site-main {
  flex: 1;
}

/* ---------- Footer ---------- */
.site-footer {
  margin-top: 64px;
  background: #fbfcfc;
  border-top: 1px solid var(--xz-border-light);
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  gap: 48px;
  padding-top: 40px;
  padding-bottom: 32px;
}

.footer-brand {
  max-width: 420px;
}

.footer-desc {
  margin-top: 14px;
  font-size: 13px;
  line-height: 1.75;
  color: var(--xz-text-secondary);
}

.footer-links {
  display: flex;
  gap: 64px;
}

.link-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.link-group h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--xz-text-primary);
  margin-bottom: 2px;
}

.link-group a,
.link-group span {
  font-size: 13px;
  color: var(--xz-text-secondary);
  cursor: pointer;
  transition: color 0.2s ease;
}

.link-group a:hover {
  color: var(--xz-primary);
}

.footer-bottom {
  border-top: 1px solid var(--xz-border-light);
  padding: 16px 0;
  font-size: 12px;
  color: var(--xz-text-placeholder);
}

.footer-bottom .xz-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 860px) {
  .site-nav {
    display: none;
  }

  .footer-inner {
    flex-direction: column;
    gap: 28px;
  }

  .footer-links {
    gap: 40px;
  }
}
</style>
