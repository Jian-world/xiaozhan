<template>
  <div v-loading="loading" class="company-dashboard">
    <!-- 认证提示 -->
    <section v-if="verifyStatus !== 2" class="verify-banner" :class="`is-${verifyTone}`">
      <el-icon :size="26"><component :is="verifyIcon" /></el-icon>
      <div class="banner-body">
        <span class="banner-title">{{ bannerTitle }}</span>
        <span class="banner-desc">{{ bannerDesc }}</span>
      </div>
      <el-button
        v-if="verifyStatus === 0 || verifyStatus === 3"
        type="primary"
        plain
        @click="router.push('/company/profile')"
      >
        前往认证
      </el-button>
    </section>

    <!-- 欢迎条 -->
    <section class="welcome">
      <div class="welcome-copy">
        <h2 class="welcome-title">{{ greeting }}，{{ userStore.displayName }}</h2>
        <p class="welcome-desc">
          <template v-if="verifyStatus !== 2">
            完成企业认证后即可使用人才检索与作品集查验。认证仅需营业执照，通常在 1-2 个工作日内完成。
          </template>
          <template v-else-if="stats.remainQuota <= 0">
            本月查验额度已用完（{{ stats.monthQuota }} 次），下月 1 日自动重置。
          </template>
          <template v-else-if="stats.pendingInvitationCount > 0">
            有 {{ stats.pendingInvitationCount }} 条邀约等待学生回应，可以先去人才库看看新的候选人。
          </template>
          <template v-else>
            本月还可查验 {{ stats.remainQuota }} 份作品集。建议先按专业大类筛选，再重点查看有专家点评的项目。
          </template>
        </p>
        <div class="welcome-actions">
          <el-button
            type="primary"
            :disabled="verifyStatus !== 2"
            @click="router.push('/company/talent')"
          >
            <el-icon><Search /></el-icon>
            开始人才检索
          </el-button>
          <el-button @click="router.push('/square')">
            <el-icon><View /></el-icon>
            浏览项目广场
          </el-button>
        </div>
      </div>

      <div class="quota-card">
        <div class="quota-head">
          <el-tag :type="PACKAGE_TYPE[stats.packageType] || 'info'" effect="dark" size="large">
            {{ PACKAGE_TEXT[stats.packageType] || '免费版' }}
          </el-tag>
          <span class="quota-expire">
            {{ stats.packageExpire ? `到期 ${formatDate(stats.packageExpire)}` : '长期有效' }}
          </span>
        </div>
        <div class="quota-body">
          <span class="quota-num">{{ stats.monthUsed || 0 }} / {{ stats.monthQuota || 0 }}</span>
          <span class="quota-label">本月已查验作品集</span>
        </div>
        <el-progress
          :percentage="quotaPercent"
          :stroke-width="8"
          :show-text="false"
          :color="quotaColor"
        />
        <p class="quota-tip">
          剩余 {{ stats.remainQuota || 0 }} 次。每次查看候选人完整档案消耗 1 次额度，
          浏览项目广场与项目详情不消耗额度。
        </p>
      </div>
    </section>

    <!-- 数据概览 -->
    <section class="stat-grid">
      <div v-for="s in statCards" :key="s.label" class="stat-card">
        <div class="stat-icon" :style="{ background: s.bg, color: s.color }">
          <el-icon :size="20"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ s.value }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
        <span v-if="s.hint" class="stat-hint">{{ s.hint }}</span>
      </div>
    </section>

    <div class="dashboard-grid">
      <!-- 推荐候选人 -->
      <section class="panel">
        <div class="panel-head">
          <h3 class="panel-title">值得关注的候选人</h3>
          <el-button text type="primary" @click="router.push('/company/talent')">
            全部人才 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div v-if="candidates.length" class="candidate-list">
          <div
            v-for="c in candidates"
            :key="c.studentId"
            class="candidate-item"
            @click="router.push(`/company/candidate/${c.studentId}`)"
          >
            <el-avatar :size="44" :src="c.avatar" class="cand-avatar">
              {{ (c.nickname || '同').slice(0, 1) }}
            </el-avatar>
            <div class="cand-info">
              <div class="cand-name">
                {{ c.nickname }}
                <el-tag v-if="c.eduVerified === 2" size="small" type="success" effect="plain">
                  学籍已认证
                </el-tag>
              </div>
              <div class="cand-meta">
                {{ c.school || '—' }}
                <template v-if="c.major"> · {{ c.major }}</template>
                <template v-if="c.degree"> · {{ c.degree }}</template>
                <template v-if="c.graduateYear"> · {{ c.graduateYear }} 届</template>
              </div>
              <div v-if="c.skillTags && c.skillTags.length" class="cand-tags">
                <span v-for="t in c.skillTags.slice(0, 5)" :key="t" class="tag-pill">{{ t }}</span>
              </div>
            </div>
            <div class="cand-right">
              <span class="cand-score">
                {{ c.avgScore > 0 ? Number(c.avgScore).toFixed(1) : '—' }}
              </span>
              <span class="cand-count">{{ c.projectCount || 0 }} 个项目</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无可检索的候选人" :image-size="70">
          <el-button
            v-if="verifyStatus === 2"
            type="primary"
            @click="router.push('/company/talent')"
          >
            前往人才检索
          </el-button>
        </el-empty>
      </section>

      <!-- 最近邀约 -->
      <section class="panel">
        <div class="panel-head">
          <h3 class="panel-title">最近的邀约</h3>
          <el-button text type="primary" @click="router.push('/company/invitations')">
            全部邀约 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div v-if="invitations.length" class="invite-list">
          <div v-for="inv in invitations" :key="inv.id" class="invite-item">
            <el-avatar :size="34" :src="inv.studentAvatar">
              {{ (inv.studentName || '同').slice(0, 1) }}
            </el-avatar>
            <div class="invite-info">
              <span class="invite-name">{{ inv.studentName }}</span>
              <span class="invite-job">{{ inv.jobTitle }}</span>
              <span class="invite-time">{{ fromNow(inv.createTime) }}</span>
            </div>
            <el-tag :type="INVITATION_STATUS[inv.status]?.type" effect="plain" size="small">
              {{ INVITATION_STATUS[inv.status]?.text }}
            </el-tag>
          </div>
        </div>
        <el-empty v-else description="还没有发出邀约" :image-size="70" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { companyApi } from '@/api'
