<template>
  <div v-loading="loading" class="student-profile">
    <div class="page-head">
      <div>
        <h2 class="page-title">我的档案</h2>
        <p class="page-sub">
          档案与技能标签会影响企业在人才检索中能否找到你，也会显示在你的公开作品集页头部。
        </p>
      </div>
      <el-button
        v-if="allowSearch"
        plain
        @click="router.push(`/portfolio/${userStore.user?.id}`)"
      >
        <el-icon><View /></el-icon>
        预览我的公开作品集
      </el-button>
    </div>

    <div class="profile-body">
      <div class="profile-main">
        <!-- 基础资料 -->
        <section class="card">
          <h3 class="card-title">基础资料</h3>

          <div class="avatar-row">
            <el-avatar :size="72" :src="base.avatar" class="avatar">
              {{ (userStore.displayName || '同').slice(0, 1) }}
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
              <span class="avatar-hint">建议使用清晰的正装照或证件照</span>
            </div>
          </div>

          <el-form :model="base" label-position="top">
            <div class="form-row">
              <el-form-item label="昵称">
                <el-input v-model="base.nickname" placeholder="作品集公开展示的名称" maxlength="30" />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input
                  v-model="base.realName"
                  placeholder="仅在学籍认证通过且开放检索时对企业可见"
                  maxlength="20"
                />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="手机号">
                <el-input v-model="base.phone" placeholder="同意企业邀约后对企业开放" maxlength="20" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="base.email" placeholder="用于接收点评与邀约通知" maxlength="60" />
              </el-form-item>
            </div>
            <el-button type="primary" :loading="savingBase" @click="saveBase">保存基础资料</el-button>
          </el-form>
        </section>

        <!-- 在校信息 -->
        <section class="card">
          <h3 class="card-title">在校信息</h3>

          <el-form :model="student" label-position="top">
            <div class="form-row">
              <el-form-item label="学校">
                <el-input v-model="student.school" placeholder="如：华中科技大学" maxlength="60" />
              </el-form-item>
              <el-form-item label="专业">
                <el-input v-model="student.major" placeholder="如：计算机科学与技术" maxlength="60" />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="专业大类">
                <el-select v-model="student.majorCategory" placeholder="请选择" class="full-width">
                  <el-option v-for="c in CATEGORIES" :key="c.value" :label="c.label" :value="c.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="学历">
                <el-select v-model="student.degree" placeholder="请选择" class="full-width">
                  <el-option label="专科" value="专科" />
                  <el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" />
                  <el-option label="博士" value="博士" />
                </el-select>
              </el-form-item>
            </div>

            <el-form-item label="毕业年份">
              <el-select v-model="student.graduateYear" placeholder="请选择" class="full-width">
                <el-option
                  v-for="y in gradYears"
                  :key="y"
                  :label="`${y} 年`"
                  :value="y"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="技能标签">
              <div class="tag-editor">
                <el-tag
                  v-for="tag in skillTags"
                  :key="tag"
                  closable
                  class="skill-chip"
                  @close="removeSkill(tag)"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="skillInputVisible"
                  ref="skillInputRef"
                  v-model="skillInput"
                  size="small"
                  class="skill-input"
                  placeholder="输入后回车"
                  @keyup.enter="confirmSkill"
                  @blur="confirmSkill"
                />
                <el-button v-else size="small" class="skill-add" @click="showSkillInput">
                  <el-icon><Plus /></el-icon>
                  添加技能
                </el-button>
              </div>
              <div class="field-hint">
                如 Java、SpringBoot、MySQL、Figma、用户研究。企业检索时会按标签匹配，建议填 5-10 个。
              </div>
            </el-form-item>

            <el-form-item label="一句话自述">
              <el-input
                v-model="student.bio"
                type="textarea"
                :rows="3"
                maxlength="150"
                show-word-limit
                placeholder="如：计算机专业应届生，做过 3 个完整的 Web 项目，偏后端与数据库方向，正在准备秋招。"
              />
            </el-form-item>

            <el-form-item label="企业检索设置">
              <div class="switch-row">
                <el-switch
                  v-model="allowSearch"
                  active-text="允许企业检索到我"
                  inactive-text="不参与企业检索"
                  inline-prompt
                />
                <span class="switch-hint">
                  关闭后，企业的人才检索中不会出现你，但你的公开作品集仍可通过链接访问。
                </span>
              </div>
            </el-form-item>

            <el-button type="primary" :loading="savingStudent" @click="saveStudent">
              保存在校信息
            </el-button>
          </el-form>
        </section>

        <!-- 学籍认证 -->
        <section class="card">
          <div class="card-head">
            <h3 class="card-title no-margin">学籍认证</h3>
            <el-tag :type="VERIFY_STATUS[eduVerified]?.type" effect="plain">
              {{ VERIFY_STATUS[eduVerified]?.text }}
            </el-tag>
          </div>

          <el-alert
            v-if="eduVerified === 3"
            type="error"
            :closable="false"
            show-icon
            class="verify-alert"
          >
            <template #title>认证被驳回</template>
            <template #default>{{ student.eduVerifyRemark || '请查看审核意见，补充材料后重新提交。' }}</template>
          </el-alert>

          <el-alert
            v-else-if="eduVerified === 1"
            type="warning"
            :closable="false"
            show-icon
            class="verify-alert"
          >
            <template #title>材料已提交，平台正在审核</template>
            <template #default>通常在 1-2 个工作日内完成。审核通过后作品集会展示「学籍已认证」标识。</template>
          </el-alert>

          <template v-if="eduVerified === 0 || eduVerified === 3">
            <p class="verify-desc">
              认证通过后，你的作品集会展示「学籍已认证」标识，企业检索时也可以按「仅看已认证」筛选，
              显著提升可信度。请上传学生证照片或学校开具的在读证明。
            </p>

            <el-form :model="verifyForm" label-position="top">
              <div class="form-row">
                <el-form-item label="学校" required>
                  <el-input v-model="verifyForm.school" placeholder="学校全称" maxlength="60" />
                </el-form-item>
                <el-form-item label="专业" required>
                  <el-input v-model="verifyForm.major" placeholder="专业名称" maxlength="60" />
                </el-form-item>
              </div>
              <div class="form-row">
                <el-form-item label="专业大类">
                  <el-select v-model="verifyForm.majorCategory" placeholder="请选择" class="full-width">
                    <el-option v-for="c in CATEGORIES" :key="c.value" :label="c.label" :value="c.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="学历">
                  <el-select v-model="verifyForm.degree" placeholder="请选择" class="full-width">
                    <el-option label="专科" value="专科" />
                    <el-option label="本科" value="本科" />
                    <el-option label="硕士" value="硕士" />
                    <el-option label="博士" value="博士" />
                  </el-select>
                </el-form-item>
              </div>
              <el-form-item label="毕业年份">
                <el-select v-model="verifyForm.graduateYear" placeholder="请选择" class="full-width">
                  <el-option v-for="y in gradYears" :key="y" :label="`${y} 年`" :value="y" />
                </el-select>
              </el-form-item>

              <el-form-item label="证明材料" required>
                <el-upload
                  :show-file-list="false"
                  accept=".pdf,.jpg,.jpeg,.png"
                  :before-upload="beforeVerifyFile"
                  :http-request="doVerifyUpload"
                >
                  <el-button :loading="verifyUploading">
                    <el-icon><Upload /></el-icon>
                    上传学生证 / 在读证明
                  </el-button>
                </el-upload>
                <div v-if="verifyForm.eduVerifyFile" class="uploaded-file">
                  <el-icon><Document /></el-icon>
                  <a :href="verifyForm.eduVerifyFile" target="_blank" rel="noopener">已上传，点击查看</a>
                  <el-button size="small" text type="danger" @click="verifyForm.eduVerifyFile = ''">
                    移除
                  </el-button>
                </div>
                <div v-else class="field-hint">
                  支持 PDF / 图片，不超过 10MB。请在材料上标注「仅用于校栈学籍认证」以防盗用。
                </div>
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
            <el-table-column label="业务类型" width="110">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ BIZ_TYPE_TEXT[row.bizType] || row.bizType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
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
        <div class="side-card">
          <h4 class="side-title">档案完整度</h4>
          <div class="completeness">
            <div class="completeness-head">
              <span class="completeness-value">{{ completeness }}%</span>
              <span class="completeness-label">已完成</span>
            </div>
            <el-progress
              :percentage="completeness"
              :stroke-width="8"
              :show-text="false"
              :color="completenessColor"
            />
          </div>
          <ul class="check-list">
            <li v-for="c in checklist" :key="c.label" :class="{ 'is-done': c.done }">
              <el-icon>
                <component :is="c.done ? 'CircleCheckFilled' : 'CircleClose'" />
              </el-icon>
              <span>{{ c.label }}</span>
            </li>
          </ul>
          <p class="check-tip">档案越完整，被企业检索到并主动联系的概率越高。</p>
        </div>

        <div class="side-card">
          <h4 class="side-title">作品集数据</h4>
          <div class="data-row">
            <span class="data-num">{{ stats.totalProjects || 0 }}</span>
            <span class="data-txt">项目总数</span>
          </div>
          <div class="data-row">
            <span class="data-num">{{ stats.totalReviews || 0 }}</span>
            <span class="data-txt">获得点评</span>
          </div>
          <div class="data-row">
            <span class="data-num">{{ stats.totalViews || 0 }}</span>
            <span class="data-txt">项目总浏览</span>
          </div>
          <div class="data-row">
            <span class="data-num">{{ stats.portfolioViews || 0 }}</span>
            <span class="data-txt">作品集被查阅</span>
          </div>
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
import { authApi, fileApi, profileApi, projectApi, verifyApi } from '@/api'
import { CATEGORIES, VERIFY_STATUS, formatDateTime } from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const savingBase = ref(false)
const savingStudent = ref(false)
const savingPwd = ref(false)
const submittingVerify = ref(false)
const avatarUploading = ref(false)
const verifyUploading = ref(false)

