<template>
  <div class="student-invitations">
    <div class="page-head">
      <div>
        <h2 class="page-title">企业邀约</h2>
        <p class="page-sub">
          企业查看你的作品集后，可能会直接发来面试或实习邀约。同意后你的联系方式会对该企业开放。
        </p>
      </div>
      <el-button plain @click="router.push('/square')">
        <el-icon><Search /></el-icon>
        去项目广场
      </el-button>
    </div>

    <section class="summary-strip">
      <div class="summary-item">
        <span class="summary-value">{{ counts.all }}</span>
        <span class="summary-label">收到邀约</span>
      </div>
      <div class="summary-item">
        <span class="summary-value is-warning">{{ counts.pending }}</span>
        <span class="summary-label">待回应</span>
      </div>
      <div class="summary-item">
        <span class="summary-value is-success">{{ counts.accepted }}</span>
        <span class="summary-label">已同意</span>
      </div>
      <div class="summary-item">
        <span class="summary-value">{{ counts.rejected }}</span>
        <span class="summary-label">已拒绝</span>
      </div>
    </section>

    <div class="filter-bar">
      <el-radio-group v-model="filter" @change="applyFilter">
        <el-radio-button value="ALL">全部</el-radio-button>
        <el-radio-button value="1">待回应</el-radio-button>
        <el-radio-button value="2">已同意</el-radio-button>
        <el-radio-button value="3">已拒绝</el-radio-button>
      </el-radio-group>
      <span class="result-count">共 {{ filtered.length }} 条</span>
    </div>

    <div v-loading="loading" class="invitation-body">
      <div v-if="filtered.length" class="invitation-list">
        <article
          v-for="inv in pagedInvitations"
          :key="inv.id"
          class="invitation-card"
          :class="`is-${statusKey(inv.status)}`"
        >
          <header class="card-head">
            <div class="company-mark">
              <el-icon :size="20"><OfficeBuilding /></el-icon>
            </div>
            <div class="company-info">
              <h3 class="company-name">
                {{ inv.companyName || '企业用户' }}
                <el-tag size="small" type="success" effect="plain" v-if="inv.companyIndustry">
                  {{ inv.companyIndustry }}
                </el-tag>
              </h3>
              <span class="company-meta">
                发送于 {{ formatDateTime(inv.createTime) }}
                <template v-if="inv.replyTime">
                  <span class="dot">·</span>
                  回应于 {{ formatDateTime(inv.replyTime) }}
                </template>
              </span>
            </div>
            <el-tag
              :type="INVITATION_STATUS[inv.status]?.type"
              effect="plain"
              size="large"
            >
              {{ INVITATION_STATUS[inv.status]?.text }}
            </el-tag>
          </header>

          <div class="job-line">
            <el-icon><Briefcase /></el-icon>
            <span class="job-title">{{ inv.jobTitle }}</span>
            <router-link
              v-if="inv.projectId"
              :to="`/project/${inv.projectId}`"
              class="project-link"
            >
              关联项目 →
            </router-link>
          </div>

          <div class="invitation-content">
            <p>{{ inv.content }}</p>
          </div>

          <div v-if="inv.contactInfo && inv.status === 2" class="contact-line">
            <el-icon><Message /></el-icon>
            <span class="contact-label">联系方式</span>
            <span class="contact-value">{{ inv.contactInfo }}</span>
          </div>

          <footer v-if="inv.status === 1" class="card-foot">
            <el-button type="primary" @click="openReply(inv)">
              <el-icon><CircleCheck /></el-icon>
              同意并接受
            </el-button>
            <el-button @click="onReject(inv)">
              <el-icon><Close /></el-icon>
              婉拒
            </el-button>
            <span class="foot-hint">同意后你的联系方式将对该企业开放</span>
          </footer>

          <footer v-else class="card-foot is-done">
            <span class="done-text">
              <template v-if="inv.status === 2">
                你已同意该邀约，请留意企业的后续联系。
              </template>
              <template v-else>你已婉拒该邀约。</template>
            </span>
          </footer>
        </article>
      </div>

      <el-empty v-else-if="!loading" description="还没有收到企业邀约">
        <el-button type="primary" @click="router.push('/student/projects')">
          完善作品集，提高被发现的概率
        </el-button>
      </el-empty>

      <div v-if="filtered.length > pageSize" class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="filtered.length"
          layout="prev, pager, next, jumper"
          background
        />
      </div>
    </div>

    <!-- 同意弹窗 -->
    <el-dialog v-model="replyVisible" title="同意邀约" width="560px" destroy-on-close>
      <p class="dialog-hint">
        同意后企业可看到你填写的联系方式。建议填写常用邮箱或手机号，便于企业快速联系。
      </p>

      <div class="reply-job">
        <el-icon><Briefcase /></el-icon>
        <div class="reply-job-body">
          <span class="reply-job-title">{{ replyTarget?.jobTitle }}</span>
          <span class="reply-job-company">{{ replyTarget?.companyName }}</span>
        </div>
      </div>

      <el-form label-position="top" class="reply-form">
        <el-form-item label="你的联系方式" required>
          <el-input
            v-model="contactInfo"
            placeholder="如：zhangsan@example.com / 138****8888"
            maxlength="80"
          />
          <div class="field-hint">
            请确认联系方式准确无误；此项会覆盖企业原本预留的联系方式。
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="submitAccept">确认同意</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { invitationApi } from '@/api'
import { INVITATION_STATUS, formatDateTime } from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const allInvitations = ref([])
const filter = ref('ALL')
const pageNum = ref(1)
const pageSize = ref(6)

const statusKey = (status) => ({ 1: 'pending', 2: 'accepted', 3: 'rejected' })[status] || 'pending'

