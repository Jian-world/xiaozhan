<template>
  <div v-loading="loading" class="portfolio-page">
    <template v-if="profile">
      <div class="portfolio-hero">
        <div class="xz-container">
          <div class="hero-card">
            <el-avatar :size="76" :src="profile.avatar" class="hero-avatar">
              {{ (profile.nickname || '同').slice(0, 1) }}
            </el-avatar>

            <div class="hero-info">
              <h1 class="hero-name">
                {{ profile.nickname || '匿名同学' }}
                <el-tooltip v-if="profile.eduVerified === 2" content="已通过学籍认证">
                  <el-tag type="success" effect="plain" size="small">
                    <el-icon><CircleCheckFilled /></el-icon>
                    学籍已认证
                  </el-tag>
                </el-tooltip>
                <el-tag v-else-if="profile.eduVerified === 1" type="warning" effect="plain" size="small">
                  认证审核中
                </el-tag>
              </h1>

              <p class="hero-school">
                <el-icon><School /></el-icon>
                {{ profile.school || '未填写学校' }}
                <template v-if="profile.major"> · {{ profile.major }}</template>
                <template v-if="profile.degree"> · {{ profile.degree }}</template>
                <template v-if="profile.graduateYear"> · {{ profile.graduateYear }} 届</template>
              </p>

              <p v-if="profile.bio" class="hero-bio">{{ profile.bio }}</p>

              <div v-if="skillTags.length" class="hero-skills">
                <span v-for="t in skillTags" :key="t" class="skill-pill">{{ t }}</span>
              </div>
            </div>

            <div class="hero-stats">
              <div class="stat">
                <span class="stat-value">{{ projects.length }}</span>
                <span class="stat-label">公开项目</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ totalReviews }}</span>
                <span class="stat-label">获得点评</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ avgScore }}</span>
                <span class="stat-label">平均评分</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ profile.portfolioViews || 0 }}</span>
                <span class="stat-label">作品集浏览</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="xz-container portfolio-body">
        <main class="portfolio-main">
          <section class="block">
            <div class="block-head">
              <h2 class="xz-section-title">项目作品</h2>
              <span class="xz-text-muted xz-text-small">共 {{ projects.length }} 个公开项目</span>
            </div>

            <div v-if="projects.length" class="card-grid">
              <ProjectCard v-for="p in projects" :key="p.id" :project="p" />
            </div>
            <el-empty v-else description="该同学还没有公开的项目作品" />
          </section>

          <section class="block">
            <div class="block-head">
              <h2 class="xz-section-title">公开点评摘要</h2>
              <span class="xz-text-muted xz-text-small">共 {{ reviews.length }} 条</span>
            </div>

            <div v-if="reviews.length" class="review-brief-list">
              <article v-for="r in reviews" :key="r.id" class="review-brief">
                <header class="brief-head">
                  <el-avatar :size="34" :src="r.expertAvatar">
                    {{ (r.expertName || '师').slice(0, 1) }}
                  </el-avatar>
                  <div class="brief-who">
                    <span class="brief-name">{{ r.expertName }}</span>
                    <span class="brief-meta">
                      {{ r.expertOrg || EXPERT_TYPE_TEXT[r.expertType] || '认证点评人' }}
                      · {{ fromNow(r.createTime) }}
                    </span>
                  </div>
                  <div class="brief-score">{{ Number(r.avgScore || 0).toFixed(1) }}</div>
                </header>
                <p class="brief-project">
                  <el-icon><FolderOpened /></el-icon>
                  <router-link :to="`/project/${r.projectId}`" class="brief-link">
                    {{ r.projectName }}
                  </router-link>
                </p>
                <p class="brief-comment">{{ r.comment }}</p>
                <router-link :to="`/project/${r.projectId}`" class="brief-more">
                  查看完整点评 →
                </router-link>
              </article>
            </div>
            <el-empty v-else description="暂无公开点评" :image-size="80" />
          </section>
        </main>

        <aside class="portfolio-side">
          <div class="side-card">
            <h3 class="side-title">能力画像</h3>
            <div class="radar-wrap">
              <div ref="radarRef" class="radar-chart"></div>
            </div>
            <p v-if="!dimensionAvg.length" class="radar-empty">积累点评后即可生成能力雷达图</p>
          </div>

          <div class="side-card">
            <h3 class="side-title">技术栈分布</h3>
            <div v-if="techStats.length" class="tech-stats">
              <div v-for="t in techStats" :key="t.name" class="tech-stat">
                <span class="tech-name">{{ t.name }}</span>
                <div class="tech-bar-wrap">
                  <div class="tech-bar" :style="{ width: `${t.percent}%` }"></div>
                </div>
                <span class="tech-count">{{ t.count }}</span>
              </div>
            </div>
            <p v-else class="radar-empty">暂无技术栈数据</p>
          </div>
        </aside>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="未找到该学生的作品集" />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import ProjectCard from '@/components/ProjectCard.vue'
