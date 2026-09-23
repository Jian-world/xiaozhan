<template>
  <div v-loading="loading" class="student-dashboard">
    <!-- 欢迎条 -->
    <section class="welcome">
      <div class="welcome-copy">
        <h2 class="welcome-title">
          {{ greeting }}，{{ userStore.displayName }}
        </h2>
        <p class="welcome-desc">
          <template v-if="stats.draftProjects > 0">
            你有 {{ stats.draftProjects }} 个草稿项目还没发布，发布后才能进入项目广场获得曝光。
          </template>
          <template v-else-if="stats.inPoolCount > 0">
            有 {{ stats.inPoolCount }} 个项目正在求点评池等待点评人领取，耐心等待一下。
          </template>
          <template v-else-if="stats.totalReviews > 0">
            已累计收到 {{ stats.totalReviews }} 条专家点评，去看看有什么可以改进的地方。
          </template>
          <template v-else>
            还没有上传项目？从一个课程设计开始，把它的源码与文档整理进作品集吧。
          </template>
        </p>
        <div class="welcome-actions">
          <el-button type="primary" @click="router.push('/student/projects/edit')">
            <el-icon><Plus /></el-icon>
            新建项目
          </el-button>
          <el-button
            v-if="stats.draftProjects > 0"
            @click="router.push('/student/projects?status=0')"
          >
            处理草稿
          </el-button>
        </div>
      </div>

      <div class="verify-card" :class="`is-${verifyTone}`">
        <el-icon :size="24"><component :is="verifyIcon" /></el-icon>
        <div class="verify-body">
          <span class="verify-title">{{ verifyTitle }}</span>
          <span class="verify-desc">{{ verifyDesc }}</span>
        </div>
        <el-button
          v-if="verifyTone !== 'ok' && verifyTone !== 'pending'"
          size="small"
          type="primary"
          plain
          @click="router.push('/student/profile')"
        >
          去认证
        </el-button>
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
      <!-- 最近项目 -->
      <section class="panel">
        <div class="panel-head">
          <h3 class="panel-title">最近编辑的项目</h3>
          <el-button text type="primary" @click="router.push('/student/projects')">
            全部项目 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div v-if="recentProjects.length" class="recent-list">
          <div
            v-for="p in recentProjects"
            :key="p.id"
            class="recent-item"
            @click="router.push(`/student/projects/edit/${p.id}`)"
          >
            <div class="recent-cover" :style="coverStyle(p)">
              <img v-if="p.coverUrl" :src="p.coverUrl" :alt="p.name" />
              <el-icon v-else :size="20"><Files /></el-icon>
            </div>
            <div class="recent-info">
              <div class="recent-name">
                {{ p.name }}
                <el-tag size="small" :type="REVIEW_PUBLISH_STATUS[p.publishStatus]?.type" effect="plain">
                  {{ REVIEW_PUBLISH_STATUS[p.publishStatus]?.text }}
                </el-tag>
                <el-tag
                  v-if="p.publishStatus === 1 && p.reviewStatus === 2"
                  size="small"
                  type="danger"
                  effect="plain"
                >
                  审核驳回
                </el-tag>
              </div>
              <div class="recent-meta">
                {{ CATEGORY_TEXT[p.category] || '未分类' }}
                <span class="dot">·</span>
                {{ p.expertReviewCount || 0 }} 条点评
                <span class="dot">·</span>
                {{ p.viewCount || 0 }} 次浏览
                <span class="dot">·</span>
                更新于 {{ fromNow(p.updateTime) }}
              </div>
            </div>
            <div class="recent-score">
              {{ p.expertAvgScore > 0 ? Number(p.expertAvgScore).toFixed(1) : '—' }}
            </div>
          </div>
        </div>
        <el-empty v-else description="还没有项目" :image-size="70">
          <el-button type="primary" @click="router.push('/student/projects/edit')">
            创建第一个项目
          </el-button>
        </el-empty>
      </section>

      <!-- 最新点评 -->
      <section class="panel">
        <div class="panel-head">
          <h3 class="panel-title">最新收到的点评</h3>
          <el-button text type="primary" @click="router.push('/student/reviews')">
            全部点评 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div v-if="recentReviews.length" class="review-list">
          <div v-for="r in recentReviews" :key="r.id" class="review-item">
            <div class="review-head">
              <el-avatar :size="32" :src="r.expertAvatar">
                {{ (r.expertName || '师').slice(0, 1) }}
              </el-avatar>
              <div class="review-who">
                <span class="review-name">{{ r.expertName }}</span>
                <span class="review-meta">
                  {{ r.projectName }} · {{ fromNow(r.createTime) }}
                </span>
              </div>
              <span class="review-score">{{ Number(r.avgScore || 0).toFixed(1) }}</span>
            </div>
            <p class="review-text">{{ r.comment }}</p>
          </div>
        </div>
        <el-empty v-else description="还没有收到点评" :image-size="70">
          <el-button
            v-if="stats.publishedProjects > 0"
            type="primary"
            plain
            @click="router.push('/student/projects')"
          >
            把项目投进求点评池
          </el-button>
        </el-empty>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { projectApi, reviewApi } from '@/api'
