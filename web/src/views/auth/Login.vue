<template>
  <div class="auth-page">
    <div class="auth-visual">
      <div class="visual-inner">
        <BrandLogo />
        <h1 class="visual-title">
          简历上说不清的，
          <br />
          用项目说清楚。
        </h1>
        <p class="visual-desc">
          把课程设计、竞赛作品、实习产出整理成一份可点开、可下载、可被点评的在线作品集。
          导师与企业工程师会逐项告诉你：哪里做得好，哪里还差一口气。
        </p>
        <ul class="visual-points">
          <li>
            <el-icon><FolderOpened /></el-icon>
            <div>
              <strong>源码 / 文档 / 演示视频</strong>
              <span>一次上传，形成结构化作品集</span>
            </div>
          </li>
          <li>
            <el-icon><ChatDotSquare /></el-icon>
            <div>
              <strong>四维专业点评</strong>
              <span>完成度、规范性、创新性、专业质量</span>
            </div>
          </li>
          <li>
            <el-icon><OfficeBuilding /></el-icon>
            <div>
              <strong>企业直接查验</strong>
              <span>用真实作品替代单薄的简历筛选</span>
            </div>
          </li>
        </ul>
      </div>
      <div class="visual-blob blob-a"></div>
      <div class="visual-blob blob-b"></div>
    </div>

    <div class="auth-form-wrap">
      <div class="auth-form">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-subtitle">登录后继续管理你的项目与点评</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
          <el-form-item label="登录身份" prop="role">
            <el-radio-group v-model="form.role" class="role-switch">
              <el-radio-button v-for="item in roles" :key="item.value" :value="item.value">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="账号" prop="username">
            <el-input
              v-model="form.username"
              size="large"
              placeholder="请输入登录账号"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="onSubmit"
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="onSubmit"
          >
            登 录
          </el-button>
        </el-form>

        <div class="form-foot">
          <span>还没有账号？</span>
          <router-link to="/register" class="link">立即注册</router-link>
        </div>

        <el-divider>演示账号（密码均为 123456）</el-divider>
        <div class="demo-accounts">
          <div
            v-for="acc in demoAccounts"
            :key="acc.username"
            class="demo-account"
            @click="fillDemo(acc)"
          >
            <el-tag size="small" effect="plain">{{ acc.label }}</el-tag>
            <span class="demo-name">{{ acc.username }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import BrandLogo from '@/components/BrandLogo.vue'
import { useUserStore } from '@/stores/user'
import { ROLE_HOME } from '@/utils/dict'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const roles = [
  { value: 'STUDENT', label: '学生', icon: 'Reading' },
  { value: 'EXPERT', label: '专家', icon: 'Medal' },
  { value: 'COMPANY', label: '企业', icon: 'OfficeBuilding' },
  { value: 'ADMIN', label: '运营', icon: 'Setting' }
]

const demoAccounts = [
  { label: '学生', username: 'student01', role: 'STUDENT' },
  { label: '专家', username: 'expert01', role: 'EXPERT' },
  { label: '企业', username: 'company01', role: 'COMPANY' },
  { label: '运营', username: 'admin', role: 'ADMIN' }
]

const form = reactive({
  role: 'STUDENT',
  username: '',
  password: ''
})

const rules = {
  role: [{ required: true, message: '请选择登录身份', trigger: 'change' }],
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

const fillDemo = (acc) => {
  form.role = acc.role
  form.username = acc.username
  form.password = '123456'
}

const onSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await userStore.login({ ...form })
    ElMessage.success(`欢迎回来，${data.user.nickname || data.user.username}`)
    const redirect = route.query.redirect
    router.push(redirect || ROLE_HOME[data.user.role] || '/')
  } catch (e) {
    /* 错误已由拦截器提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
  background: #ffffff;
}

.auth-visual {
  position: relative;
  flex: 1.05;
  display: flex;
  align-items: center;
  padding: 56px 64px;
  background: linear-gradient(150deg, #f4faf9 0%, #eaf3f2 55%, #f9f2ea 100%);
  overflow: hidden;
}

.visual-inner {
  position: relative;
  z-index: 2;
  max-width: 460px;
}

.visual-title {
  margin-top: 40px;
  font-size: 32px;
  line-height: 1.4;
  font-weight: 700;
  color: var(--xz-text-primary);
  letter-spacing: 0.5px;
}

.visual-desc {
  margin-top: 16px;
  font-size: 14px;
  line-height: 1.85;
  color: var(--xz-text-secondary);
}

.visual-points {
  margin-top: 34px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.visual-points li {
  display: flex;
  gap: 13px;
  align-items: flex-start;
}

.visual-points .el-icon {
  font-size: 20px;
  color: var(--xz-primary);
  margin-top: 3px;
  flex-shrink: 0;
}

.visual-points div {
  display: flex;
  flex-direction: column;
}

.visual-points strong {
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.visual-points span {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  margin-top: 2px;
}

.visual-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
}

.blob-a {
  width: 320px;
  height: 320px;
  background: #bfe3e0;
  right: -80px;
  top: -60px;
}

.blob-b {
  width: 280px;
  height: 280px;
  background: #f6d6ba;
  left: -70px;
  bottom: -70px;
}

.auth-form-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
}

.auth-form {
  width: 100%;
  max-width: 380px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--xz-text-primary);
}

.form-subtitle {
  margin-top: 8px;
  font-size: 13.5px;
  color: var(--xz-text-secondary);
  margin-bottom: 28px;
}

.role-switch {
  width: 100%;
  display: flex;
}

.role-switch :deep(.el-radio-button) {
  flex: 1;
}

.role-switch :deep(.el-radio-button__inner) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: 13px;
}

.submit-btn {
  width: 100%;
  margin-top: 4px;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
}

.form-foot {
  margin-top: 18px;
  text-align: center;
  font-size: 13px;
  color: var(--xz-text-secondary);
}

.link {
  color: var(--xz-primary);
  font-weight: 500;
  margin-left: 4px;
}

.link:hover {
  text-decoration: underline;
}

.demo-accounts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.demo-account {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 7px 10px;
  border: 1px solid var(--xz-border);
  border-radius: var(--xz-radius-sm);
  cursor: pointer;
  transition: all 0.18s ease;
  background: #fbfcfc;
}

.demo-account:hover {
  border-color: var(--xz-primary);
  background: var(--xz-primary-lighter);
}

.demo-name {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  font-family: 'JetBrains Mono', Consolas, monospace;
}

@media (max-width: 960px) {
  .auth-visual {
    display: none;
  }
}
</style>
