<template>
  <div v-loading="loading" class="detail-page">
    <template v-if="project">
      <div class="detail-hero">
        <div class="xz-container">
          <el-breadcrumb separator="/" class="crumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/square' }">项目广场</el-breadcrumb-item>
            <el-breadcrumb-item>{{ project.name }}</el-breadcrumb-item>
          </el-breadcrumb>

          <div class="hero-main">
            <div class="hero-copy">
              <div class="hero-tags">
                <el-tag size="small" effect="dark">{{ categoryText }}</el-tag>
                <el-tag size="small" type="info" effect="plain">{{ typeText }}</el-tag>
                <el-tag v-if="project.inReviewPool === 1" size="small" type="warning" effect="plain">
                  正在求点评
                </el-tag>
              </div>

              <h1 class="project-title">{{ project.name }}</h1>
              <p class="project-summary">{{ project.summary || '作者暂未填写项目简介' }}</p>

              <div class="hero-metrics">
                <div class="metric">
                  <span class="metric-value">{{ project.expertAvgScore > 0 ? Number(project.expertAvgScore).toFixed(1) : '—' }}</span>
                  <span class="metric-label">专家评分</span>
                </div>
                <div class="metric">
                  <span class="metric-value">{{ project.expertReviewCount || 0 }}</span>
                  <span class="metric-label">点评数</span>
                </div>
                <div class="metric">
                  <span class="metric-value">{{ project.viewCount || 0 }}</span>
                  <span class="metric-label">浏览量</span>
                </div>
              </div>

              <div v-if="techList.length" class="hero-tech">
                <span v-for="t in techList" :key="t" class="tech-pill">{{ t }}</span>
              </div>

              <div class="hero-actions">
                <el-button v-if="project.repoUrl" type="primary" @click="openRepo">
                  <el-icon><Link /></el-icon>
                  查看代码仓库
                </el-button>
                <el-button v-if="project.owner" @click="router.push(`/student/projects/edit/${project.id}`)">
                  <el-icon><Edit /></el-icon>
                  编辑项目
                </el-button>
                <el-button
                  v-if="userStore.isExpert && userStore.expertVerifyStatus === 2"
                  type="primary"
                  plain
                  @click="router.push(`/expert/review/${project.id}`)"
                >
                  <el-icon><ChatDotSquare /></el-icon>
                  撰写点评
                </el-button>
              </div>
            </div>

            <aside class="hero-author">
              <div class="author-card">
                <el-avatar :size="58" :src="project.studentAvatar" class="author-avatar">
                  {{ (project.studentName || '同').slice(0, 1) }}
                </el-avatar>
                <div class="author-name">
                  {{ project.studentName || '匿名同学' }}
                  <el-tooltip v-if="project.eduVerified === 2" content="已通过学籍认证">
                    <el-icon class="verified-icon"><CircleCheckFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="author-school">
                  {{ project.studentSchool || '—' }}
                  <template v-if="project.studentMajor"> · {{ project.studentMajor }}</template>
                </div>

                <div class="author-actions">
                  <el-button size="small" @click="router.push(`/portfolio/${project.studentId}`)">
                    查看完整作品集
                  </el-button>
                </div>

                <dl class="author-meta">
                  <div>
                    <dt>项目周期</dt>
                    <dd>{{ periodText }}</dd>
                  </div>
                  <div>
                    <dt>担任角色</dt>
                    <dd>{{ project.roleDesc || '未填写' }}</dd>
                  </div>
                  <div>
                    <dt>可见范围</dt>
                    <dd>{{ visibilityText }}</dd>
                  </div>
                </dl>
              </div>
            </aside>
          </div>
        </div>
      </div>

      <div class="xz-container detail-body">
        <div class="detail-main">
          <!-- 素材 -->
          <section class="block">
            <h2 class="xz-section-title">项目素材</h2>
            <div v-if="assetsByType.length" class="asset-groups">
              <div v-for="group in assetsByType" :key="group.type" class="asset-group">
                <div class="asset-group-head">
                  <el-icon><component :is="ASSET_TYPE_ICON[group.type]" /></el-icon>
                  <span>{{ ASSET_TYPE_TEXT[group.type] }}</span>
                  <span class="asset-count">{{ group.items.length }}</span>
                </div>
                <ul class="asset-list">
                  <li v-for="a in group.items" :key="a.id" class="asset-item">
                    <div class="asset-info">
                      <el-icon class="asset-icon"><component :is="ASSET_TYPE_ICON[group.type]" /></el-icon>
                      <div class="asset-text">
                        <span class="asset-name">{{ a.fileName }}</span>
                        <span class="asset-meta">
                          {{ a.fileExt ? a.fileExt.toUpperCase() : '' }}
                          <template v-if="a.readableSize"> · {{ a.readableSize }}</template>
                        </span>
                      </div>
                    </div>
                    <div class="asset-ops">
                      <el-button
                        v-if="group.type === 'VIDEO'"
                        size="small"
                        type="primary"
                        text
                        @click="playVideo(a)"
                      >
                        在线播放
                      </el-button>
                      <a :href="a.fileUrl" target="_blank" rel="noopener" class="asset-link">
                        <el-button size="small" text>
                          {{ group.type === 'VIDEO' ? '下载' : '查看 / 下载' }}
                        </el-button>
                      </a>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
            <el-empty v-else description="该项目暂未上传素材" :image-size="80" />
          </section>

          <!-- 技术亮点 -->
          <section class="block">
            <h2 class="xz-section-title">技术难点与亮点</h2>
            <div v-if="project.highlight" class="xz-rich-text" v-html="project.highlight"></div>
            <el-empty v-else description="作者暂未填写技术说明" :image-size="80" />
          </section>

          <!-- 协作者 -->
          <section v-if="project.members && project.members.length" class="block">
            <h2 class="xz-section-title">项目成员</h2>
            <div class="member-list">
              <div v-for="m in project.members" :key="m.studentId" class="member-item">
                <el-avatar :size="38" :src="m.avatar">{{ (m.studentName || '同').slice(0, 1) }}</el-avatar>
                <div class="member-info">
                  <span class="member-name">{{ m.studentName }}</span>
                  <span class="member-role">
                    {{ m.memberRole }}
                    <template v-if="m.school"> · {{ m.school }}</template>
                  </span>
                </div>
                <el-tag v-if="m.isOwner === 1" size="small" effect="plain">主理人</el-tag>
              </div>
            </div>
          </section>

          <!-- 点评 -->
          <section class="block">
            <div class="block-head">
              <h2 class="xz-section-title">专家点评</h2>
              <span class="xz-text-muted xz-text-small">
                共 {{ project.reviews?.length || 0 }} 条
              </span>
            </div>

            <div v-if="project.reviews && project.reviews.length" class="review-list">
              <article v-for="r in project.reviews" :key="r.id" class="review-card">
                <header class="review-head">
                  <el-avatar :size="42" :src="r.expertAvatar">
                    {{ (r.expertName || '师').slice(0, 1) }}
                  </el-avatar>
                  <div class="reviewer">
                    <div class="reviewer-name">
                      {{ r.expertName }}
                      <el-tag
                        v-if="r.expertLevel"
                        size="small"
                        :type="EXPERT_LEVEL_TYPE[r.expertLevel]"
                        effect="plain"
                      >
                        {{ EXPERT_LEVEL_TEXT[r.expertLevel] }}
                      </el-tag>
                    </div>
                    <div class="reviewer-org">
                      <template v-if="r.expertOrg">
                        {{ r.expertOrg }}<template v-if="r.expertPosition"> · {{ r.expertPosition }}</template>
                      </template>
                      <template v-else>{{ EXPERT_TYPE_TEXT[r.expertType] || '认证点评人' }}</template>
                      <span class="review-time"> · {{ fromNow(r.createTime) }}</span>
                    </div>
                  </div>
                  <div class="review-score">
                    <span class="score-num">{{ Number(r.avgScore || 0).toFixed(1) }}</span>
                    <el-rate
                      :model-value="Number(r.avgScore || 0)"
                      disabled
                      allow-half
                      size="small"
                      show-score="false"
                    />
                  </div>
                </header>

                <div class="review-scores">
                  <div v-for="dim in SCORE_DIMENSIONS" :key="dim.key" class="mini-dim">
                    <span class="mini-label">{{ dim.label }}</span>
                    <el-rate :model-value="r[dim.key] || 0" disabled size="small" />
                  </div>
                </div>

                <div class="review-content">
                  <p class="review-comment">{{ r.comment }}</p>

                  <div v-if="r.strength" class="review-part is-strength">
                    <span class="part-label">
                      <el-icon><Select /></el-icon>
                      做得好的地方
                    </span>
                    <p>{{ r.strength }}</p>
                  </div>
                  <div v-if="r.weakness" class="review-part is-weakness">
                    <span class="part-label">
                      <el-icon><Warning /></el-icon>
                      不足之处
                    </span>
                    <p>{{ r.weakness }}</p>
                  </div>
                  <div v-if="r.suggestion" class="review-part is-suggestion">
                    <span class="part-label">
                      <el-icon><Opportunity /></el-icon>
                      改进建议
                    </span>
                    <p>{{ r.suggestion }}</p>
                  </div>
                </div>

                <div v-if="r.replies && r.replies.length" class="reply-list">
                  <div v-for="rp in r.replies" :key="rp.id" class="reply-item">
                    <el-avatar :size="24" :src="rp.avatar">{{ (rp.userName || '用').slice(0, 1) }}</el-avatar>
                    <div class="reply-body">
                      <span class="reply-name">
                        {{ rp.userName }}
                        <el-tag size="small" effect="plain">{{ ROLE_TEXT[rp.role] || rp.role }}</el-tag>
                      </span>
                      <p class="reply-text">{{ rp.content }}</p>
                    </div>
                  </div>
                </div>
              </article>
            </div>

            <el-empty v-else description="还没有专家点评，把项目投进求点评池试试" :image-size="80" />
          </section>
        </div>

        <aside class="detail-side">
          <div class="side-card">
            <h3 class="side-title">项目速览</h3>
            <dl class="side-meta">
              <div>
                <dt>项目方向</dt>
                <dd>{{ categoryText }}</dd>
              </div>
              <div>
                <dt>项目类型</dt>
                <dd>{{ typeText }}</dd>
              </div>
              <div>
                <dt>时间跨度</dt>
                <dd>{{ periodText }}</dd>
              </div>
              <div>
                <dt>技术栈</dt>
                <dd>{{ techList.length ? techList.join('、') : '未填写' }}</dd>
              </div>
              <div>
                <dt>发布时间</dt>
                <dd>{{ formatDate(project.createTime) }}</dd>
              </div>
            </dl>
          </div>

          <div v-if="sharedScore" class="side-card">
            <h3 class="side-title">评分构成</h3>
            <ScoreBlock :review="sharedScore" :count="project.expertReviewCount || 1" />
          </div>

          <div class="side-card side-tip">
            <el-icon><InfoFilled /></el-icon>
            <p>
              该项目由学生自主提交，平台已做内容审核。专家点评仅代表点评人个人判断，
              供参考与改进之用。
            </p>
          </div>
        </aside>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="项目不存在或已被删除" />

    <el-dialog v-model="videoVisible" :title="videoTitle" width="820px" destroy-on-close>
      <video v-if="videoUrl" :src="videoUrl" controls autoplay class="video-player"></video>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ScoreBlock from '@/components/ScoreBlock.vue'