const records = ref([])
const stats = ref({})

const BIZ_TYPE_TEXT = { STUDENT: '学籍认证', EXPERT: '专家认证', COMPANY: '企业认证' }

const base = reactive({
  nickname: '',
  realName: '',
  avatar: '',
  phone: '',
  email: ''
})

const student = reactive({
  school: '',
  major: '',
  majorCategory: '',
  degree: '',
  graduateYear: null,
  domain: '',
  skillTags: [],
  bio: '',
  allowCompanySearch: 1,
  eduVerified: 0,
  eduVerifyRemark: ''
})

const verifyForm = reactive({
  school: '',
  major: '',
  majorCategory: '',
  degree: '',
  graduateYear: null,
  eduVerifyFile: ''
})

const pwd = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const currentYear = new Date().getFullYear()
const gradYears = Array.from({ length: 8 }, (_, i) => currentYear + i - 2)

const eduVerified = computed(() => student.eduVerified || 0)

const allowSearch = computed({
  get: () => student.allowCompanySearch === 1,
  set: (v) => {
    student.allowCompanySearch = v ? 1 : 0
  }
})

const skillTags = computed(() => student.skillTags || [])

const completeness = computed(() => {
  const items = checklist.value
  const done = items.filter((i) => i.done).length
  return Math.round((done / items.length) * 100)
})

