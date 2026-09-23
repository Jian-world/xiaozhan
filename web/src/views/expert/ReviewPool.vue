<template>
  <div class="review-pool-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">求点评池</h2>
        <p class="page-sub">
          学生主动投递的项目优先展示「未被点评过」的作品。领取后请在项目中留下逐项评分与具体建议。
        </p>
      </div>
      <div class="quota-chip" :class="{ 'is-exhausted': remainQuota <= 0 }">
        <el-icon><EditPen /></el-icon>
        <div class="chip-body">
          <span class="chip-value">{{ todayReviews }} / {{ dailyQuota }}</span>
          <span class="chip-label">今日点评额度</span>
        </div>
      </div>
    </div>

    <el-alert
      v-if="verifyStatus !== 2"
      type="warning"
      :closable="false"
      show-icon
      class="verify-alert"
    >
      <template #title>尚未通过专家认证，无法撰写点评</template>
      <template #default>
        <span class="alert-body">
          认证通过后才能领取项目并提交点评。
          <el-button link type="primary" @click="router.push('/expert/profile')">
            前往专家档案完成认证 →
          </el-button>
        </span>
      </template>
    </el-alert>

    <div class="filter-bar">
      <el-radio-group v-model="query.category" @change="reload">
        <el-radio-button :value="''">全部方向</el-radio-button>
        <el-radio-button v-for="c in CATEGORIES" :key="c.value" :value="c.value">
          {{ c.label }}
        </el-radio-button>
      </el-radio-group>
      <span class="result-count">共 {{ total }} 个项目等待点评</span>
    </div>

    <div v-loading="loading" class="pool-body">
      <div v-if="projects.length" class="pool-grid">
        <article
          v-for="p in projects"
          :key="p.id"
          class="pool-card"
          @click="router.push(`/project/${p.id}`)"
        >
          <div class="card-cover" :style="coverStyle(p)">
            <img v-if="p.coverUrl" :src="p.coverUrl" :alt="p.name" />
            <div v-else class="cover-placeholder">
              <el-icon :size="30"><Files /></el-icon>
              <span>{{ CATEGORY_TEXT[p.category] || '项目' }}</span>
            </div>
            <span v-if="!p.expertReviewCount" class="badge-new">首发点评</span>
          </div>

          <div class="card-body">
            <h3 class="card-title" :title="p.name">{{ p.name }}</h3>
            <p class="card-summary">{{ p.summary || '作者暂未填写简介' }}</p>

            <div class="card-meta">
              <span>{{ PROJECT_TYPE_TEXT[p.projectType] || '其他' }}</span>
              <span class="dot">·</span>
              <span>{{ p.studentSchool || '—' }}</span>
              <template v-if="p.studentMajor">
                <span class="dot">·</span>
                <span>{{ p.studentMajor }}</span>
              </template>
            </div>

            <div v-if="techListOf(p).length" class="card-tags">
              <span v-for="t in techListOf(p).slice(0, 4)" :key="t" class="tech-tag">{{ t }}</span>
              <span v-if="techListOf(p).length > 4" class="tech-tag tech-tag-more">
                +{{ techListOf(p).length - 4 }}
              </span>
            </div>

            <div class="card-assets">
              <span v-for="g in assetSummary(p)" :key="g.type" class="asset-chip">
                <el-icon><component :is="ASSET_TYPE_ICON[g.type]" /></el-icon>
                {{ ASSET_TYPE_TEXT[g.type] }} {{ g.count }}
              </span>
            </div>

            <div class="card-footer">
              <div class="author">
                <el-avatar :size="24" :src="p.studentAvatar">
                  {{ (p.studentName || '同').slice(0, 1) }}
                </el-avatar>
                <span class="author-name">{{ p.studentName || '匿名同学' }}</span>
              </div>
              <el-button
                type="primary"
                size="small"
                :disabled="verifyStatus !== 2 || remainQuota <= 0"
                @click.stop="router.push(`/expert/review/${p.id}`)"
              >
                {{ p.expertReviewCount ? '补充点评' : '撰写点评' }}
              </el-button>
            </div>
          </div>
        </article>
      </div>

      <el-empty
        v-else-if="!loading"
        description="当前筛选条件下没有待点评的项目"
      >
        <el-button @click="router.push('/square')">去项目广场看看</el-button>
      </el-empty>
    </div>

    <div v-if="total > query.pageSize" class="pagination-wrap">
      <el-pagination
        v-model:current-page="query.pageNum"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        background
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { projectApi, reviewApi } from '@/api'
import {
  ASSET_TYPE_ICON,
  ASSET_TYPE_TEXT,
  CATEGORIES,
  CATEGORY_TEXT,
  PROJECT_TYPE_TEXT
} from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const projects = ref([])
const total = ref(0)
const todayReviews = ref(0)
const dailyQuota = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 9,
  category: ''
})