import { projectApi } from '@/api'
import { useUserStore } from '@/stores/user'
import {
  ASSET_TYPE_ICON,
  ASSET_TYPE_TEXT,
  CATEGORY_TEXT,
  EXPERT_LEVEL_TEXT,
  EXPERT_LEVEL_TYPE,
  EXPERT_TYPE_TEXT,
  PROJECT_TYPE_TEXT,
  ROLE_TEXT,
  SCORE_DIMENSIONS,
  VISIBILITY_TEXT,
  formatDate,
  fromNow
} from '@/utils/dict'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const project = ref(null)

const videoVisible = ref(false)
const videoUrl = ref('')
const videoTitle = ref('')

const categoryText = computed(() => CATEGORY_TEXT[project.value?.category] || '未分类')
const typeText = computed(() => PROJECT_TYPE_TEXT[project.value?.projectType] || '其他')
const visibilityText = computed(() => VISIBILITY_TEXT[project.value?.visibility] || '公开可见')

const techList = computed(() => {
  const t = project.value?.techStack
  if (Array.isArray(t)) return t
  if (typeof t === 'string' && t) return t.split(',').map((s) => s.trim()).filter(Boolean)
  return []
})

const periodText = computed(() => {
  const p = project.value
  if (!p) return '—'
  const start = p.startDate ? formatDate(p.startDate) : ''
  const end = p.endDate ? formatDate(p.endDate) : ''
  if (!start && !end) return '未填写'
  if (start && end) return `${start} ~ ${end}`
  return start ? `${start} 起` : `至 ${end}`
})