const completenessColor = computed(() =>
  completeness.value >= 80 ? '#3f9e6a' : completeness.value >= 50 ? '#d99a2b' : '#cc6b4b'
)

const checklist = computed(() => [
  { label: '已填写昵称', done: !!base.nickname },
  { label: '已填写学校与专业', done: !!(student.school && student.major) },
  { label: '已设置专业大类与学历', done: !!(student.majorCategory && student.degree) },
  { label: '已添加 5 个以上技能标签', done: (student.skillTags || []).length >= 5 },
  { label: '已填写一句话自述', done: !!student.bio },
  { label: '已上传头像', done: !!base.avatar },
  { label: '已通过学籍认证', done: eduVerified.value === 2 },
  { label: '已开放企业检索', done: student.allowCompanySearch === 1 }
])

/* ---------- 技能标签 ---------- */
const skillInputVisible = ref(false)
const skillInput = ref('')
const skillInputRef = ref()

const showSkillInput = () => {
  skillInputVisible.value = true
  setTimeout(() => skillInputRef.value?.focus(), 30)
}

const confirmSkill = () => {
  const value = skillInput.value.trim()
  if (value && !(student.skillTags || []).includes(value)) {
    student.skillTags = [...(student.skillTags || []), value]
  }
  skillInput.value = ''
  skillInputVisible.value = false
}

