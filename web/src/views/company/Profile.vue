<template>
  <div class="company-profile" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>企业档案</h2>
        <p class="desc">完善企业信息并通过认证，才能进入人才检索与发起邀约</p>
      </div>
    </div>

    <div class="profile-grid">
      <div class="main-col">
        <!-- 认证状态 -->
        <section class="panel">
          <h3>企业认证状态</h3>
          <div v-if="verifyStatus === 1" class="verify-banner ok">
            <el-icon><CircleCheckFilled /></el-icon>
            <div>
              <b>已通过认证</b>
              <p>你已可使用人才检索、档案查验与邀约功能。</p>
            </div>
          </div>
          <div v-else-if="verifyStatus === 2" class="verify-banner fail">
            <el-icon><CircleCloseFilled /></el-icon>
            <div>
              <b>认证未通过</b>
              <p>{{ verifyRemark || '请检查营业执照与联系方式后重新提交。' }}</p>
            </div>
          </div>
          <div v-else class="verify-banner pending">
            <el-icon><Clock /></el-icon>
            <div>
              <b>{{ verifyStatus === 0 ? '待提交认证材料' : '认证审核中' }}</b>
              <p>
                {{
                  verifyStatus === 0
                    ? '请填写下方企业信息并上传营业执照。'
                    : '平台运营会在 1-2 个工作日内完成审核。'
                }}
              </p>
            </div>
          </div>

          <el-form
            ref="verifyFormRef"
            :model="verifyForm"
            :rules="verifyRules"
            label-width="110px"
            class="verify-form"
            v-if="verifyStatus === 0 || verifyStatus === 3"
          >
            <el-form-item label="企业名称" prop="companyName">
              <el-input v-model="verifyForm.companyName" placeholder="与营业执照一致" />
            </el-form-item>
            <el-form-item label="所属行业" prop="industry">
              <el-select v-model="verifyForm.industry" placeholder="请选择" style="width: 100%">
                <el-option v-for="o in INDUSTRIES" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
            <el-form-item label="企业规模">
              <el-select v-model="verifyForm.scale" placeholder="请选择" style="width: 100%">
                <el-option v-for="o in SCALES" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
            <el-form-item label="营业执照" prop="licenseFile">
              <div class="upload-row">
                <el-upload
                  :show-file-list="false"
                  :http-request="(o) => uploadLicense(o)"
                  accept="image/*,.pdf"
                >
                  <el-button :icon="UploadFilled">上传营业执照</el-button>
                </el-upload>
                <span v-if="verifyForm.licenseFile" class="uploaded">
                  <el-icon><CircleCheckFilled /></el-icon> 已上传
                </span>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="submitVerify">
                提交认证
              </el-button>
            </el-form-item>
          </el-form>
        </section>

        <!-- 企业信息 -->
        <section class="panel">
          <h3>企业信息</h3>
          <el-form
            ref="infoFormRef"
            :model="infoForm"
            :rules="infoRules"
            label-width="110px"
          >
            <el-form-item label="企业名称" prop="companyName">
              <el-input v-model="infoForm.companyName" />
            </el-form-item>
            <el-form-item label="所属行业">
              <el-select v-model="infoForm.industry" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="o in INDUSTRIES"
                  :key="o"
                  :label="o"
                  :value="o"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="企业规模">
              <el-select v-model="infoForm.scale" placeholder="请选择" style="width: 100%">
                <el-option v-for="o in SCALES" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveInfo">
                保存企业信息
              </el-button>
            </el-form-item>
          </el-form>
        </section>

        <!-- 认证记录 -->
        <section class="panel">
          <h3>认证记录</h3>
          <el-table :data="records" size="small" empty-text="暂无认证记录">
            <el-table-column label="提交时间" min-width="160">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="recordType(row.status)" effect="light">
                  {{ recordText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核意见" min-width="240">
              <template #default="{ row }">{{ row.remark || '—' }}</template>
            </el-table-column>
          </el-table>
        </section>
      </div>

      <!-- 侧栏 -->
      <aside class="side-col">
        <div class="panel quota-panel">
          <h3>套餐与额度</h3>
          <div class="quota-big">
            <span class="num">{{ stat.remainQuota ?? 0 }}</span>
            <span class="unit">次</span>
          </div>
          <div class="quota-sub">
            本月已用 {{ stat.monthUsed || 0 }} / {{ stat.monthQuota || 0 }}
          </div>
          <el-progress
            :percentage="quotaPercent"
            :stroke-width="8"
            :show-text="false"
            color="#1f6f6b"
          />
          <ul class="quota-list">
            <li>
              <span>套餐类型</span>
              <b>{{ packageText }}</b>
            </li>
            <li>
              <span>到期时间</span>
              <b>{{ stat.packageExpire ? formatDate(stat.packageExpire) : '—' }}</b>
            </li>
          </ul>
        </div>

        <div class="panel">
          <h3>账号安全</h3>
          <el-form :model="pwdForm" label-position="top" class="pwd-form">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-button
              type="primary"
              plain
              style="width: 100%"
              :loading="changingPwd"
              @click="changePwd"
            >
              修改密码
            </el-button>
          </el-form>
        </div>

        <div class="panel">
          <h3>功能说明</h3>
          <ul class="tips">
            <li>认证通过后可检索全平台已公开的学生作品集</li>
            <li>每次查验候选人完整档案消耗 1 次额度</li>
            <li>邀约候选人需保证信息真实、岗位真实</li>
            <li>查验与邀约记录均对候选人可见</li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UploadFilled, CircleCheckFilled, CircleCloseFilled, Clock
} from '@element-plus/icons-vue'
import { profileApi, verifyApi, companyApi, authApi, fileApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const INDUSTRIES = [
  '互联网/软件', '人工智能', '金融/保险', '电子商务', '智能制造',
  '教育培训', '医疗健康', '文化传媒', '咨询服务', '其他'
]
const SCALES = ['1-50人', '50-200人', '200-1000人', '1000-5000人', '5000人以上']

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const changingPwd = ref(false)

const verifyStatus = ref(0)
const verifyRemark = ref('')
const records = ref([])
const stat = ref({})

const infoFormRef = ref()
const verifyFormRef = ref()

const infoForm = ref({
  companyName: '', industry: '', scale: ''
})

const verifyForm = ref({
  companyName: '', industry: '', scale: '', licenseFile: ''
})

const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const infoRules = {
  companyName: [{ required: true, message: '请填写企业名称', trigger: 'blur' }]
}

const verifyRules = {
  companyName: [{ required: true, message: '请填写企业名称', trigger: 'blur' }],
  industry: [{ required: true, message: '请选择所属行业', trigger: 'change' }],
  licenseFile: [{ required: true, message: '请上传营业执照', trigger: 'change' }]
}

const quotaPercent = computed(() => {
  const total = stat.value.monthQuota || 0
  const used = stat.value.monthUsed || 0
  if (!total) return 0
  return Math.min(100, Math.round((used / total) * 100))
})

const packageText = computed(() => {
  const map = { FREE: '免费版', BASIC: '基础版', PRO: '专业版', ENTERPRISE: '企业版' }
  return map[stat.value.packageType] || '免费版'
})

function formatDate(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 16)
}

function recordType(s) {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info'
}

function recordText(s) {
  return { 0: '待审核', 1: '已通过', 2: '已驳回' }[s] || '—'
}

async function load() {
  loading.value = true
  try {
    const me = await userStore.fetchMe()
    const cp = me?.companyProfile || {}

    infoForm.value = {
      companyName: cp.companyName || '',
      industry: cp.industry || '',
      scale: cp.scale || ''
    }

    verifyForm.value = {
      companyName: cp.companyName || '',
      industry: cp.industry || '',
      scale: cp.scale || '',
      licenseFile: cp.licenseFile || ''
    }

    verifyStatus.value = cp.verifyStatus ?? 0
    verifyRemark.value = cp.verifyRemark || ''
  } catch (e) {
    /* 已提示 */
  } finally {
    loading.value = false
  }

  try {
    const rec = await verifyApi.myRecords()
    records.value = Array.isArray(rec) ? rec : rec?.records || []
  } catch (e) {
    records.value = []
  }

  try {
    const s = await companyApi.statistics()
    stat.value = s || {}
    if (s.verifyStatus !== undefined) verifyStatus.value = s.verifyStatus
  } catch (e) {
    /* 忽略 */
  }
}

async function saveInfo() {
  try {
    await infoFormRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    await profileApi.updateCompany(infoForm.value)
    ElMessage.success('企业信息已保存')
    userStore.fetchMe()
  } catch (e) {
    /* 已提示 */
  } finally {
    saving.value = false
  }
}

async function uploadLicense(option) {
  try {
    const res = await fileApi.upload(option.file, 'VERIFY')
    const url = res.url || res.path || res.fileUrl
    verifyForm.value.licenseFile = url
    verifyFormRef.value?.validateField('licenseFile')
    ElMessage.success('营业执照上传成功')
  } catch (e) {
    /* 已提示 */
  }
}

async function submitVerify() {
  try {
    await verifyFormRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    await verifyApi.submitCompany({ ...verifyForm.value })
    ElMessage.success('认证材料已提交，等待平台审核')
    load()
  } catch (e) {
    /* 已提示 */
  } finally {
    submitting.value = false
  }
}

async function changePwd() {
  const f = pwdForm.value
  if (!f.oldPassword || !f.newPassword) {
    ElMessage.warning('请完整填写密码')
    return
  }
  if (f.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (f.newPassword !== f.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  changingPwd.value = true
  try {
    await authApi.changePassword({
      oldPassword: f.oldPassword,
      newPassword: f.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    /* 已提示 */
  } finally {
    changingPwd.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.company-profile {
  padding-bottom: 30px;
}

.page-head {
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 22px;
    color: var(--xz-text-primary);
  }

  .desc {
    margin: 8px 0 0;
    font-size: 13px;
    color: #8a9a99;
  }
}

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 330px;
  gap: 20px;
}

.main-col,
.side-col {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.panel {
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 22px 24px;
  box-shadow: var(--xz-shadow-sm);

  h3 {
    margin: 0 0 16px;
    font-size: 16px;
    color: var(--xz-text-primary);
  }
}

.verify-banner {
  display: flex;
  gap: 12px;
  padding: 15px 18px;
  border-radius: 12px;
  margin-bottom: 18px;
  font-size: 13px;

  .el-icon {
    font-size: 20px;
    margin-top: 1px;
  }

  b {
    display: block;
    margin-bottom: 3px;
    font-size: 14px;
  }

  p {
    margin: 0;
    line-height: 1.7;
    opacity: 0.85;
  }

  &.ok {
    background: #eef8f4;
    border: 1px solid #d5ede2;
    color: #2c6e57;
  }

  &.pending {
    background: #fff8ee;
    border: 1px solid #f6e2c4;
    color: #8a6534;
  }

  &.fail {
    background: #fdf1f0;
    border: 1px solid #f6d9d7;
    color: #a4483f;
  }
}

.verify-form {
  margin-top: 6px;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.uploaded {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #52b788;
}

/* 侧栏 */
.quota-panel {
  .quota-big {
    display: flex;
    align-items: baseline;
    gap: 4px;

    .num {
      font-size: 40px;
      font-weight: 700;
      color: var(--xz-primary);
      line-height: 1;
    }

    .unit {
      font-size: 14px;
      color: #8a9a99;
    }
  }

  .quota-sub {
    margin: 10px 0 10px;
    font-size: 12px;
    color: #8a9a99;
  }
}

.quota-list {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;

  li {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    font-size: 13px;
    border-bottom: 1px dashed #f0f4f4;

    &:last-child {
      border-bottom: none;
    }

    span {
      color: #8a9a99;
    }

    b {
      color: var(--xz-text-primary);
    }
  }
}

.pwd-form {
  :deep(.el-form-item) {
    margin-bottom: 14px;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
    padding-bottom: 4px;
  }
}

.tips {
  list-style: none;
  margin: 0;
  padding: 0;

  li {
    position: relative;
    padding-left: 16px;
    margin-bottom: 10px;
    font-size: 13px;
    line-height: 1.7;
    color: var(--xz-text-regular);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 8px;
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: var(--xz-accent);
    }
  }
}
</style>