const assetsByType = computed(() => {
  const assets = project.value?.assets || []
  const order = ['SOURCE', 'DOC', 'VIDEO', 'IMAGE']
  return order
    .map((type) => ({ type, items: assets.filter((a) => a.assetType === type) }))
    .filter((g) => g.items.length > 0)
})

/** 从点评列表聚合出一个平均分对象，用于侧栏评分构成 */
const sharedScore = computed(() => {
  const reviews = project.value?.reviews || []
  if (!reviews.length) return null
  const sum = { scoreCompletion: 0, scoreNormative: 0, scoreInnovation: 0, scoreTechnical: 0 }
  reviews.forEach((r) => {
    SCORE_DIMENSIONS.forEach((d) => {
      sum[d.key] += r[d.key] || 0
    })
  })
  const n = reviews.length
  const avg = { ...sum }
  SCORE_DIMENSIONS.forEach((d) => {
    avg[d.key] = Math.round((sum[d.key] / n) * 10) / 10
  })
  avg.avgScore = Number(project.value.expertAvgScore || 0)
  return avg
})

const load = async () => {
  loading.value = true
  try {
    const forCompany = userStore.isCompany
    project.value = await projectApi.detail(route.params.id, forCompany)
  } catch (e) {
    project.value = null
  } finally {
    loading.value = false
  }
}