const removeSkill = (tag) => {
  student.skillTags = (student.skillTags || []).filter((t) => t !== tag)
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
    verifyForm.eduVerifyFile = res.url
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

const saveStudent = async () => {
  savingStudent.value = true
  try {
    await profileApi.updateStudent({
      school: student.school,
      major: student.major,
      majorCategory: student.majorCategory || undefined,
      degree: student.degree || undefined,
      graduateYear: student.graduateYear || undefined,
      skillTags: student.skillTags || [],
      bio: student.bio,
      allowCompanySearch: student.allowCompanySearch
    })
    await userStore.fetchMe()
    ElMessage.success('在校信息已保存')
  } catch (e) {
    /* 已提示 */
  } finally {
    savingStudent.value = false
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
    await authApi.changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
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
  if (!verifyForm.school?.trim() || !verifyForm.major?.trim()) {
    ElMessage.warning('请填写学校与专业')
    return
  }
  if (!verifyForm.eduVerifyFile) {
    ElMessage.warning('请上传学生证或在读证明')
    return
  }
  submittingVerify.value = true
  try {
    await verifyApi.submitEdu({ ...verifyForm })
    await userStore.fetchMe()
    ElMessage.success('认证申请已提交，请等待平台审核')
    await Promise.all([loadRecords(), syncFromStore()])
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

const syncFromStore = async () => {
  const user = userStore.user
  if (!user) return
  base.nickname = user.nickname || ''
  base.realName = user.realName || ''
  base.avatar = user.avatar || ''
  base.phone = user.phone || ''
  base.email = user.email || ''

  const sp = user.studentProfile || {}
  student.school = sp.school || ''
  student.major = sp.major || ''
  student.majorCategory = sp.majorCategory || ''
  student.degree = sp.degree || ''
  student.graduateYear = sp.graduateYear || null
  student.skillTags = sp.skillTags || []
  student.bio = sp.bio || ''
  student.allowCompanySearch = sp.allowCompanySearch === 0 ? 0 : 1
  student.eduVerified = sp.eduVerified ?? 0
  student.eduVerifyRemark = sp.eduVerifyRemark || ''
}

const load = async () => {
  loading.value = true
  try {
    await userStore.fetchMe().catch(() => null)
    await syncFromStore()

    verifyForm.school = student.school
    verifyForm.major = student.major
    verifyForm.majorCategory = student.majorCategory
    verifyForm.degree = student.degree
    verifyForm.graduateYear = student.graduateYear

    const s = await projectApi.statistics().catch(() => null)
    if (s) stats.value = s
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

.full-width {
  width: 100%;
}

.tag-editor {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.skill-chip {
  background: var(--xz-primary-lighter);
  color: var(--xz-primary-dark);
  border-color: #cfe3e2;
}

.skill-input {
  width: 140px;
}

.skill-add {
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

.completeness-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.completeness-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.completeness-label {
  font-size: 12px;
  color: var(--xz-text-secondary);
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin-top: 16px;
}

.check-list li {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12.5px;
  color: var(--xz-text-placeholder);
}

.check-list li .el-icon {
  font-size: 14px;
}

.check-list li.is-done {
  color: var(--xz-text-primary);
}

.check-list li.is-done .el-icon {
  color: var(--xz-success);
}

.check-tip {
  margin-top: 14px;
  padding-top: 13px;
  border-top: 1px solid var(--xz-border-light);
  font-size: 11.5px;
  line-height: 1.65;
  color: var(--xz-text-placeholder);
}

.data-row {
  display: flex;
  align-items: baseline;
  gap: 9px;
  padding: 7px 0;
}

.data-row + .data-row {
  border-top: 1px solid var(--xz-border-light);
}

.data-num {
  font-size: 19px;
  font-weight: 700;
  color: var(--xz-primary);
  min-width: 52px;
  font-variant-numeric: tabular-nums;
}

.data-txt {
  font-size: 12.5px;
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