import request from '@/api/request'
import { projectApi, commonApi } from '@/api'
import { EXPERT_TYPE_TEXT, SCORE_DIMENSIONS, fromNow } from '@/utils/dict'

const route = useRoute()

const loading = ref(true)
const profile = ref(null)
const projects = ref([])
const reviews = ref([])
const radarRef = ref(null)
let radarChart = null

const skillTags = computed(() => profile.value?.skillTags || [])

const totalReviews = computed(() => projects.value.reduce((sum, p) => sum + (p.expertReviewCount || 0), 0))

const avgScore = computed(() => {
  const scores = projects.value
    .map((p) => Number(p.expertAvgScore || 0))
    .filter((s) => s > 0)
  if (!scores.length) return '—'
  return (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
})

/** 从公开点评聚合四维平均分 */
const dimensionAvg = computed(() => {
  if (!reviews.value.length) return []
  const sum = { scoreCompletion: 0, scoreNormative: 0, scoreInnovation: 0, scoreTechnical: 0 }
  reviews.value.forEach((r) => {
    SCORE_DIMENSIONS.forEach((d) => {
      sum[d.key] += r[d.key] || 0
    })
  })
  return SCORE_DIMENSIONS.map((d) => ({
    ...d,
    value: Math.round((sum[d.key] / reviews.value.length) * 10) / 10
  }))
})

/** 技术栈词频统计 */
const techStats = computed(() => {
  const counter = {}
  projects.value.forEach((p) => {
    const t = p.techStack
    const list = Array.isArray(t) ? t : typeof t === 'string' ? t.split(',') : []
    list.map((s) => s.trim()).filter(Boolean).forEach((tag) => {
      counter[tag] = (counter[tag] || 0) + 1
    })
  })
  const entries = Object.entries(counter).sort((a, b) => b[1] - a[1]).slice(0, 8)
  const max = entries.length ? entries[0][1] : 1
  return entries.map(([name, count]) => ({ name, count, percent: Math.round((count / max) * 100) }))
})

const load = async () => {
  loading.value = true
  const studentId = route.params.studentId
  try {
    const [list, userInfo] = await Promise.all([
      projectApi.portfolio(studentId),
      commonApi.studentProfile(studentId).catch(() => null)
    ])
    projects.value = list || []
    profile.value = userInfo

    // 汇总该项目集合下的公开点评
    const allReviews = []
    for (const p of projects.value) {
      try {
        const rs = await request.get(`/review/project/${p.id}`)
        if (rs?.length) allReviews.push(...rs)
      } catch (e) {
        /* 忽略单个项目点评拉取失败 */
      }
    }
    reviews.value = allReviews.sort(
      (a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0)
    )
  } catch (e) {
    profile.value = null
  } finally {
    loading.value = false
    await nextTick()
    renderRadar()
  }
}

const renderRadar = () => {
  if (!radarRef.value || !dimensionAvg.value.length) return
  if (!radarChart) {
    radarChart = echarts.init(radarRef.value)
  }
  radarChart.setOption({
    radar: {
      indicator: dimensionAvg.value.map((d) => ({ name: d.label, max: 5 })),
      radius: '64%',
      splitNumber: 5,
      axisName: { color: '#5b6b78', fontSize: 11 },
      splitLine: { lineStyle: { color: '#e2e8ec' } },
      splitArea: { areaStyle: { color: ['#ffffff', '#f8fbfb'] } },
      axisLine: { lineStyle: { color: '#e2e8ec' } }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: dimensionAvg.value.map((d) => d.value),
            name: '能力评分',
            areaStyle: { color: 'rgba(31, 111, 107, 0.22)' },
            lineStyle: { color: '#1f6f6b', width: 2 },
            itemStyle: { color: '#1f6f6b' }
          }
        ]
      }
    ]
  })
  radarChart.resize()
}

