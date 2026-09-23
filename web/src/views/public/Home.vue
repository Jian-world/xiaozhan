<template>
  <div class="home-page">
    <!-- 首屏 -->
    <section class="hero">
      <div class="xz-container hero-inner">
        <div class="hero-copy">
          <div class="hero-badge">
            <el-icon><Opportunity /></el-icon>
            <span>用作品说话 · 让能力被看见</span>
          </div>
          <h1 class="hero-title">
            简历上写「精通」，
            <br />
            不如让人<span class="accent">直接打开你的项目</span>。
          </h1>
          <p class="hero-desc">
            校栈把课程设计、竞赛作品、实习产出整理成一份可点开的在线作品集。
            上传源码、文档与演示视频，获得高校导师与企业工程师的逐项点评，
            招聘方也能直接查验——彻底解决「简历好看，能力不足」的供需信息差。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="goStart">
              {{ userStore.isLogin ? '进入我的工作台' : '免费建立作品集' }}
            </el-button>
            <el-button size="large" @click="router.push('/square')">
              先逛逛项目广场
            </el-button>
          </div>

          <div class="hero-stats">
            <div v-for="s in statCards" :key="s.label" class="stat-item">
              <span class="stat-value">{{ s.value }}</span>
              <span class="stat-label">{{ s.label }}</span>
            </div>
          </div>
        </div>

        <div class="hero-visual">
          <div class="mock-window">
            <div class="mock-bar">
              <span class="dot dot-r"></span>
              <span class="dot dot-y"></span>
              <span class="dot dot-g"></span>
              <span class="mock-url">校栈 / 我的作品集</span>
            </div>
            <div class="mock-body">
              <div class="mock-profile">
                <el-avatar :size="44" style="background: var(--xz-primary)">李</el-avatar>
                <div class="mock-profile-text">
                  <div class="mock-name">
                    李思远
                    <span class="verified-badge">
                      <el-icon><CircleCheckFilled /></el-icon>
                      已认证
                    </span>
                  </div>
                  <div class="mock-sub">南京理工大学 · 软件工程 · 2026 届</div>
                </div>
              </div>

              <div class="mock-projects">
                <div v-for="p in mockProjects" :key="p.name" class="mock-project">
                  <div class="mock-project-head">
                    <span class="mock-project-name">{{ p.name }}</span>
                    <span class="mock-score">{{ p.score }}</span>
                  </div>
                  <div class="mock-project-tech">
                    <span v-for="t in p.tech" :key="t">{{ t }}</span>
                  </div>
                  <div class="mock-project-meta">
                    <span>{{ p.reviews }} 条专家点评</span>
                    <span>{{ p.views }} 次查看</span>
                  </div>
                </div>
              </div>

              <div class="mock-review">
                <div class="mock-review-head">
                  <el-avatar :size="22" style="background: var(--xz-accent)">王</el-avatar>
                  <span class="mock-reviewer">王工 · 某互联网公司 架构师</span>
                </div>
                <p class="mock-review-text">
                  「分层清晰，仓储层抽象到位。建议补充压测数据与降级方案，
                  并说明索引设计依据——这是校招面试的高频追问点。」
                </p>
              </div>
            </div>
          </div>
          <div class="visual-glow"></div>
        </div>
      </div>
    </section>

    <!-- 痛点 -->
    <section class="section pain-section">
      <div class="xz-container">
        <header class="section-head">
          <h2 class="section-title">问题不在「没有经历」，而在「经历无法被验证」</h2>
          <p class="section-sub">
            三方都卡在同一个信息断点上：学生说不清，导师看不见，企业判断不了。
          </p>
        </header>

        <div class="pain-grid">
          <div v-for="p in pains" :key="p.role" class="pain-card">
            <div class="pain-role">
              <el-icon :size="20"><component :is="p.icon" /></el-icon>
              <span>{{ p.role }}</span>
            </div>
            <ul class="pain-list">
              <li v-for="(item, i) in p.items" :key="i">{{ item }}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- 解决方案 / 功能 -->
    <section class="section solution-section">
      <div class="xz-container">
        <header class="section-head">
          <h2 class="section-title">校栈怎么做</h2>
          <p class="section-sub">
            一条从「上传作品」到「获得背书」再到「被企业看见」的完整链路。
          </p>
        </header>

        <div class="flow-steps">
          <div v-for="(step, index) in steps" :key="step.title" class="flow-step">
            <div class="step-index">{{ String(index + 1).padStart(2, '0') }}</div>
            <div class="step-body">
              <h3 class="step-title">{{ step.title }}</h3>
              <p class="step-desc">{{ step.desc }}</p>
              <div class="step-tags">
                <span v-for="t in step.tags" :key="t">{{ t }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 点评维度 -->
    <section class="section dimension-section">
      <div class="xz-container dimension-inner">
        <div class="dimension-copy">
          <h2 class="section-title">四维点评，把模糊的「不错」拆成可改进的清单</h2>
          <p class="dimension-desc">
            导师与企业工程师会从四个维度打分并写下具体意见。评分不只看结果，
            也看过程是否规范、方案是否经得起追问——这正是招聘方真正想确认的东西。
          </p>
          <el-button type="primary" plain @click="router.push('/experts')">
            看看点评人都是谁
          </el-button>
        </div>

        <div class="dimension-list">
          <div v-for="d in SCORE_DIMENSIONS" :key="d.key" class="dimension-item">
            <div class="dimension-name">
              <span>{{ d.label }}</span>
              <span class="dimension-hint">{{ d.hint }}</span>
            </div>
            <div class="dimension-bar">
              <span v-for="n in 5" :key="n" class="bar-cell" :style="barStyle(n)"></span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 精选项目 -->
    <section class="section featured-section">
      <div class="xz-container">
        <header class="section-head">
          <h2 class="section-title">近期值得一看的作品</h2>
          <p class="section-sub">来自不同专业方向的学生项目，均已获得专家点评</p>
        </header>

        <div v-if="loading" class="card-grid">
          <el-skeleton v-for="i in 6" :key="i" animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 100%; height: 148px" />
              <div style="padding: 14px 16px">
                <el-skeleton-item variant="h3" style="width: 60%" />
                <el-skeleton-item variant="text" style="margin-top: 10px" />
                <el-skeleton-item variant="text" style="width: 80%" />
              </div>
            </template>
          </el-skeleton>
        </div>

        <div v-else-if="projects.length" class="card-grid">
          <ProjectCard v-for="p in projects" :key="p.id" :project="p" show-meta />
        </div>

        <div v-else class="xz-empty-hint">
          还没有公开项目，去项目广场看看或成为第一个上传作品的人。
        </div>

        <div class="featured-more">
          <el-button size="large" @click="router.push('/square')">
            浏览全部项目
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </section>

    <!-- 关于 / CTA -->
    <section id="about" class="section about-section">
      <div class="xz-container">
        <div class="about-card">
          <div class="about-copy">
            <h2 class="about-title">我们相信：能力应该被直接看见</h2>
            <p class="about-desc">
              校栈不做「美化简历」的包装，而是提供一条让真实能力被验证的通道。
              学生的每一次 commit、每一份文档、每一次被点评后的改进，都会沉淀成可信的能力证据。
            </p>
            <div class="about-actions">
              <el-button type="primary" size="large" @click="goStart">
                {{ userStore.isLogin ? '进入工作台' : '现在开始，免费' }}
              </el-button>
              <el-button size="large" @click="router.push('/experts')">
                成为点评专家
              </el-button>
            </div>
          </div>

          <div class="about-values">
            <div v-for="v in values" :key="v.title" class="value-item">
              <el-icon :size="20"><component :is="v.icon" /></el-icon>
              <div>
                <strong>{{ v.title }}</strong>
                <span>{{ v.desc }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ProjectCard from '@/components/ProjectCard.vue'
import { useUserStore } from '@/stores/user'
import { commonApi, projectApi } from '@/api'
import { ROLE_HOME, SCORE_DIMENSIONS } from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const projects = ref([])
const stats = ref({
  studentCount: 0,
  expertCount: 0,
  companyCount: 0,
  projectCount: 0,
  reviewCount: 0
})

const statCards = computed(() => [
  { label: '入驻学生', value: formatNum(stats.value.studentCount) },
  { label: '专业点评人', value: formatNum(stats.value.expertCount) },
  { label: '在线项目', value: formatNum(stats.value.projectCount) },
  { label: '专家点评', value: formatNum(stats.value.reviewCount) }
])

function formatNum(n) {
  const v = Number(n) || 0
  return v >= 10000 ? `${(v / 10000).toFixed(1)}w+` : String(v)
}

const mockProjects = [
  { name: '校园二手交易平台', score: '4.8', tech: ['SpringBoot', 'Vue3', 'MySQL'], reviews: 3, views: 268 },
  { name: '分布式短链接服务', score: '4.5', tech: ['Redis', '布隆过滤器'], reviews: 2, views: 174 }
]

const pains = [
  {
    role: '学生',
    icon: 'Reading',
    items: [
      '课程作业散落在电脑各处，面试时翻不出来',
      '简历只能写「参与开发」，说不清自己做了什么',
      '没人告诉自己究竟差在哪里，只能盲目刷题'
    ]
  },
  {
    role: '导师 / 工程师',
    icon: 'Medal',
    items: [
      '想帮学生但缺少结构化的点评工具与抓手',
      '点评内容散落在聊天里，无法沉淀为可查记录',
      '经验丰富却难以规模化地影响更多学生'
    ]
  },
  {
    role: '招聘企业',
    icon: 'OfficeBuilding',
    items: [
      '简历同质化严重，「精通」二字毫无信息量',
      '面试成本高，初筛阶段无法验证真实动手能力',
      '缺少第三方专业视角作为能力参考'
    ]
  }
]

const steps = [
  {
    title: '结构化上传作品',
    desc: '源码包、设计文档、演示视频分类归档，配上技术栈、担任角色与难点说明，自动生成一页可分享的作品集。',
    tags: ['源码包归档', '富文本技术亮点', '演示视频']
  },
  {
    title: '申请身份认证',
    desc: '学生上传学生证完成学籍认证，导师与工程师提交资质认证。认证标识直接展示在作品集与点评上。',
    tags: ['学籍认证', '专家资质', '企业营业执照']
  },
  {
    title: '投递求点评池',
    desc: '把希望获得反馈的项目投进求点评池，系统按「待点评优先 + 加推权重」推给合适的点评人。',
    tags: ['求点评池', '加推机制', '定向邀请']
  },
  {
    title: '获得四维点评',
    desc: '点评人从完成度、规范性、创新性、专业质量四个维度打分，并写下亮点、不足与具体改进建议。',
    tags: ['四维评分', '亮点/不足/建议', '质量校验']
  },
  {
    title: '企业直接查验',
    desc: '招聘方按专业方向、技能标签、毕业年份检索候选人，直接打开项目源码与点评记录，替代单薄的简历初筛。',
    tags: ['人才检索', '作品集查验', '收藏与邀约']
  }
]

const values = [
  { icon: 'DocumentChecked', title: '真实优先', desc: '拒绝包装与夸大，项目材料需可追溯' },
  { icon: 'ChatLineSquare', title: '专业点评', desc: '只看能力的实际水平，不看热度' },
  { icon: 'Lock', title: '隐私可控', desc: '作品可见范围由学生自己决定' }
]

const barStyle = (n) => ({
  background: n <= 4 ? 'var(--xz-primary)' : '#dbe6e6'
})

const goStart = () => {
  if (userStore.isLogin) {
    router.push(ROLE_HOME[userStore.role] || '/')
  } else {
    router.push('/register')
  }
}

onMounted(async () => {
  try {
    const [s, p] = await Promise.all([
      commonApi.stats().catch(() => stats.value),
      projectApi.search({ pageNum: 1, pageSize: 6, sortBy: 'score' }).catch(() => null)
    ])
    if (s) stats.value = { ...stats.value, ...s }
    if (p?.records) projects.value = p.records
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-page {
  padding-bottom: 8px;
}

/* ---------- Hero ---------- */
.hero {
  position: relative;
  padding: 64px 0 72px;
  background: linear-gradient(168deg, #f7fbfa 0%, #eef6f5 45%, #fdf8f3 100%);
  overflow: hidden;
}

.hero-inner {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 56px;
  align-items: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid #d6e8e6;
  font-size: 12.5px;
  color: var(--xz-primary-dark);
  font-weight: 500;
}

.hero-title {
  margin-top: 22px;
  font-size: 42px;
  line-height: 1.32;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--xz-text-primary);
}

.hero-title .accent {
  color: var(--xz-primary);
  position: relative;
}

.hero-title .accent::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 2px;
  height: 8px;
  background: rgba(224, 122, 63, 0.22);
  border-radius: 4px;
  z-index: -1;
}

.hero-desc {
  margin-top: 20px;
  font-size: 15px;
  line-height: 1.95;
  color: var(--xz-text-secondary);
  max-width: 540px;
}

.hero-actions {
  margin-top: 30px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-actions :deep(.el-button) {
  height: 46px;
  padding: 0 26px;
  font-size: 15px;
}

.hero-stats {
  margin-top: 44px;
  display: flex;
  gap: 44px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  margin-top: 2px;
}

/* ---------- Hero 视觉 ---------- */
.hero-visual {
  position: relative;
  display: flex;
  justify-content: center;
}

.visual-glow {
  position: absolute;
  width: 380px;
  height: 380px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(31, 111, 107, 0.13), transparent 68%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 0;
}

.mock-window {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 430px;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 20px 50px rgba(28, 43, 51, 0.13);
  overflow: hidden;
  border: 1px solid #e8eef0;
}

.mock-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 11px 14px;
  background: #f7f9fa;
  border-bottom: 1px solid var(--xz-border-light);
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.dot-r {
  background: #f2a6a0;
}

.dot-y {
  background: #f0cf94;
}

.dot-g {
  background: #a4d8b4;
}

.mock-url {
  margin-left: 10px;
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

.mock-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.mock-profile {
  display: flex;
  align-items: center;
  gap: 11px;
}

.mock-name {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 15px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.verified-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 10.5px;
  font-weight: 500;
  color: var(--xz-success);
  background: #eaf5ef;
  padding: 1px 7px;
  border-radius: 10px;
}

.mock-sub {
  font-size: 11.5px;
  color: var(--xz-text-secondary);
  margin-top: 2px;
}

.mock-projects {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.mock-project {
  padding: 11px 12px;
  border-radius: 9px;
  background: #fbfcfc;
  border: 1px solid var(--xz-border-light);
}

.mock-project-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mock-project-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.mock-score {
  font-size: 13px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
}

.mock-project-tech {
  display: flex;
  gap: 5px;
  margin-top: 7px;
}

.mock-project-tech span {
  font-size: 10.5px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 1px 7px;
  border-radius: 4px;
}

.mock-project-meta {
  display: flex;
  gap: 14px;
  margin-top: 7px;
  font-size: 11px;
  color: var(--xz-text-placeholder);
}

.mock-review {
  padding: 12px;
  border-radius: 9px;
  background: var(--xz-accent-light);
  border: 1px solid #f4e0cf;
}

.mock-review-head {
  display: flex;
  align-items: center;
  gap: 7px;
}

.mock-reviewer {
  font-size: 11.5px;
  font-weight: 600;
  color: #8a5227;
}

.mock-review-text {
  margin-top: 7px;
  font-size: 11.5px;
  line-height: 1.7;
  color: #7a5333;
}

/* ---------- 通用 section ---------- */
.section {
  padding: 72px 0;
}

.section-head {
  text-align: center;
  max-width: 720px;
  margin: 0 auto 44px;
}

.section-title {
  font-size: 27px;
  font-weight: 700;
  color: var(--xz-text-primary);
  line-height: 1.45;
}

.section-sub {
  margin-top: 12px;
  font-size: 14.5px;
  color: var(--xz-text-secondary);
  line-height: 1.8;
}

/* ---------- 痛点 ---------- */
.pain-section {
  background: #ffffff;
}

.pain-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}

.pain-card {
  padding: 26px 24px;
  border-radius: var(--xz-radius-lg);
  background: #fbfcfc;
  border: 1px solid var(--xz-border-light);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.pain-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--xz-shadow);
}

.pain-role {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 15px;
  font-weight: 600;
  color: var(--xz-primary);
  padding-bottom: 14px;
  border-bottom: 1px dashed var(--xz-border);
  margin-bottom: 16px;
}

.pain-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pain-list li {
  position: relative;
  padding-left: 18px;
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--xz-text-secondary);
}

.pain-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--xz-accent);
  opacity: 0.65;
}

/* ---------- 流程 ---------- */
.solution-section {
  background: var(--xz-bg-page);
}

.flow-steps {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.flow-step {
  display: flex;
  gap: 26px;
  padding: 26px 0;
  border-bottom: 1px solid var(--xz-border-light);
}

.flow-step:last-child {
  border-bottom: none;
}

.step-index {
  font-size: 30px;
  font-weight: 700;
  color: #d3e0df;
  line-height: 1.1;
  flex-shrink: 0;
  width: 62px;
  font-variant-numeric: tabular-nums;
}

.step-body {
  flex: 1;
}

.step-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.step-desc {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.85;
  color: var(--xz-text-secondary);
  max-width: 820px;
}

.step-tags {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.step-tags span {
  font-size: 12px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 3px 10px;
  border-radius: 20px;
}

/* ---------- 点评维度 ---------- */
.dimension-section {
  background: #ffffff;
}

.dimension-inner {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 56px;
  align-items: center;
}

.dimension-desc {
  margin: 16px 0 24px;
  font-size: 14.5px;
  line-height: 1.9;
  color: var(--xz-text-secondary);
}

.dimension-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dimension-item {
  padding: 18px 20px;
  border-radius: var(--xz-radius);
  background: #fbfcfc;
  border: 1px solid var(--xz-border-light);
}

.dimension-name {
  display: flex;
  flex-direction: column;
  margin-bottom: 11px;
}

.dimension-name > span:first-child {
  font-size: 15px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.dimension-hint {
  font-size: 12.5px;
  color: var(--xz-text-placeholder);
  margin-top: 3px;
}

.dimension-bar {
  display: flex;
  gap: 6px;
}

.bar-cell {
  flex: 1;
  height: 7px;
  border-radius: 4px;
}

/* ---------- 精选项目 ---------- */
.featured-section {
  background: var(--xz-bg-page);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}

.featured-more {
  margin-top: 38px;
  text-align: center;
}

.featured-more :deep(.el-button) {
  height: 44px;
  padding: 0 28px;
}

/* ---------- 关于 ---------- */
.about-section {
  background: #ffffff;
  padding-bottom: 40px;
}

.about-card {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 56px;
  padding: 48px 52px;
  border-radius: var(--xz-radius-lg);
  background: linear-gradient(140deg, #f2f9f8 0%, #eaf3f2 100%);
  border: 1px solid #dcebe9;
}

.about-title {
  font-size: 25px;
  font-weight: 700;
  color: var(--xz-text-primary);
  line-height: 1.45;
}

.about-desc {
  margin-top: 16px;
  font-size: 14.5px;
  line-height: 1.9;
  color: var(--xz-text-secondary);
}

.about-actions {
  margin-top: 28px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.about-actions :deep(.el-button) {
  height: 44px;
  padding: 0 24px;
}

.about-values {
  display: flex;
  flex-direction: column;
  gap: 22px;
  justify-content: center;
}

.value-item {
  display: flex;
  gap: 13px;
  align-items: flex-start;
}

.value-item .el-icon {
  color: var(--xz-primary);
  margin-top: 3px;
  flex-shrink: 0;
}

.value-item div {
  display: flex;
  flex-direction: column;
}

.value-item strong {
  font-size: 14.5px;
  color: var(--xz-text-primary);
}

.value-item span {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  margin-top: 3px;
}

@media (max-width: 1080px) {
  .hero-inner,
  .dimension-inner,
  .about-card {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .pain-grid,
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .hero-title {
    font-size: 34px;
  }
}

@media (max-width: 680px) {
  .pain-grid,
  .card-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    padding: 40px 0 48px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-stats {
    gap: 26px;
  }

  .about-card {
    padding: 32px 24px;
  }

  .step-index {
    width: 44px;
    font-size: 24px;
  }
}
</style>