const verifyStatus = computed(() => userStore.expertVerifyStatus)
const remainQuota = computed(() => Math.max(0, (dailyQuota.value || 0) - (todayReviews.value || 0)))

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

const assetSummary = (p) => {
  const assets = p.assets || []
  return ['SOURCE', 'DOC', 'VIDEO']
    .map((type) => ({ type, count: assets.filter((a) => a.assetType === type).length }))
    .filter((g) => g.count > 0)
}

const load = async () => {
  loading.value = true
  try {
    const data = await projectApi.reviewPool({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      category: query.category || undefined
    })
    projects.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    projects.value = []
  } finally {
    loading.value = false
  }
}

const loadQuota = async () => {
  if (verifyStatus.value !== 2) return
  try {
    const s = await reviewApi.statistics()
    todayReviews.value = s.todayReviews || 0
    dailyQuota.value = s.dailyQuota || 0
  } catch (e) {
    /* 忽略 */
  }
}

const reload = () => {
  query.pageNum = 1
  load()
}

onMounted(() => {
  load()
  loadQuota()
})
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
  max-width: 640px;
}

.quota-chip {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 12px 20px;
  border-radius: var(--xz-radius);
  background: var(--xz-primary-lighter);
  color: var(--xz-primary);
  border: 1px solid #cfe3e2;
}

.quota-chip.is-exhausted {
  background: #fdf1f1;
  color: var(--xz-danger);
  border-color: #f3d2d2;
}

.chip-body {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.chip-value {
  font-size: 17px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.chip-label {
  font-size: 11px;
  opacity: 0.85;
}

.verify-alert {
  margin-bottom: 18px;
  border-radius: var(--xz-radius);
}

.alert-body {
  font-size: 12.5px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.result-count {
  font-size: 13px;
  color: var(--xz-text-secondary);
  flex-shrink: 0;
}

.pool-body {
  min-height: 240px;
}

.pool-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.pool-card {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.pool-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--xz-shadow);
  border-color: #d5e3e2;
}

.card-cover {
  position: relative;
  height: 128px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--xz-primary);
  opacity: 0.7;
  font-size: 12px;
}

.badge-new {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 9px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 600;
  background: rgba(224, 122, 63, 0.92);
  color: #fff;
  backdrop-filter: blur(4px);
}

.card-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-summary {
  margin-top: 6px;
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  line-height: 1.55;
  height: 39px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 9px;
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  flex-wrap: wrap;
}

.dot {
  opacity: 0.6;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 9px;
}

.tech-tag {
  font-size: 11px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 2px 8px;
  border-radius: 4px;
}

.tech-tag-more {
  background: #eef2f4;
  color: var(--xz-text-secondary);
}

.card-assets {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.asset-chip {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--xz-text-secondary);
  background: #f7fafa;
  border: 1px solid var(--xz-border-light);
  padding: 2px 8px;
  border-radius: 4px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
  padding-top: 12px;
  margin-top: 12px;
  border-top: 1px solid var(--xz-border-light);
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.author-name {
  font-size: 12px;
  color: var(--xz-text-secondary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-wrap {
  margin-top: 26px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1200px) {
  .pool-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 700px) {
  .pool-grid {
    grid-template-columns: 1fr;
  }
}
</style>