watch(() => route.params.studentId, load)
onMounted(load)
</script>

<style scoped>
.portfolio-page {
  min-height: 60vh;
}

.portfolio-hero {
  padding: 30px 0 34px;
  background: linear-gradient(168deg, #f7fbfa 0%, #eef6f5 100%);
  border-bottom: 1px solid var(--xz-border-light);
}

.hero-card {
  display: flex;
  align-items: flex-start;
  gap: 26px;
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  box-shadow: var(--xz-shadow-sm);
  padding: 28px 32px;
  flex-wrap: wrap;
}

.hero-avatar {
  background: var(--xz-primary);
  font-size: 28px;
  flex-shrink: 0;
}

.hero-info {
  flex: 1;
  min-width: 260px;
}

.hero-name {
  font-size: 23px;
  font-weight: 700;
  color: var(--xz-text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-school {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 13.5px;
  color: var(--xz-text-secondary);
}

.hero-bio {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--xz-text-secondary);
  max-width: 620px;
}

.hero-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 14px;
}

.skill-pill {
  font-size: 12px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 3px 11px;
  border-radius: 20px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px 34px;
  padding-left: 30px;
  border-left: 1px solid var(--xz-border-light);
  flex-shrink: 0;
}

.stat {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 12px;
  color: var(--xz-text-secondary);
  margin-top: 2px;
}

/* ---------- 主体 ---------- */
.portfolio-body {
  display: grid;
  grid-template-columns: 1fr 306px;
  gap: 30px;
  padding-top: 30px;
  align-items: start;
}

.block {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 24px 26px;
  margin-bottom: 22px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

/* ---------- 点评摘要 ---------- */
.review-brief-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-brief {
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius);
  padding: 17px 19px;
  background: #fdfefe;
}

.brief-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brief-who {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.brief-name {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.brief-meta {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 1px;
}

.brief-score {
  font-size: 19px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
}

.brief-project {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.brief-link {
  color: var(--xz-primary);
  font-weight: 500;
}

.brief-link:hover {
  text-decoration: underline;
}

.brief-comment {
  margin-top: 9px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--xz-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.brief-more {
  display: inline-block;
  margin-top: 9px;
  font-size: 12.5px;
  color: var(--xz-primary);
}

.brief-more:hover {
  text-decoration: underline;
}

/* ---------- 侧栏 ---------- */
.portfolio-side {
  display: flex;
  flex-direction: column;
  gap: 18px;
  position: sticky;
  top: 84px;
}

.side-card {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 20px 22px;
}

.side-title {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
  margin-bottom: 16px;
  padding-left: 9px;
  border-left: 3px solid var(--xz-primary);
  line-height: 1.3;
}

.radar-wrap {
  height: 230px;
}

.radar-chart {
  width: 100%;
  height: 100%;
}

.radar-empty {
  font-size: 12.5px;
  color: var(--xz-text-placeholder);
  text-align: center;
  padding: 12px 0;
}

.tech-stats {
  display: flex;
  flex-direction: column;
  gap: 11px;
}

.tech-stat {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tech-name {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  width: 92px;
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tech-bar-wrap {
  flex: 1;
  height: 6px;
  background: var(--xz-bg-hover);
  border-radius: 4px;
  overflow: hidden;
}

.tech-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--xz-primary), #4a918d);
  border-radius: 4px;
  transition: width 0.4s ease;
}

.tech-count {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  width: 18px;
  text-align: right;
  flex-shrink: 0;
}

@media (max-width: 1080px) {
  .portfolio-body {
    grid-template-columns: 1fr;
  }

  .portfolio-side {
    position: static;
  }

  .hero-stats {
    padding-left: 0;
    border-left: none;
    border-top: 1px solid var(--xz-border-light);
    padding-top: 20px;
    width: 100%;
  }
}

@media (max-width: 680px) {
  .card-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    padding: 22px 20px;
  }
}
</style>