const openRepo = () => {
  if (project.value?.repoUrl) {
    window.open(project.value.repoUrl, '_blank', 'noopener')
  }
}

const playVideo = (asset) => {
  videoUrl.value = asset.fileUrl
  videoTitle.value = asset.fileName
  videoVisible.value = true
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.detail-page {
  min-height: 60vh;
}

.detail-hero {
  padding: 22px 0 34px;
  background: linear-gradient(168deg, #f7fbfa 0%, #eef6f5 100%);
  border-bottom: 1px solid var(--xz-border-light);
}

.crumb {
  margin-bottom: 18px;
  font-size: 13px;
}

.hero-main {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 40px;
  align-items: start;
}

.hero-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.project-title {
  margin-top: 14px;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--xz-text-primary);
}

.project-summary {
  margin-top: 12px;
  font-size: 15px;
  line-height: 1.85;
  color: var(--xz-text-secondary);
  max-width: 720px;
}

.hero-metrics {
  display: flex;
  gap: 40px;
  margin-top: 24px;
}

.metric {
  display: flex;
  flex-direction: column;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.metric-label {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  margin-top: 2px;
}

.hero-tech {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 22px;
}

.tech-pill {
  font-size: 12.5px;
  color: var(--xz-primary-dark);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #d6e8e6;
  padding: 4px 11px;
  border-radius: 20px;
}

.hero-actions {
  margin-top: 26px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* ---------- 作者卡片 ---------- */
.author-card {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  box-shadow: var(--xz-shadow-sm);
  padding: 24px 20px;
  text-align: center;
}

.author-avatar {
  background: var(--xz-primary);
  font-size: 22px;
}

.author-name {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--xz-text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.verified-icon {
  color: var(--xz-success);
  font-size: 15px;
}

.author-school {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.author-actions {
  margin-top: 16px;
}

.author-meta {
  margin: 18px 0 0;
  padding-top: 16px;
  border-top: 1px solid var(--xz-border-light);
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.author-meta > div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 12.5px;
}

.author-meta dt {
  color: var(--xz-text-placeholder);
  flex-shrink: 0;
}

.author-meta dd {
  margin: 0;
  color: var(--xz-text-primary);
  text-align: right;
  word-break: break-word;
}

/* ---------- 主体 ---------- */
.detail-body {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 40px;
  padding-top: 32px;
  align-items: start;
}

.block {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 26px 28px;
  margin-bottom: 22px;
}

.block .xz-section-title {
  margin-bottom: 20px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.block-head .xz-section-title {
  margin-bottom: 0;
}

/* ---------- 素材 ---------- */
.asset-groups {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.asset-group-head {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
  margin-bottom: 10px;
}

.asset-group-head .el-icon {
  color: var(--xz-primary);
}

.asset-count {
  font-size: 11.5px;
  font-weight: 400;
  color: var(--xz-text-placeholder);
  background: var(--xz-bg-hover);
  border-radius: 10px;
  padding: 0 7px;
}

.asset-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.asset-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 14px;
  border-radius: var(--xz-radius-sm);
  background: #fbfcfc;
  border: 1px solid var(--xz-border-light);
  transition: border-color 0.18s ease, background 0.18s ease;
}

.asset-item:hover {
  border-color: #cfe3e2;
  background: #f8fbfb;
}

.asset-info {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.asset-icon {
  font-size: 17px;
  color: var(--xz-primary);
  flex-shrink: 0;
}

.asset-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.asset-name {
  font-size: 13.5px;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 420px;
}

.asset-meta {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 1px;
}

.asset-ops {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.asset-link {
  text-decoration: none;
}

/* ---------- 成员 ---------- */
.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.member-name {
  font-size: 14px;
  color: var(--xz-text-primary);
  font-weight: 500;
}

.member-role {
  font-size: 12px;
  color: var(--xz-text-secondary);
  margin-top: 1px;
}

/* ---------- 点评 ---------- */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.review-card {
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius);
  padding: 20px 22px;
  background: #fdfefe;
}

.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer {
  flex: 1;
  min-width: 0;
}

.reviewer-name {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.reviewer-org {
  font-size: 12px;
  color: var(--xz-text-secondary);
  margin-top: 2px;
}

.review-time {
  color: var(--xz-text-placeholder);
}

.review-score {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}

.score-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.review-scores {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 26px;
  margin-top: 16px;
  padding: 13px 16px;
  background: #f7fafa;
  border-radius: var(--xz-radius-sm);
}

.mini-dim {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.mini-label {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.review-content {
  margin-top: 16px;
}

.review-comment {
  font-size: 14px;
  line-height: 1.9;
  color: var(--xz-text-primary);
  white-space: pre-wrap;
}

.review-part {
  margin-top: 14px;
  padding: 12px 15px;
  border-radius: var(--xz-radius-sm);
}

.review-part p {
  font-size: 13.5px;
  line-height: 1.85;
  color: var(--xz-text-primary);
  margin-top: 6px;
  white-space: pre-wrap;
}

.part-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
}

.is-strength {
  background: #f0f8f3;
}

.is-strength .part-label {
  color: var(--xz-success);
}

.is-weakness {
  background: #fdf4f4;
}

.is-weakness .part-label {
  color: var(--xz-danger);
}

.is-suggestion {
  background: var(--xz-accent-light);
}

.is-suggestion .part-label {
  color: #b0642c;
}

/* ---------- 回应 ---------- */
.reply-list {
  margin-top: 16px;
  padding-top: 15px;
  border-top: 1px dashed var(--xz-border);
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.reply-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.reply-body {
  flex: 1;
}

.reply-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.reply-text {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--xz-text-secondary);
  white-space: pre-wrap;
}

/* ---------- 侧栏 ---------- */
.detail-side {
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

.side-meta {
  display: flex;
  flex-direction: column;
  gap: 13px;
  margin: 0;
}

.side-meta > div {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.side-meta dt {
  font-size: 12px;
  color: var(--xz-text-placeholder);
}

.side-meta dd {
  margin: 0;
  font-size: 13.5px;
  color: var(--xz-text-primary);
  line-height: 1.6;
  word-break: break-word;
}

.side-tip {
  display: flex;
  gap: 9px;
  background: #fbfcfc;
}

.side-tip .el-icon {
  color: var(--xz-text-placeholder);
  margin-top: 2px;
  flex-shrink: 0;
}

.side-tip p {
  font-size: 12px;
  line-height: 1.75;
  color: var(--xz-text-secondary);
}

.video-player {
  width: 100%;
  border-radius: var(--xz-radius-sm);
  background: #000;
}

@media (max-width: 1080px) {
  .hero-main,
  .detail-body {
    grid-template-columns: 1fr;
    gap: 26px;
  }

  .detail-side {
    position: static;
  }

  .project-title {
    font-size: 24px;
  }
}

@media (max-width: 680px) {
  .review-scores {
    grid-template-columns: 1fr;
  }

  .block {
    padding: 20px 18px;
  }

  .hero-metrics {
    gap: 26px;
  }
}
</style>