import {
  INVITATION_STATUS,
  PACKAGE_TEXT,
  formatDate,
  fromNow
} from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const stats = ref({
  favoriteCount: 0,
  invitationCount: 0,
  pendingInvitationCount: 0,
  acceptedInvitationCount: 0,
  viewCount: 0,
  packageType: 'FREE',
  monthQuota: 0,
  monthUsed: 0,
  remainQuota: 0,
  packageExpire: null,
  verifyStatus: 0
})
const candidates = ref([])
const invitations = ref([])

/** 套餐类型 → Element Plus tag type */
const PACKAGE_TYPE = { FREE: 'info', BASIC: 'primary', PRO: 'success' }

const verifyStatus = computed(() => userStore.companyVerifyStatus)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const verifyTone = computed(
  () => ({ 0: 'none', 1: 'pending', 2: 'ok', 3: 'reject' })[verifyStatus.value] || 'none'
)

const verifyIcon = computed(
  () =>
    ({ none: 'InfoFilled', pending: 'Clock', ok: 'CircleCheckFilled', reject: 'CircleCloseFilled' })[
      verifyTone.value
    ]
)

const bannerTitle = computed(
  () =>
    ({ none: '企业资质尚未认证', pending: '企业认证审核中', reject: '认证被驳回' })[verifyTone.value] ||
    '认证状态'
)

const bannerDesc = computed(
  () =>
    ({
      none: '上传营业执照完成认证后，才能使用人才检索、收藏与邀约功能。平台仅核验资质真实性，不会对外披露证照内容。',
      pending: '平台正在核验营业执照信息，通常在 1-2 个工作日内完成。',
      reject: userStore.user?.companyProfile?.verifyRemark || '请查看审核意见，修正后重新提交。'
    })[verifyTone.value] || ''
)

const quotaPercent = computed(() => {
  if (!stats.value.monthQuota) return 0
  return Math.min(100, Math.round((stats.value.monthUsed / stats.value.monthQuota) * 100))
})

const quotaColor = computed(() =>
  quotaPercent.value >= 100 ? '#cc4b4b' : quotaPercent.value >= 80 ? '#d99a2b' : '#1f6f6b'
)

const statCards = computed(() => [
  {
    label: '收藏候选人',
    value: stats.value.favoriteCount || 0,
    icon: 'Star',
    bg: '#e6f2f1',
    color: '#1f6f6b',
    hint: '可分组管理'
  },
  {
    label: '发出邀约',
    value: stats.value.invitationCount || 0,
    icon: 'Promotion',
    bg: '#fdf0e7',
    color: '#e07a3f',
    hint: `${stats.value.pendingInvitationCount || 0} 条待回应`
  },
  {
    label: '已接受邀约',
    value: stats.value.acceptedInvitationCount || 0,
    icon: 'CircleCheck',
    bg: '#eaf5ef',
    color: '#3f9e6a',
    hint: '学生已同意沟通'
  },
  {
    label: '累计查验',
    value: stats.value.viewCount || 0,
    icon: 'View',
    bg: '#eef2f4',
    color: '#6b7b8c',
    hint: '含项目与作品集'
  }
])

