<template>
  <div v-loading="loading" class="expert-dashboard">
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
        @click="router.push('/expert/profile')"
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
            完成专家认证后即可开始点评。你的每一次点评都会沉淀为可查的专业记录。
          </template>
          <template v-else-if="stats.todayReviews >= stats.dailyQuota && stats.dailyQuota > 0">
            今日点评额度已用完（{{ stats.dailyQuota }} 条），明天再来看看有什么新作品。
          </template>
          <template v-else-if="poolTotal > 0">
            求点评池里有 {{ poolTotal }} 个项目在等待点评，今天还可以点评
            {{ Math.max(0, (stats.dailyQuota || 0) - (stats.todayReviews || 0)) }} 个。
          </template>
          <template v-else>
            当前求点评池是空的。可以去项目广场逛逛，主动为优秀的作品留下点评。
          </template>
        </p>
        <div class="welcome-actions">
          <el-button
            type="primary"
            :disabled="verifyStatus !== 2"
            @click="router.push('/expert/pool')"
          >
            <el-icon><Collection /></el-icon>
            去求点评池领取
          </el-button>
          <el-button @click="router.push('/square')">
            <el-icon><Search /></el-icon>
            浏览项目广场
          </el-button>
        </div>
      </div>

      <div class="level-card">
        <div class="level-head">
          <el-tag :type="EXPERT_LEVEL_TYPE[stats.level]" effect="dark" size="large">
            {{ EXPERT_LEVEL_TEXT[stats.level] || '青铜点评人' }}
          </el-tag>
          <span class="level-points">{{ stats.points || 0 }} 积分</span>
        </div>
        <div class="level-progress">
          <div class="progress-info">
            <span>距离下一等级还需 {{ nextLevelNeed }} 条点评</span>
            <span>{{ stats.totalReviews || 0 }} / {{ nextLevelTarget }}</span>
          </div>
          <el-progress
            :percentage="levelPercent"
            :stroke-width="8"
            :show-text="false"
            color="var(--xz-primary)"
          />
        </div>
        <p class="level-tip">{{ levelTip }}</p>
      </div>
    </section>

    <!-- 数据卡 -->
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
      <!-- 待点评 -->
      <section class="panel">
        <div class="panel-head">
          <h3 class="panel-title">求点评池 · 待领取</h3>
          <el-button text type="primary" @click="router.push('/expert/pool')">
            全部 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div v-if="poolProjects.length" class="pool-list">
          <div
            v-for="p in poolProjects"
            :key="p.id"
            class="pool-item"
            @click="router.push(`/project/${p.id}`)"
          >
            <div class="pool-cover" :style="coverStyle(p)">
              <img v-if="p.coverUrl" :src="p.coverUrl" :alt="p.name" />
              <el-icon v-else :size="18"><Files /></el-icon>
            </div>
            <div class="pool-info">
              <span class="pool-name">{{ p.name }}</span>
              <span class="pool-meta">
                {{ CATEGORY_TEXT[p.category] || '未分类' }}
                <span class="dot">·</span>
                {{ PROJECT_TYPE_TEXT[p.projectType] || '其他' }}
                <span class="dot">·</span>
                {{ p.studentSchool || '—' }}
              </span>
              <div v-if="techListOf(p).length" class="pool-tags">
                <span v-for="t in techListOf(p).slice(0, 4)" :key="t" class="tech-tag">{{ t }}</span>
              </div>
            </div>
            <div class="pool-right">
              <el-tag
                v-if="!p.expertReviewCount"
                size="small"
                type="warning"
                effect="plain"
                class="new-tag"
              >
                首发点评
              </el-tag>
              <span class="pool-count">{{ p.expertReviewCount || 0 }} 条点评</span>
              <el-button
                v-if="verifyStatus === 2"
                size="small"
                type="primary"
                @click.stop="router.push(`/expert/review/${p.id}`)"
              >
                撰写点评
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="求点评池暂时是空的" :image-size="70" />
      </section>

      <!-- 最近点评 -->
      <section class="panel">
        <div class="panel-head">
          <h3 class="panel-title">我最近的点评</h3>
          <el-button text type="primary" @click="router.push('/expert/history')">
            全部记录 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div v-if="recentReviews.length" class="review-list">
          <div
            v-for="r in recentReviews"
            :key="r.id"
            class="review-item"
            @click="router.push(`/project/${r.projectId}`)"
          >
            <div class="review-head">
              <span class="review-project">{{ r.projectName }}</span>
              <span class="review-score">{{ Number(r.avgScore || 0).toFixed(1) }}</span>
            </div>
            <div class="review-meta">
              <el-tag
                v-if="r.thanksFlag === 1"
                size="small"
                type="success"
                effect="plain"
              >
                已获致谢
              </el-tag>
              <el-tag
                v-if="r.qualityStatus === 1"
                size="small"
                type="warning"
                effect="plain"
              >
                嫌疑敷衍
              </el-tag>
              <span class="review-time">{{ fromNow(r.createTime) }}</span>
            </div>
            <p class="review-text">{{ r.comment }}</p>
          </div>
        </div>
        <el-empty v-else description="还没有点评记录" :image-size="70">
          <el-button
            v-if="verifyStatus === 2"
            type="primary"
            @click="router.push('/expert/pool')"
          >
            领取第一个项目
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
import {
  CATEGORY_TEXT,
  EXPERT_LEVEL_TEXT,
  EXPERT_LEVEL_TYPE,
  PROJECT_TYPE_TEXT,
  fromNow
} from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const stats = ref({
  todayReviews: 0,
  dailyQuota: 0,
  totalReviews: 0,
  avgGivenScore: 0,
  thanksCount: 0,
  level: 'BRONZE',
  points: 0
})
const poolProjects = ref([])
const poolTotal = ref(0)
const recentReviews = ref([])

