<template>
  <el-container class="console-layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="console-aside">
      <div class="aside-head">
        <BrandLogo :compact="collapsed" @click="router.push('/')" />
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        class="aside-menu"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.label }}</template>
          <el-badge
            v-if="item.badge"
            :value="item.badge"
            :max="99"
            class="menu-badge"
          />
        </el-menu-item>
      </el-menu>

      <div v-if="!collapsed" class="aside-foot">
        <div class="verify-hint" :class="`is-${verifyTone}`">
          <el-icon><component :is="verifyIcon" /></el-icon>
          <div class="hint-body">
            <span class="hint-title">{{ verifyTitle }}</span>
            <span class="hint-desc">{{ verifyDesc }}</span>
          </div>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="console-header">
        <div class="header-left">
          <el-button text class="collapse-btn" @click="collapsed = !collapsed">
            <el-icon :size="18">
              <component :is="collapsed ? 'Expand' : 'Fold'" />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: roleHome }">{{ roleConsoleName }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-button text class="back-site" @click="router.push('/')">
            <el-icon><Monitor /></el-icon>
            <span>前台首页</span>
          </el-button>

          <el-dropdown @command="onCommand">
            <div class="user-chip">
              <el-avatar :size="30" :src="userStore.avatar">
                {{ userStore.displayName.slice(0, 1) }}
              </el-avatar>
              <div class="chip-text">
                <span class="chip-name">{{ userStore.displayName }}</span>
                <span class="chip-role">{{ roleText }}</span>
              </div>
              <el-icon class="chip-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人档案
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="console-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BrandLogo from '@/components/BrandLogo.vue'
import { useUserStore } from '@/stores/user'
import { ROLE_HOME, ROLE_TEXT } from '@/utils/dict'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)

const roleText = computed(() => ROLE_TEXT[userStore.role] || '用户')
const roleHome = computed(() => ROLE_HOME[userStore.role] || '/')

const roleConsoleName = computed(
  () =>
    ({
      STUDENT: '学生工作台',
      EXPERT: '专家工作台',
      COMPANY: '企业工作台',
      ADMIN: '平台运营'
    })[userStore.role] || '工作台'
)

/** 侧边菜单按角色配置 */
const menus = computed(() => {
  const map = {
    STUDENT: [
      { path: '/student', label: '工作台', icon: 'Odometer' },
      { path: '/student/projects', label: '我的项目', icon: 'FolderOpened' },
      { path: '/student/reviews', label: '收到的点评', icon: 'ChatDotSquare' },
      { path: '/student/invitations', label: '企业邀约', icon: 'Promotion' },
      { path: '/student/profile', label: '我的档案', icon: 'User' }
    ],
    EXPERT: [
      { path: '/expert', label: '点评工作台', icon: 'Odometer' },
      { path: '/expert/pool', label: '求点评池', icon: 'Collection' },
      { path: '/expert/history', label: '点评记录', icon: 'Document' },
      { path: '/expert/profile', label: '专家档案', icon: 'User' }
    ],
    COMPANY: [
      { path: '/company', label: '招聘工作台', icon: 'Odometer' },
      { path: '/company/talent', label: '人才检索', icon: 'Search' },
      { path: '/company/favorites', label: '收藏夹', icon: 'Star' },
      { path: '/company/invitations', label: '发出的邀约', icon: 'Promotion' },
      { path: '/company/logs', label: '查验记录', icon: 'Clock' },
      { path: '/company/profile', label: '企业档案', icon: 'OfficeBuilding' }
    ],
    ADMIN: [
      { path: '/admin', label: '平台总览', icon: 'DataAnalysis' },
      { path: '/admin/users', label: '用户管理', icon: 'UserFilled' },
      { path: '/admin/verifies', label: '认证审核', icon: 'CircleCheck' },
      { path: '/admin/audits', label: '内容审核', icon: 'View' },
      { path: '/admin/appeals', label: '申诉处理', icon: 'Warning' },
      { path: '/admin/projects', label: '项目管理', icon: 'FolderOpened' }
    ]
  }
  return map[userStore.role] || []
})