const filtered = computed(() => {
  if (filter.value === 'ALL') return allInvitations.value
  return allInvitations.value.filter((i) => String(i.status) === filter.value)
})

const pagedInvitations = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

const counts = computed(() => {
  const list = allInvitations.value
  return {
    all: list.length,
    pending: list.filter((i) => i.status === 1).length,
    accepted: list.filter((i) => i.status === 2).length,
    rejected: list.filter((i) => i.status === 3).length
  }
})

const applyFilter = () => {
  pageNum.value = 1
}

const load = async () => {
  loading.value = true
  try {
    const data = await invitationApi.received({ pageNum: 1, pageSize: 200 })
    allInvitations.value = data.records || []
  } catch (e) {
    allInvitations.value = []
  } finally {
    loading.value = false
  }
}

/* ---------- 同意 ---------- */
const replyVisible = ref(false)
const replying = ref(false)
const contactInfo = ref('')
const replyTarget = ref(null)

const openReply = (inv) => {
  replyTarget.value = inv
  contactInfo.value = userStore.user?.email || userStore.user?.phone || ''
  replyVisible.value = true
}

const submitAccept = async () => {
  if (!contactInfo.value.trim()) {
    ElMessage.warning('请填写联系方式')
    return
  }
  replying.value = true
  try {
    await invitationApi.reply(replyTarget.value.id, true, contactInfo.value.trim())
    ElMessage.success('已同意邀约，企业会尽快联系你')
    replyVisible.value = false
    load()
  } catch (e) {
    /* 已提示 */
  } finally {
    replying.value = false
  }
}

/* ---------- 拒绝 ---------- */
const onReject = async (inv) => {
  try {
    await ElMessageBox.confirm(
      `确定婉拒「${inv.companyName || '该企业'}」的邀约吗？婉拒后该企业无法再次向你发送邀约。`,
      '婉拒邀约',
      { type: 'warning', confirmButtonText: '确认婉拒' }
    )
    await invitationApi.reply(inv.id, false)
    ElMessage.success('已婉拒该邀约')
    load()
  } catch (e) {
    /* 取消或失败 */
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
  margin-bottom: 18px;
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

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  background: var(--xz-border-light);
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius);
  overflow: hidden;
  margin-bottom: 18px;
}

.summary-item {
  background: #ffffff;
  padding: 16px 22px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.summary-value.is-warning {
  color: var(--xz-accent);
}

.summary-value.is-success {
  color: var(--xz-success);
}

.summary-label {
  font-size: 12px;
  color: var(--xz-text-secondary);
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.result-count {
  font-size: 13px;
  color: var(--xz-text-secondary);
}

.invitation-body {
  min-height: 200px;
}

.invitation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.invitation-card {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 22px 26px;
  position: relative;
  overflow: hidden;
}

.invitation-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
}

.invitation-card.is-pending::before {
  background: var(--xz-accent);
}

.invitation-card.is-accepted::before {
  background: var(--xz-success);
}

.invitation-card.is-rejected::before {
  background: #c3ced6;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 14px;
}

.company-mark {
  width: 44px;
  height: 44px;
  border-radius: 11px;
  background: var(--xz-primary-lighter);
  color: var(--xz-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.company-info {
  flex: 1;
  min-width: 0;
}

.company-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--xz-text-primary);
  flex-wrap: wrap;
}

.company-meta {
  display: block;
  margin-top: 3px;
  font-size: 12px;
  color: var(--xz-text-placeholder);
}

.dot {
  opacity: 0.6;
}

.job-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 15px;
  background: #f8fbfb;
  border-radius: var(--xz-radius-sm);
  font-size: 13px;
}

.job-line .el-icon {
  color: var(--xz-primary);
}

.job-title {
  font-weight: 600;
  color: var(--xz-text-primary);
}

.project-link {
  margin-left: auto;
  font-size: 12.5px;
  color: var(--xz-primary);
}

.project-link:hover {
  text-decoration: underline;
}

.invitation-content {
  margin-top: 14px;
}

.invitation-content p {
  font-size: 13.5px;
  line-height: 1.9;
  color: var(--xz-text-primary);
  white-space: pre-wrap;
}

.contact-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  padding: 11px 15px;
  background: #f0f8f3;
  border-radius: var(--xz-radius-sm);
  font-size: 13px;
}

.contact-line .el-icon {
  color: var(--xz-success);
}

.contact-label {
  color: var(--xz-text-secondary);
}

.contact-value {
  font-weight: 600;
  color: var(--xz-text-primary);
  font-family: 'JetBrains Mono', Consolas, monospace;
}

.card-foot {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 18px;
  padding-top: 15px;
  border-top: 1px solid var(--xz-border-light);
  flex-wrap: wrap;
}

.card-foot.is-done {
  padding-top: 13px;
}

.done-text {
  font-size: 12.5px;
  color: var(--xz-text-placeholder);
}

.foot-hint {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-left: auto;
}

.pagination-wrap {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.dialog-hint {
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--xz-text-secondary);
  margin-bottom: 14px;
}

.reply-job {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 17px;
  background: #f8fbfb;
  border-radius: var(--xz-radius-sm);
  margin-bottom: 18px;
}

.reply-job .el-icon {
  color: var(--xz-primary);
  font-size: 19px;
}

.reply-job-body {
  display: flex;
  flex-direction: column;
}

.reply-job-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.reply-job-company {
  font-size: 12px;
  color: var(--xz-text-secondary);
  margin-top: 2px;
}

.field-hint {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 6px;
  line-height: 1.6;
}

@media (max-width: 900px) {
  .summary-strip {
    grid-template-columns: repeat(2, 1fr);
  }

  .foot-hint {
    margin-left: 0;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .invitation-card {
    padding: 18px;
  }

  .card-head {
    flex-wrap: wrap;
  }
}
</style>