const verifyStatus = computed(() => userStore.expertVerifyStatus)

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
    ({ none: '尚未提交专家认证', pending: '认证审核中', reject: '认证被驳回' })[verifyTone.value] ||
    '认证状态'
)

const bannerDesc = computed(
  () =>
    ({
      none: '认证通过后才能领取求点评池的项目并撰写点评。你可以是高校教师，也可以是有项目经验的企业工程师。',
      pending: '平台正在核对你的资质材料，通常在 1-2 个工作日内完成。',
      reject: userStore.user?.expertProfile?.verifyRemark || '请查看审核意见，补充材料后重新提交。'
    })[verifyTone.value] || ''
)

const levelTargets = { BRONZE: 20, SILVER: 50, GOLD: 50 }

const nextLevelTarget = computed(() => levelTargets[stats.value.level] || 20)

const nextLevelNeed = computed(() =>
  Math.max(0, nextLevelTarget.value - (stats.value.totalReviews || 0))
)

const levelPercent = computed(() => {
  const total = stats.value.totalReviews || 0
  const target = nextLevelTarget.value || 1
  if (stats.value.level === 'GOLD') return 100
  return Math.min(100, Math.round((total / target) * 100))
})

const levelTip = computed(
  () =>
    ({
      BRONZE: '累计 20 条有效点评可升级为「白银点评人」，获得求点评池优先推荐。',
      SILVER: '累计 50 条有效点评可升级为「金牌点评人」，点评会优先展示在项目详情页顶部。',
      GOLD: '你已是金牌点评人，感谢你为学生的成长持续投入。'
    })[stats.value.level] || ''
)

const statCards = computed(() => [
  {
    label: '今日已点评',
    value: `${stats.value.todayReviews || 0} / ${stats.value.dailyQuota || 0}`,
    icon: 'EditPen',
    bg: '#e6f2f1',
    color: '#1f6f6b',
    hint: '每日额度'
  },
  {
    label: '累计点评',
    value: stats.value.totalReviews || 0,
    icon: 'Document',
    bg: '#eef2f4',
    color: '#6b7b8c',
    hint: `求点评池 ${poolTotal.value} 个待领`
  },
  {
    label: '给出的均分',
    value: stats.value.avgGivenScore > 0 ? Number(stats.value.avgGivenScore).toFixed(1) : '—',
    icon: 'DataLine',
    bg: '#fdf0e7',
    color: '#e07a3f',
    hint: '反映你的评分尺度'
  },
  {
    label: '收到致谢',
    value: stats.value.thanksCount || 0,
    icon: 'Star',
    bg: '#f3eef7',
    color: '#8a63a8',
    hint: '学生自发致谢'
  }
])

const coverStyle = (p) => {
  const palette = ['#e6f2f1', '#fdf0e7', '#eef2f4', '#f3eef7']
  return { background: palette[(p.id || 0) % palette.length] }
}

const techListOf = (p) => {
  const t = p.techStack
  if (Array.isArray(t)) return t
  if (typeof t === 'string' && t) return t.split(',').map((s) => s.trim()).filter(Boolean)
  return []
}

const load = async () => {
  loading.value = true
  try {
    const tasks = [
      reviewApi.statistics().catch(() => null),
      projectApi.reviewPool({ pageNum: 1, pageSize: 5 }).catch(() => null)
    ]
    if (verifyStatus.value === 2) {
      tasks.push(reviewApi.my({ pageNum: 1, pageSize: 4 }).catch(() => null))
    }
    const [s, pool, mine] = await Promise.all(tasks)
    if (s) stats.value = { ...stats.value, ...s }
    if (pool?.records) {
      poolProjects.value = pool.records
      poolTotal.value = pool.total || 0
    }
    if (mine?.records) recentReviews.value = mine.records
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.expert-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ---------- 认证提示 ---------- */
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

.level-card {
  background: #ffffff;
  border-radius: var(--xz-radius);
  border: 1px solid var(--xz-border-light);
  padding: 18px 20px;
}

.level-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.level-points {
  font-size: 13px;
  font-weight: 600;
  color: var(--xz-accent);
  font-variant-numeric: tabular-nums;
}

.level-progress {
  margin-top: 16px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--xz-text-secondary);
  margin-bottom: 7px;
}

.level-tip {
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
  font-size: 21px;
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

/* ---------- 求点评池 ---------- */
.pool-list {
  display: flex;
  flex-direction: column;
}

.pool-item {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 13px 10px;
  border-radius: var(--xz-radius-sm);
  cursor: pointer;
  transition: background 0.18s ease;
}

.pool-item:hover {
  background: var(--xz-bg-hover);
}

.pool-item + .pool-item {
  border-top: 1px solid var(--xz-border-light);
}

.pool-cover {
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

.pool-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pool-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.pool-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pool-meta {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 3px;
}

.pool-tags {
  display: flex;
  gap: 5px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.tech-tag {
  font-size: 11px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 1px 7px;
  border-radius: 4px;
}

.pool-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.new-tag {
  font-size: 11px;
}

.pool-count {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

/* ---------- 最近点评 ---------- */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-item {
  padding: 13px 15px;
  border-radius: var(--xz-radius-sm);
  background: #fbfcfc;
  border: 1px solid var(--xz-border-light);
  cursor: pointer;
  transition: border-color 0.18s ease;
}

.review-item:hover {
  border-color: #cfe3e2;
}

.review-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.review-project {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-score {
  font-size: 16px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 7px;
}

.review-time {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

.review-text {
  margin-top: 8px;
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--xz-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dot {
  opacity: 0.6;
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
