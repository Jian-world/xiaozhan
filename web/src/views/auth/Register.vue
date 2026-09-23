<template>
  <div class="auth-page">
    <div class="auth-visual">
      <div class="visual-inner">
        <BrandLogo />
        <h1 class="visual-title">
          先建立一个
          <br />
          属于你的作品档案。
        </h1>
        <p class="visual-desc">
          注册后你可以：上传项目源码与演示视频、申请学籍认证、把作品投进求点评池，
          收到来自高校导师与企业一线工程师的逐项点评。
        </p>

        <div class="role-cards">
          <div
            v-for="item in roles"
            :key="item.value"
            class="role-card"
            :class="{ 'is-active': form.role === item.value }"
            @click="form.role = item.value"
          >
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
            <div class="role-card-body">
              <strong>{{ item.label }}</strong>
              <span>{{ item.desc }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="visual-blob blob-a"></div>
      <div class="visual-blob blob-b"></div>
    </div>

    <div class="auth-form-wrap">
      <div class="auth-form">
        <h2 class="form-title">创建账号</h2>
        <p class="form-subtitle">{{ currentRoleHint }}</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent
        >
          <div class="form-grid">
            <el-form-item label="登录账号" prop="username" class="span-2">
              <el-input v-model="form.username" placeholder="4-20 位字母、数字或下划线" clearable />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="至少 6 位" show-password />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入"
                show-password
              />
            </el-form-item>

            <el-form-item :label="nameLabel" prop="nickname">
              <el-input v-model="form.nickname" :placeholder="namePlaceholder" clearable />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="选填，用于接收邀约提醒" clearable />
            </el-form-item>

            <!-- 学生专属 -->
            <template v-if="form.role === 'STUDENT'">
              <el-form-item label="学校" prop="school">
                <el-input v-model="form.school" placeholder="如：南京理工大学" clearable />
              </el-form-item>

              <el-form-item label="专业" prop="major">
                <el-input v-model="form.major" placeholder="如：软件工程" clearable />
              </el-form-item>

              <el-form-item label="专业大类" prop="majorCategory">
                <el-select v-model="form.majorCategory" placeholder="请选择" class="full-width">
                  <el-option
                    v-for="c in CATEGORIES"
                    :key="c.value"
                    :label="c.label"
                    :value="c.value"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="学历" prop="degree">
                <el-select v-model="form.degree" placeholder="请选择" class="full-width">
                  <el-option v-for="d in degrees" :key="d" :label="d" :value="d" />
                </el-select>
              </el-form-item>

              <el-form-item label="毕业年份" prop="graduateYear" class="span-2">
                <el-select v-model="form.graduateYear" placeholder="请选择" class="full-width">
                  <el-option
                    v-for="y in graduateYears"
                    :key="y"
                    :label="`${y} 届`"
                    :value="y"
                  />
                </el-select>
              </el-form-item>
            </template>

            <!-- 专家专属 -->
            <template v-if="form.role === 'EXPERT'">
              <el-form-item label="所在单位" prop="orgName">
                <el-input v-model="form.orgName" placeholder="高校或企业名称" clearable />
              </el-form-item>

              <el-form-item label="职位" prop="position">
                <el-input v-model="form.position" placeholder="如：高级后端工程师" clearable />
              </el-form-item>
            </template>

            <!-- 企业专属 -->
            <template v-if="form.role === 'COMPANY'">
              <el-form-item label="企业名称" prop="companyName">
                <el-input v-model="form.companyName" placeholder="请输入营业执照上的企业全称" clearable />
              </el-form-item>

              <el-form-item label="所属行业" prop="industry">
                <el-input v-model="form.industry" placeholder="如：人工智能 / 企业服务" clearable />
              </el-form-item>
            </template>
          </div>

          <el-form-item prop="agree" class="agree-item">
            <el-checkbox v-model="form.agree">
              我已阅读并同意《用户协议》与《隐私政策》，承诺上传的项目材料真实有效
            </el-checkbox>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="onSubmit"
          >
            注 册
          </el-button>
        </el-form>

        <div class="form-foot">
          <span>已有账号？</span>
          <router-link to="/login" class="link">直接登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BrandLogo from '@/components/BrandLogo.vue'
import { useUserStore } from '@/stores/user'
import { CATEGORIES, ROLE_HOME } from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const roles = [
  { value: 'STUDENT', label: '我是学生', icon: 'Reading', desc: '沉淀作品集，获得专业点评' },
  { value: 'EXPERT', label: '我是导师/工程师', icon: 'Medal', desc: '点评项目，为真实能力背书' },
  { value: 'COMPANY', label: '我是招聘方', icon: 'OfficeBuilding', desc: '查验作品，精准评估候选人' }
]

const degrees = ['专科', '本科', '硕士', '博士']
const graduateYears = Array.from({ length: 7 }, (_, i) => new Date().getFullYear() + i - 2)

const form = reactive({
  role: 'STUDENT',
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  school: '',
  major: '',
  majorCategory: 'CS',
  degree: '',
  graduateYear: null,
  orgName: '',
  position: '',
  companyName: '',
  industry: '',
  agree: false
})

const currentRoleHint = computed(
  () =>
    ({
      STUDENT: '填写学校和专业，之后可上传项目并申请学籍认证',
      EXPERT: '填写单位与职位，提交认证后即可开始点评项目',
      COMPANY: '填写企业信息，认证通过后可检索候选人作品集'
    })[form.role]
)

const nameLabel = computed(() => (form.role === 'COMPANY' ? '联系人姓名' : '你的称呼'))
const namePlaceholder = computed(() =>
  form.role === 'COMPANY' ? '如：王女士' : '如：李思远（会展示在作品集上）'
)

const rules = computed(() => ({
  username: [
    { required: true, message: '请填写登录账号', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度 4-20 位', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '账号只能包含字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请填写密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) =>
        value === form.password ? callback() : callback(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ],
  nickname: [{ required: true, message: '请填写称呼', trigger: 'blur' }],
  phone: [
    {
      pattern: /^$|^1[3-9]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur'
    }
  ],
  school: form.role === 'STUDENT' ? [{ required: true, message: '请填写学校', trigger: 'blur' }] : [],
  major: form.role === 'STUDENT' ? [{ required: true, message: '请填写专业', trigger: 'blur' }] : [],
  orgName: form.role === 'EXPERT' ? [{ required: true, message: '请填写所在单位', trigger: 'blur' }] : [],
  position: form.role === 'EXPERT' ? [{ required: true, message: '请填写职位', trigger: 'blur' }] : [],
  companyName:
    form.role === 'COMPANY' ? [{ required: true, message: '请填写企业名称', trigger: 'blur' }] : [],
  industry: form.role === 'COMPANY' ? [{ required: true, message: '请填写所属行业', trigger: 'blur' }] : [],
  agree: [
    {
      validator: (rule, value, callback) =>
        value ? callback() : callback(new Error('请阅读并同意用户协议')),
      trigger: 'change'
    }
  ]
}))

watch(
  () => form.role,
  () => {
    formRef.value?.clearValidate()
  }
)

const onSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const payload = {
      role: form.role,
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone
    }
    if (form.role === 'STUDENT') {
      Object.assign(payload, {
        school: form.school,
        major: form.major,
        majorCategory: form.majorCategory,
        degree: form.degree,
        graduateYear: form.graduateYear
      })
    } else if (form.role === 'EXPERT') {
      Object.assign(payload, { orgName: form.orgName, position: form.position })
    } else if (form.role === 'COMPANY') {
      Object.assign(payload, { companyName: form.companyName, industry: form.industry })
      payload.realName = form.nickname
    }

    const data = await userStore.register(payload)
    ElMessage.success('注册成功，欢迎加入校栈')
    router.push(ROLE_HOME[data.user.role] || '/')
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
  flex: 1;
  display: flex;
  align-items: center;
  padding: 56px 56px;
  background: linear-gradient(150deg, #f4faf9 0%, #eaf3f2 55%, #f9f2ea 100%);
  overflow: hidden;
}

.visual-inner {
  position: relative;
  z-index: 2;
  max-width: 430px;
}

.visual-title {
  margin-top: 40px;
  font-size: 30px;
  line-height: 1.4;
  font-weight: 700;
  color: var(--xz-text-primary);
}

.visual-desc {
  margin-top: 16px;
  font-size: 14px;
  line-height: 1.85;
  color: var(--xz-text-secondary);
}

.role-cards {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.role-card {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 13px 16px;
  border-radius: var(--xz-radius);
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.2s ease;
}

.role-card .el-icon {
  color: var(--xz-text-secondary);
  flex-shrink: 0;
}

.role-card-body {
  display: flex;
  flex-direction: column;
}

.role-card-body strong {
  font-size: 14px;
  color: var(--xz-text-primary);
}

.role-card-body span {
  font-size: 12px;
  color: var(--xz-text-secondary);
  margin-top: 1px;
}

.role-card:hover {
  background: #ffffff;
  border-color: #cfe3e2;
}

.role-card.is-active {
  background: #ffffff;
  border-color: var(--xz-primary);
  box-shadow: 0 4px 14px rgba(31, 111, 107, 0.12);
}

.role-card.is-active .el-icon,
.role-card.is-active strong {
  color: var(--xz-primary);
}

.visual-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
}

.blob-a {
  width: 300px;
  height: 300px;
  background: #bfe3e0;
  right: -90px;
  top: -70px;
}

.blob-b {
  width: 260px;
  height: 260px;
  background: #f6d6ba;
  left: -80px;
  bottom: -60px;
}

.auth-form-wrap {
  flex: 1.15;
  display: flex;
  justify-content: center;
  padding: 48px 48px;
  overflow-y: auto;
}

.auth-form {
  width: 100%;
  max-width: 520px;
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
  margin-bottom: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.span-2 {
  grid-column: span 2;
}

.full-width {
  width: 100%;
}

.agree-item {
  margin-top: 6px;
}

.agree-item :deep(.el-checkbox) {
  height: auto;
  align-items: flex-start;
  white-space: normal;
}

.agree-item :deep(.el-checkbox__label) {
  font-size: 12.5px;
  line-height: 1.6;
  color: var(--xz-text-secondary);
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
  margin-top: 8px;
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

@media (max-width: 1024px) {
  .auth-visual {
    display: none;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: span 1;
  }
}
</style>