const load = async () => {
  loading.value = true
  try {
    const [s, cand, inv] = await Promise.all([
      companyApi.statistics().catch(() => null),
      companyApi.searchCandidates({ onlyVerified: 1 }, { pageNum: 1, pageSize: 5 }).catch(() => null),
      companyApi.sentInvitations({ pageNum: 1, pageSize: 5 }).catch(() => null)
    ])
    if (s) stats.value = { ...stats.value, ...s }
    if (cand?.records) candidates.value = cand.records
    if (inv?.records) invitations.value = inv.records
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.company-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.verify-banner {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 24px;
  border-radius: var(--xz-radius-lg);
  border: 1px solid transparent;
}

.verify-banner .banner-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.banner-title {
  font-size: 14.5px;
  font-weight: 600;
}

.banner-desc {
  font-size: 12.5px;
  line-height: 1.7;
  margin-top: 3px;
  opacity: 0.9;
}

.verify-banner.is-none {
  background: #f4f7f9;
  border-color: #e2e8ec;
  color: var(--xz-info);
}

.verify-banner.is-pending {
  background: #fdf8ec;
  border-color: #f3e3c2;
  color: #a9761c;
}

.verify-banner.is-reject {
  background: #fdf1f1;
  border-color: #f3d2d2;
  color: var(--xz-danger);
}

/* ---------- 欢迎条 ---------- */
.welcome {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 30px;
  padding: 26px 30px;
  border-radius: var(--xz-radius-lg);
  background: linear-gradient(140deg, #f2f9f8 0%, #eaf3f2 100%);
  border: 1px solid #dcebe9;
  align-items: center;
}

.welcome-title {
  font-size: 21px;
  font-weight: 700;
  color: var(--xz-text-primary);
}

.welcome-desc {
  margin-top: 9px;
  font-size: 13.5px;
  line-height: 1.8;
  color: var(--xz-text-secondary);
  max-width: 560px;
}

.welcome-actions {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.quota-card {
  background: #ffffff;
  border-radius: var(--xz-radius);
  border: 1px solid var(--xz-border-light);
  padding: 18px 20px;
}

.quota-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.quota-expire {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

.quota-body {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 15px 0 9px;
}

.quota-num {
  font-size: 24px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.quota-label {
  font-size: 11.5px;
  color: var(--xz-text-secondary);
}

.quota-tip {
  margin-top: 11px;
  font-size: 11.5px;
  line-height: 1.65;
  color: var(--xz-text-placeholder);
}

/* ---------- 统计卡 ---------- */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 22px;
  background: #ffffff;
  border-radius: var(--xz-radius);
  border: 1px solid var(--xz-border-light);
  box-shadow: var(--xz-shadow-sm);
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--xz-text-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  margin-top: 1px;
}

.stat-hint {
  position: absolute;
  top: 12px;
  right: 14px;
  font-size: 11px;
  color: var(--xz-text-placeholder);
}

/* ---------- 面板 ---------- */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 20px;
  align-items: start;
}

.panel {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 20px 22px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--xz-text-primary);
  padding-left: 9px;
  border-left: 3px solid var(--xz-primary);
  line-height: 1.3;
}

/* ---------- 候选人 ---------- */
.candidate-list {
  display: flex;
  flex-direction: column;
}

.candidate-item {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 13px 10px;
  border-radius: var(--xz-radius-sm);
  cursor: pointer;
  transition: background 0.18s ease;
}

.candidate-item:hover {
  background: var(--xz-bg-hover);
}

.candidate-item + .candidate-item {
  border-top: 1px solid var(--xz-border-light);
}

.cand-avatar {
  background: var(--xz-primary);
  flex-shrink: 0;
}

.cand-info {
  flex: 1;
  min-width: 0;
}

.cand-name {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.cand-meta {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 3px;
}

.cand-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 6px;
}

.tag-pill {
  font-size: 11px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 1px 7px;
  border-radius: 4px;
}

.cand-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
}

.cand-score {
  font-size: 18px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.cand-count {
  font-size: 11px;
  color: var(--xz-text-placeholder);
  margin-top: 2px;
}

/* ---------- 邀约 ---------- */
.invite-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.invite-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 8px;
  border-radius: var(--xz-radius-sm);
}

.invite-item + .invite-item {
  border-top: 1px solid var(--xz-border-light);
}

.invite-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.invite-name {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.invite-job {
  font-size: 11.5px;
  color: var(--xz-text-secondary);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.invite-time {
  font-size: 11px;
  color: var(--xz-text-placeholder);
  margin-top: 2px;
}

@media (max-width: 1080px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-grid,
  .welcome {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