const activeMenu = computed(() => {
  // 详情页/编辑页高亮其列表页
  const path = route.path
  if (path.startsWith('/student/projects/edit')) return '/student/projects'
  if (path.startsWith('/expert/review')) return '/expert/pool'
  if (path.startsWith('/company/candidate')) return '/company/talent'
  return path
})

/** 认证状态提示条 */
const verifyTone = computed(() => {
  if (userStore.isStudent) {
    return { 0: 'none', 1: 'pending', 2: 'ok', 3: 'reject' }[userStore.eduVerified] || 'none'
  }
  if (userStore.isExpert) {
    return { 0: 'none', 1: 'pending', 2: 'ok', 3: 'reject' }[userStore.expertVerifyStatus] || 'none'
  }
  if (userStore.isCompany) {
    return { 0: 'none', 1: 'pending', 2: 'ok', 3: 'reject' }[userStore.companyVerifyStatus] || 'none'
  }
  return 'ok'
})

const verifyIcon = computed(
  () =>
    ({
      none: 'InfoFilled',
      pending: 'Clock',
      ok: 'CircleCheckFilled',
      reject: 'CircleCloseFilled'
    })[verifyTone.value] || 'InfoFilled'
)

const verifyTitle = computed(() => {
  if (userStore.isStudent) return '学籍认证'
  if (userStore.isExpert) return '专家认证'
  if (userStore.isCompany) return '企业认证'
  return '运营账号'
})

const verifyDesc = computed(
  () =>
    ({
      none: '未提交，提交后可信度更高',
      pending: '审核中，请耐心等待',
      ok: '已通过认证',
      reject: '已驳回，请查看意见'
    })[verifyTone.value] || ''
)

const onCommand = async (command) => {
  if (command === 'logout') {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    const profilePath = {
      STUDENT: '/student/profile',
      EXPERT: '/expert/profile',
      COMPANY: '/company/profile',
      ADMIN: '/admin'
    }[userStore.role]
    if (profilePath) router.push(profilePath)
  }
}
</script>

<style scoped>
.console-layout {
  height: 100vh;
}

.console-aside {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-right: 1px solid var(--xz-border-light);
  transition: width 0.22s ease;
  overflow: hidden;
}

.aside-head {
  height: var(--xz-header-height);
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--xz-border-light);
  flex-shrink: 0;
}

.aside-menu {
  flex: 1;
  border-right: none;
  padding: 10px 8px;
  overflow-y: auto;
}

.aside-menu:not(.el-menu--collapse) {
  width: 100%;
}

.aside-menu :deep(.el-menu-item) {
  height: 42px;
  line-height: 42px;
  border-radius: 8px;
  margin-bottom: 4px;
  font-size: 14px;
}

.aside-menu :deep(.el-menu-item.is-active) {
  background: var(--xz-primary-lighter);
  color: var(--xz-primary);
  font-weight: 500;
}

.aside-menu :deep(.el-menu-item:hover) {
  background: var(--xz-bg-hover);
}

.menu-badge {
  margin-left: 8px;
}

.aside-foot {
  padding: 12px;
  flex-shrink: 0;
}

.verify-hint {
  display: flex;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.45;
}

.verify-hint .el-icon {
  margin-top: 2px;
  flex-shrink: 0;
}

.hint-body {
  display: flex;
  flex-direction: column;
}

.hint-title {
  font-weight: 600;
}

.hint-desc {
  opacity: 0.85;
  font-size: 11px;
}

.is-none {
  background: #eef2f4;
  color: var(--xz-info);
}

.is-pending {
  background: #fdf6e6;
  color: var(--xz-warning);
}

.is-ok {
  background: #eaf5ef;
  color: var(--xz-success);
}

.is-reject {
  background: #fbecec;
  color: var(--xz-danger);
}

.console-header {
  height: var(--xz-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid var(--xz-border-light);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.collapse-btn {
  padding: 6px;
  color: var(--xz-text-secondary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-site {
  color: var(--xz-text-secondary);
  font-size: 13px;
  gap: 4px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 9px;
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

.chip-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.chip-name {
  font-size: 13px;
  color: var(--xz-text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chip-role {
  font-size: 11px;
  color: var(--xz-text-placeholder);
}

.chip-arrow {
  color: var(--xz-text-placeholder);
  font-size: 12px;
}

.console-main {
  background: var(--xz-bg-page);
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