import { CATEGORY_TEXT, REVIEW_PUBLISH_STATUS, fromNow } from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const stats = ref({
  totalProjects: 0,
  publishedProjects: 0,
  draftProjects: 0,
  totalViews: 0,
  totalReviews: 0,
  inPoolCount: 0,
  portfolioViews: 0,
  eduVerified: 0
})
const recentProjects = ref([])
const recentReviews = ref([])

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const statCards = computed(() => [
  {
    label: '项目总数',
    value: stats.value.totalProjects,
    icon: 'FolderOpened',
    bg: '#e6f2f1',
    color: '#1f6f6b',
    hint: `${stats.value.publishedProjects} 已发布`
  },
  {
    label: '作品集浏览',
    value: stats.value.portfolioViews,
    icon: 'View',
    bg: '#eef2f4',
    color: '#6b7b8c',
    hint: '企业查验计入'
  },
  {
    label: '获得点评',
    value: stats.value.totalReviews,
    icon: 'ChatDotSquare',
    bg: '#fdf0e7',
    color: '#e07a3f',
    hint: `${stats.value.inPoolCount} 个在求点评`
  },
  {
    label: '项目总浏览',
    value: stats.value.totalViews,
    icon: 'DataLine',
    bg: '#f3eef7',
    color: '#8a63a8',
    hint: '含广场与分享'
  }
])

const verifyTone = computed(
  () => ({ 0: 'none', 1: 'pending', 2: 'ok', 3: 'reject' })[stats.value.eduVerified] || 'none'
)

const verifyIcon = computed(
  () =>
    ({ none: 'InfoFilled', pending: 'Clock', ok: 'CircleCheckFilled', reject: 'CircleCloseFilled' })[
      verifyTone.value
    ]
)

const verifyTitle = computed(() => '学籍认证状态')
const verifyDesc = computed(
  () =>
    ({
      none: '未提交认证，认证后作品集会显示「已认证」标识，企业更信任',
      pending: '认证材料已提交，平台正在审核中',
      ok: '已通过学籍认证，作品集已展示认证标识',
      reject: '认证被驳回，请查看审核意见后重新提交'
    })[verifyTone.value]
)

const coverStyle = (p) => {
  const palette = ['#e6f2f1', '#fdf0e7', '#eef2f4', '#f3eef7']
  return { background: palette[(p.id || 0) % palette.length] }
}

const load = async () => {
  loading.value = true
  try {
    const [s, projects, reviews] = await Promise.all([
      projectApi.statistics().catch(() => stats.value),
      projectApi.my({ pageNum: 1, pageSize: 5 }).catch(() => null),
      reviewApi.received({ pageNum: 1, pageSize: 4 }).catch(() => null)
    ])
    if (s) stats.value = { ...stats.value, ...s }
    if (projects?.records) recentProjects.value = projects.records
    if (reviews?.records) recentReviews.value = reviews.records
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.student-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ---------- 欢迎条 ---------- */
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
  padding: 26px 30px;
  border-radius: var(--xz-radius-lg);
  background: linear-gradient(140deg, #f2f9f8 0%, #eaf3f2 100%);
  border: 1px solid #dcebe9;
  flex-wrap: wrap;
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
  max-width: 620px;
}

.welcome-actions {
  margin-top: 18px;
  display: flex;
  gap: 10px;
}

.verify-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-radius: var(--xz-radius);
  background: #ffffff;
  border: 1px solid var(--xz-border-light);
  min-width: 320px;
  flex: 0 1 380px;
}

.verify-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.verify-title {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.verify-desc {
  font-size: 12px;
  line-height: 1.6;
  color: var(--xz-text-secondary);
  margin-top: 2px;
}

.verify-card.is-none .el-icon {
  color: var(--xz-info);
}

.verify-card.is-pending .el-icon {
  color: var(--xz-warning);
}

.verify-card.is-ok .el-icon {
  color: var(--xz-success);
}

.verify-card.is-reject .el-icon {
  color: var(--xz-danger);
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

/* ---------- 最近项目 ---------- */
.recent-list {
  display: flex;
  flex-direction: column;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 13px 10px;
  border-radius: var(--xz-radius-sm);
  cursor: pointer;
  transition: background 0.18s ease;
}

.recent-item:hover {
  background: var(--xz-bg-hover);
}

.recent-item + .recent-item {
  border-top: 1px solid var(--xz-border-light);
}

.recent-cover {
  width: 56px;
  height: 44px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--xz-primary);
  overflow: hidden;
  flex-shrink: 0;
}

.recent-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recent-info {
  flex: 1;
  min-width: 0;
}

.recent-name {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 4px;
}

.dot {
  opacity: 0.6;
}

.recent-score {
  font-size: 17px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

/* ---------- 最新点评 ---------- */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-item {
  padding: 14px 15px;
  border-radius: var(--xz-radius-sm);
  background: #fbfcfc;
  border: 1px solid var(--xz-border-light);
}

.review-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-who {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.review-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.review-meta {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-score {
  font-size: 17px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
}

.review-text {
  margin-top: 9px;
  font-size: 12.5px;
  line-height: 1.8;
  color: var(--xz-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@media (max-width: 1080px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .verify-card {
    flex: 1 1 100%;
  }
}

@media (max-width: 600px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }

  .welcome {
    padding: 20px 20px;
  }
}
</style>
