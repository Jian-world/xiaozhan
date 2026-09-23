<template>
  <div v-loading="loading" class="expert-profile">
    <div class="page-head">
      <div>
        <h2 class="page-title">专家档案</h2>
        <p class="page-sub">
          完善档案与通过认证后，你的点评会展示单位与职位信息（可关闭），学生也能在专家广场找到你。
        </p>
      </div>
      <el-button v-if="verifyStatus === 2" plain @click="router.push('/experts')">
        <el-icon><View /></el-icon>
        在专家广场查看我的展示
      </el-button>
    </div>

    <div class="profile-body">
      <div class="profile-main">
        <!-- 基础资料 -->
        <section class="card">
          <h3 class="card-title">基础资料</h3>

          <div class="avatar-row">
            <el-avatar :size="72" :src="base.avatar" class="avatar">
              {{ (userStore.displayName || '师').slice(0, 1) }}
            </el-avatar>
            <div class="avatar-ops">
              <el-upload
                :show-file-list="false"
                accept=".jpg,.jpeg,.png,.gif,.webp,.bmp,.svg"
                :before-upload="beforeAvatar"
                :http-request="doAvatarUpload"
              >
                <el-button size="small" :loading="avatarUploading">
                  <el-icon><Upload /></el-icon>
                  更换头像
                </el-button>
              </el-upload>
              <span class="avatar-hint">建议使用正装照，企业工程师可上传工牌照</span>
            </div>
          </div>

          <el-form :model="base" label-position="top" class="base-form">
            <div class="form-row">
              <el-form-item label="昵称">
                <el-input v-model="base.nickname" placeholder="用于公开展示的名称" maxlength="30" />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="base.realName" placeholder="仅在点评署名时展示" maxlength="20" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="手机号">
                <el-input v-model="base.phone" placeholder="仅平台运营可见" maxlength="20" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="base.email" placeholder="用于接收站内通知" maxlength="60" />
              </el-form-item>
            </div>
            <el-button type="primary" :loading="savingBase" @click="saveBase">保存基础资料</el-button>
          </el-form>
        </section>

        <!-- 专业信息 -->
        <section class="card">
          <h3 class="card-title">专业信息</h3>

          <el-form :model="expert" label-position="top">
            <div class="form-row">
              <el-form-item label="所在单位">
                <el-input v-model="expert.orgName" placeholder="如：华中科技大学 / 字节跳动" maxlength="60" />
              </el-form-item>
              <el-form-item label="职位 / 职称">
                <el-input v-model="expert.position" placeholder="如：副教授 / 高级后端工程师" maxlength="40" />
              </el-form-item>
            </div>

            <el-form-item label="擅长领域">
              <div class="tag-editor">
                <el-tag
                  v-for="tag in domainTags"
                  :key="tag"
                  closable
                  class="domain-chip"
                  @close="removeDomain(tag)"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="domainInputVisible"
                  ref="domainInputRef"
                  v-model="domainInput"
                  size="small"
                  class="domain-input"
                  placeholder="输入后回车"
                  @keyup.enter="confirmDomain"
                  @blur="confirmDomain"
                />
                <el-button v-else size="small" class="domain-add" @click="showDomainInput">
                  <el-icon><Plus /></el-icon>
                  添加领域
                </el-button>
              </div>
              <div class="field-hint">
                如：Java 后端、分布式系统、数据结构与算法、交互设计。学生邀请定向点评时会按领域匹配。
              </div>
            </el-form-item>

            <el-form-item label="单位信息公开">
              <div class="switch-row">
                <el-switch
                  v-model="showOrg"
                  active-text="公开单位与职位"
                  inactive-text="仅显示认证身份"
                  inline-prompt
                />
                <span class="switch-hint">
                  关闭后，你的点评将只显示「认证点评人」与等级，不展示单位信息。
                </span>
              </div>
            </el-form-item>

            <el-button type="primary" :loading="savingExpert" @click="saveExpert">保存专业信息</el-button>
          </el-form>
        </section>

        <!-- 专家认证 -->
        <section class="card">
          <div class="card-head">
            <h3 class="card-title no-margin">专家认证</h3>
            <el-tag :type="VERIFY_STATUS[verifyStatus]?.type" effect="plain">
              {{ VERIFY_STATUS[verifyStatus]?.text }}
            </el-tag>
          </div>

          <el-alert
            v-if="verifyStatus === 3"
            type="error"
            :closable="false"
            show-icon
            class="verify-alert"
          >
            <template #title>认证被驳回</template>
            <template #default>{{ expert.verifyRemark || '请查看审核意见，补充材料后重新提交。' }}</template>
          </el-alert>

          <el-alert
            v-else-if="verifyStatus === 1"
            type="warning"
            :closable="false"
            show-icon
            class="verify-alert"
          >
            <template #title>材料已提交，平台正在审核</template>
            <template #default>通常在 1-2 个工作日内完成，审核通过后即可开始点评。</template>
          </el-alert>

          <template v-if="verifyStatus === 0 || verifyStatus === 3">
            <p class="verify-desc">
              认证通过后才能领取求点评池的项目并提交点评。请上传能证明身份与专业资质的材料
              （如教师工作证、在职证明、职称证书、企业工牌等）。
            </p>

            <el-form :model="verifyForm" label-position="top" class="verify-form">
              <div class="form-row">
                <el-form-item label="所在单位" required>
                  <el-input v-model="verifyForm.orgName" placeholder="认证主体单位全称" maxlength="60" />
                </el-form-item>
                <el-form-item label="职位 / 职称" required>
                  <el-input v-model="verifyForm.position" placeholder="如：副教授、技术专家" maxlength="40" />
                </el-form-item>
              </div>

              <el-form-item label="专家类型" required>
                <el-radio-group v-model="verifyForm.expertType">
                  <el-radio-button value="TEACHER">高校导师</el-radio-button>
                  <el-radio-button value="ENGINEER">企业工程师</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="承担课程 / 负责方向">
                <el-input
                  v-model="verifyForm.domain"
                  placeholder="如：软件工程、数据库系统原理 / 交易系统、中间件"
                  maxlength="120"
                />
                <div class="field-hint">用逗号分隔，会同步到你的擅长领域标签。</div>
              </el-form-item>

              <el-form-item label="资质证明材料" required>
                <el-upload
                  :show-file-list="false"
                  accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                  :before-upload="beforeVerifyFile"
                  :http-request="doVerifyUpload"
                >
                  <el-button :loading="verifyUploading">
                    <el-icon><Upload /></el-icon>
                    上传证明材料
                  </el-button>
                </el-upload>
                <div v-if="verifyForm.verifyFile" class="uploaded-file">
                  <el-icon><Document /></el-icon>
                  <a :href="verifyForm.verifyFile" target="_blank" rel="noopener">已上传，点击查看</a>
                  <el-button size="small" text type="danger" @click="verifyForm.verifyFile = ''">
                    移除
                  </el-button>
                </div>
                <div v-else class="field-hint">支持 PDF / Word / 图片，不超过 10MB</div>
              </el-form-item>

              <el-button type="primary" :loading="submittingVerify" @click="submitVerify">
                提交认证申请
              </el-button>
            </el-form>
          </template>
        </section>

        <!-- 认证记录 -->
        <section class="card">
          <h3 class="card-title">认证记录</h3>
          <el-table v-if="records.length" :data="records" class="record-table">
            <el-table-column label="提交时间" width="160">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="VERIFY_STATUS[row.status]?.type" effect="plain">
                  {{ VERIFY_STATUS[row.status]?.text }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核意见" min-width="180">
              <template #default="{ row }">
                <span :class="{ 'is-empty': !row.remark }">{{ row.remark || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="处理时间" width="160">
              <template #default="{ row }">{{ formatDateTime(row.handleTime) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无认证记录" :image-size="70" />
        </section>
      </div>

      <!-- 侧栏 -->
      <aside class="profile-side">
        <div class="side-card stat-side">
          <h4 class="side-title">点评业绩</h4>
          <div class="stat-row">
            <span class="stat-num">{{ stats.totalReviews || 0 }}</span>
            <span class="stat-txt">累计点评</span>
          </div>
          <div class="stat-row">
            <span class="stat-num">{{ stats.thanksCount || 0 }}</span>
            <span class="stat-txt">收到致谢</span>
          </div>
          <div class="stat-row">
            <span class="stat-num">
              {{ stats.avgGivenScore > 0 ? Number(stats.avgGivenScore).toFixed(1) : '—' }}
            </span>
            <span class="stat-txt">给出均分</span>
          </div>
          <div class="stat-row">
            <span class="stat-num">{{ stats.points || 0 }}</span>
            <span class="stat-txt">累计积分</span>
          </div>
          <div class="level-badge">
            <el-tag :type="EXPERT_LEVEL_TYPE[stats.level]" effect="dark">
              {{ EXPERT_LEVEL_TEXT[stats.level] || '青铜点评人' }}
            </el-tag>
          </div>
        </div>

        <div class="side-card">
          <h4 class="side-title">每日额度</h4>
          <div class="quota-line">
            <span class="quota-num">{{ stats.todayReviews || 0 }} / {{ stats.dailyQuota || 0 }}</span>
            <span class="quota-txt">今日已用</span>
          </div>
          <el-progress
            :percentage="quotaPercent"
            :stroke-width="7"
            :show-text="false"
            :color="quotaColor"
          />
          <p class="quota-note">
            额度每日 0 点重置。等级提升后单日额度会相应增加。
          </p>
        </div>

        <div class="side-card">
          <h4 class="side-title">成为点评人意味着什么</h4>
          <ul class="meaning-list">
            <li>
              <strong>你的判断会被引用</strong>
              <span>学生的作品集与企业的检索结果都会展示你的点评与评分。</span>
            </li>
            <li>
              <strong>保持客观与克制</strong>
              <span>学生可以对点评发起申诉，申诉成立的点评会被隐藏并移出计分。</span>
            </li>
            <li>
              <strong>持续输出会有回报</strong>
              <span>累计点评数、致谢数与质量分共同决定你的等级与求点评池推荐权重。</span>
            </li>
          </ul>
        </div>

        <div class="side-card">
          <h4 class="side-title">账号安全</h4>
          <el-form :model="pwd" label-position="top">
            <el-form-item label="原密码">
              <el-input v-model="pwd.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="6-32 位" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwd.confirmPassword" type="password" show-password placeholder="再次输入" />
            </el-form-item>
          </el-form>
          <el-button :loading="savingPwd" class="pwd-btn" @click="savePassword">修改密码</el-button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi, fileApi, profileApi, reviewApi, verifyApi } from '@/api'
import {
  EXPERT_LEVEL_TEXT,
  EXPERT_LEVEL_TYPE,
  VERIFY_STATUS,
  formatDateTime
} from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const savingBase = ref(false)
const savingExpert = ref(false)
const savingPwd = ref(false)
const submittingVerify = ref(false)
const avatarUploading = ref(false)
const verifyUploading = ref(false)

const stats = ref({
  totalReviews: 0,
  todayReviews: 0,
  dailyQuota: 0,
  avgGivenScore: 0,
  thanksCount: 0,
  level: 'BRONZE',
  points: 0
})

const records = ref([])

const base = reactive({
  nickname: '',
  realName: '',
  avatar: '',
  phone: '',
  email: ''
})

const expert = reactive({
  orgName: '',
  position: '',
  domain: '',
  showOrg: 1,
  verifyRemark: ''
})

const verifyForm = reactive({
  orgName: '',
  position: '',
  expertType: 'TEACHER',
  domain: '',
  verifyFile: ''
})

const pwd = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const verifyStatus = computed(() => userStore.expertVerifyStatus)

const showOrg = computed({
  get: () => expert.showOrg === 1,
  set: (v) => {
    expert.showOrg = v ? 1 : 0
  }
})

const domainTags = computed(() =>
  (expert.domain || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
)

const quotaPercent = computed(() => {
  if (!stats.value.dailyQuota) return 0
  return Math.min(100, Math.round((stats.value.todayReviews / stats.value.dailyQuota) * 100))
})

const quotaColor = computed(() =>
  quotaPercent.value >= 100 ? '#cc4b4b' : quotaPercent.value >= 80 ? '#d99a2b' : '#1f6f6b'
)

/* ---------- 领域标签 ---------- */
const domainInputVisible = ref(false)
const domainInput = ref('')
const domainInputRef = ref()

const showDomainInput = () => {
  domainInputVisible.value = true
  setTimeout(() => domainInputRef.value?.focus(), 30)
}

const confirmDomain = () => {
  const value = domainInput.value.trim()
  if (value && !domainTags.value.includes(value)) {
    expert.domain = [...domainTags.value, value].join(',')
  }
  domainInput.value = ''
  domainInputVisible.value = false
}

const removeDomain = (tag) => {
  expert.domain = domainTags.value.filter((t) => t !== tag).join(',')
}

/* ---------- 上传 ---------- */
const beforeAvatar = (file) => {
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('头像不超过 5MB')
    return false
  }
  return true
}

const doAvatarUpload = async (options) => {
  avatarUploading.value = true
  try {
    const res = await fileApi.upload(options.file, 'AVATAR')
    base.avatar = res.url
    await profileApi.updateBase({ ...base })
    await userStore.fetchMe()
    ElMessage.success('头像已更新')
  } catch (e) {
    /* 已提示 */
  } finally {
    avatarUploading.value = false
  }
}

const beforeVerifyFile = (file) => {
  if (file.size / 1024 / 1024 > 10) {
    ElMessage.error('证明材料不超过 10MB')
    return false
  }
  return true
}

const doVerifyUpload = async (options) => {
  verifyUploading.value = true
  try {
    const res = await fileApi.upload(options.file, 'VERIFY')
    verifyForm.verifyFile = res.url
    ElMessage.success('材料上传成功')
  } catch (e) {
    /* 已提示 */
  } finally {
    verifyUploading.value = false
  }
}

/* ---------- 保存 ---------- */
const saveBase = async () => {
  savingBase.value = true
  try {
    await profileApi.updateBase({ ...base })
    await userStore.fetchMe()
    ElMessage.success('基础资料已保存')
  } catch (e) {
    /* 已提示 */
  } finally {
    savingBase.value = false
  }
}

const saveExpert = async () => {
  if (!expert.orgName?.trim() || !expert.position?.trim()) {
    ElMessage.warning('请先填写所在单位与职位')
    return
  }
  savingExpert.value = true
  try {
    await profileApi.updateExpert({
      orgName: expert.orgName,
      position: expert.position,
      domainTags: domainTags.value,
      showOrg: expert.showOrg
    })
    await userStore.fetchMe()
    ElMessage.success('专业信息已保存')
  } catch (e) {
    /* 已提示 */
  } finally {
    savingExpert.value = false
  }
}

const savePassword = async () => {
  if (!pwd.oldPassword || !pwd.newPassword) {
    ElMessage.warning('请填写原密码与新密码')
    return
  }
  if (pwd.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (pwd.newPassword !== pwd.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  savingPwd.value = true
  try {
    await authApi.changePassword({
      oldPassword: pwd.oldPassword,
      newPassword: pwd.newPassword
    })
    ElMessage.success('密码已修改，请重新登录')
    await userStore.logout()
    router.push('/login')
  } catch (e) {
    /* 已提示 */
  } finally {
    savingPwd.value = false
  }
}

const submitVerify = async () => {
  if (!verifyForm.orgName?.trim() || !verifyForm.position?.trim()) {
    ElMessage.warning('请填写所在单位与职位')
    return
  }
  if (!verifyForm.expertType) {
    ElMessage.warning('请选择专家类型')
    return
  }
  if (!verifyForm.verifyFile) {
    ElMessage.warning('请上传资质证明材料')
    return
  }
  submittingVerify.value = true
  try {
    await verifyApi.submitExpert({ ...verifyForm })
    await userStore.fetchMe()
    ElMessage.success('认证申请已提交，请等待平台审核')
    await loadRecords()
  } catch (e) {
    /* 已提示 */
  } finally {
    submittingVerify.value = false
  }
}

/* ---------- 初始化 ---------- */
const loadRecords = async () => {
  try {
    records.value = (await verifyApi.myRecords()) || []
  } catch (e) {
    records.value = []
  }
}

const load = async () => {
  loading.value = true
  try {
    const me = userStore.isLogin ? await userStore.fetchMe().catch(() => null) : null
    const user = me || userStore.user
    if (user) {
      base.nickname = user.nickname || ''
      base.realName = user.realName || ''
      base.avatar = user.avatar || ''
      base.phone = user.phone || ''
      base.email = user.email || ''

      const ep = user.expertProfile || {}
      expert.orgName = ep.orgName || ''
      expert.position = ep.position || ''
      expert.domain = (ep.domainTags || []).join(',')
      expert.showOrg = ep.showOrg === 0 ? 0 : 1
      expert.verifyRemark = ep.verifyRemark || ''

      verifyForm.orgName = ep.orgName || ''
      verifyForm.position = ep.position || ''
      verifyForm.expertType = ep.expertType || 'TEACHER'
      verifyForm.domain = (ep.domainTags || []).join(',')
    }

    const s = await reviewApi.statistics().catch(() => null)
    if (s) stats.value = { ...stats.value, ...s }
    await loadRecords()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--xz-text-primary);
}

.page-sub {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--xz-text-secondary);
  max-width: 660px;
}

.profile-body {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

.card {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 22px 26px;
  margin-bottom: 18px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--xz-text-primary);
  padding-left: 10px;
  border-left: 3px solid var(--xz-primary);
  line-height: 1.3;
  margin-bottom: 18px;
}

.card-title.no-margin {
  margin-bottom: 0;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 18px;
}

.card-head .card-title {
  margin-bottom: 0;
}

/* ---------- 基础资料 ---------- */
.avatar-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--xz-border-light);
}

.avatar {
  background: var(--xz-primary);
  font-size: 26px;
}

.avatar-ops {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-hint {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.base-form :deep(.el-form-item),
.verify-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

/* ---------- 领域标签 ---------- */
.tag-editor {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.domain-chip {
  background: var(--xz-primary-lighter);
  color: var(--xz-primary-dark);
  border-color: #cfe3e2;
}

.domain-input {
  width: 140px;
}

.domain-add {
  border-style: dashed;
}

.field-hint {
  font-size: 11.5px;
  line-height: 1.65;
  color: var(--xz-text-placeholder);
  margin-top: 6px;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.switch-hint {
  font-size: 12px;
  color: var(--xz-text-secondary);
  line-height: 1.6;
  flex: 1;
  min-width: 200px;
}

/* ---------- 认证 ---------- */
.verify-desc {
  font-size: 12.5px;
  line-height: 1.8;
  color: var(--xz-text-secondary);
  margin-bottom: 18px;
}

.verify-alert {
  margin-bottom: 18px;
  border-radius: var(--xz-radius-sm);
}

.uploaded-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 8px 14px;
  background: #f0f8f3;
  border-radius: var(--xz-radius-sm);
  font-size: 12.5px;
}

.uploaded-file a {
  color: var(--xz-primary);
  text-decoration: underline;
}

.is-empty {
  color: var(--xz-text-placeholder);
}

.record-table {
  --el-table-border-color: var(--xz-border-light);
}

/* ---------- 侧栏 ---------- */
.profile-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 84px;
}

.side-card {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 18px 20px;
}

.side-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
  margin-bottom: 14px;
  padding-left: 9px;
  border-left: 3px solid var(--xz-primary);
  line-height: 1.3;
}

.stat-side .stat-row {
  display: flex;
  align-items: baseline;
  gap: 9px;
  padding: 7px 0;
}

.stat-side .stat-row + .stat-row {
  border-top: 1px solid var(--xz-border-light);
}

.stat-num {
  font-size: 19px;
  font-weight: 700;
  color: var(--xz-primary);
  min-width: 52px;
  font-variant-numeric: tabular-nums;
}

.stat-txt {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.level-badge {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--xz-border-light);
}

.quota-line {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.quota-num {
  font-size: 21px;
  font-weight: 700;
  color: var(--xz-text-primary);
  font-variant-numeric: tabular-nums;
}

.quota-txt {
  font-size: 12px;
  color: var(--xz-text-secondary);
}

.quota-note {
  margin-top: 11px;
  font-size: 11.5px;
  line-height: 1.65;
  color: var(--xz-text-placeholder);
}

.meaning-list {
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.meaning-list li {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.meaning-list strong {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.meaning-list span {
  font-size: 11.5px;
  line-height: 1.7;
  color: var(--xz-text-secondary);
}

.pwd-btn {
  width: 100%;
}

@media (max-width: 1080px) {
  .profile-body {
    grid-template-columns: 1fr;
  }

  .profile-side {
    position: static;
  }
}

@media (max-width: 680px) {
  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .card {
    padding: 18px;
  }
}
</style>
