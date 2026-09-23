<template>
  <div class="company-talent">
    <div class="page-head">
      <div>
        <h2 class="page-title">人才检索</h2>
        <p class="page-sub">
          按专业方向、学历与技能标签定位候选人。列表展示的是脱敏摘要，
          查看完整档案（含全部项目与点评）会消耗 1 次查验额度。
        </p>
      </div>
      <div class="quota-chip" :class="{ 'is-exhausted': remainQuota <= 0 }">
        <el-icon><View /></el-icon>
        <div class="chip-body">
          <span class="chip-value">{{ remainQuota }}</span>
          <span class="chip-label">本月剩余额度</span>
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
      <template #title>尚未通过企业认证，无法使用人才检索</template>
      <template #default>
        <span class="alert-body">
          完成企业认证后才能查看候选人档案与发送邀约。
          <el-button link type="primary" @click="router.push('/company/profile')">
            前往企业档案完成认证 →
          </el-button>
        </span>
      </template>
    </el-alert>

    <!-- 检索条件 -->
    <section class="filter-panel">
      <div class="filter-row">
        <el-input
          v-model="query.keyword"
          placeholder="搜索姓名、学校、技能或项目名称"
          :prefix-icon="Search"
          clearable
          class="keyword-input"
          @keyup.enter="reload"
          @clear="reload"
        />

        <el-select v-model="query.majorCategory" placeholder="专业大类" clearable class="filter-select">
          <el-option v-for="c in CATEGORIES" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>

        <el-select v-model="query.degree" placeholder="学历" clearable class="filter-select">
          <el-option label="专科" value="专科" />
          <el-option label="本科" value="本科" />
          <el-option label="硕士" value="硕士" />
          <el-option label="博士" value="博士" />
        </el-select>

        <el-select v-model="query.graduateYear" placeholder="毕业年份" clearable class="filter-select">
          <el-option v-for="y in gradYears" :key="y" :label="`${y} 届`" :value="y" />
        </el-select>

        <el-button type="primary" @click="reload">
          <el-icon><Search /></el-icon>
          检索
        </el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <div class="filter-row secondary">
        <div class="skill-filter">
          <span class="filter-label">技能标签</span>
          <el-tag
            v-for="t in quickSkills"
            :key="t"
            :effect="skillFilter.includes(t) ? 'dark' : 'plain'"
            class="skill-option"
            @click="toggleSkill(t)"
          >
            {{ t }}
          </el-tag>
          <el-input
            v-model="query.skillTags"
            placeholder="或手动输入，逗号分隔"
            size="small"
            class="skill-manual"
            clearable
          />
        </div>

        <el-checkbox v-model="onlyVerified" @change="reload">仅看已通过学籍认证</el-checkbox>
      </div>
    </section>

    <div class="result-bar">
      <span class="result-count">共找到 <strong>{{ total }}</strong> 位候选人</span>
      <span class="result-tip">
        <el-icon><InfoFilled /></el-icon>
        列表视图不消耗查验额度
      </span>
    </div>

    <div v-loading="loading" class="talent-body">
      <div v-if="candidates.length" class="candidate-grid">
        <article v-for="c in candidates" :key="c.studentId" class="candidate-card">
          <header class="card-head">
            <el-avatar :size="52" :src="c.avatar" class="cand-avatar">
              {{ (c.nickname || '同').slice(0, 1) }}
            </el-avatar>
            <div class="cand-head-info">
              <h3 class="cand-name">
                {{ displayName(c) }}
                <el-tooltip v-if="c.eduVerified === 2" content="已通过学籍认证">
                  <el-icon class="verified-icon"><CircleCheckFilled /></el-icon>
                </el-tooltip>
                <el-tooltip v-else-if="c.eduVerified === 1" content="学籍认证审核中">
                  <el-icon class="pending-icon"><Clock /></el-icon>
                </el-tooltip>
              </h3>
              <p class="cand-school">
                {{ c.school || '未填写学校' }}
                <template v-if="c.major"> · {{ c.major }}</template>
              </p>
              <p class="cand-degree">
                {{ c.degree || '学历未填' }}
                <template v-if="c.graduateYear"> · {{ c.graduateYear }} 届</template>
                <template v-if="c.majorCategory"> · {{ CATEGORY_TEXT[c.majorCategory] }}</template>
              </p>
            </div>
            <el-button
              size="small"
              text
              :type="c.favorited ? 'warning' : 'default'"
              @click="toggleFavorite(c)"
            >
              <el-icon><component :is="c.favorited ? 'StarFilled' : 'Star'" /></el-icon>
            </el-button>
          </header>

          <p v-if="c.bio" class="cand-bio">{{ c.bio }}</p>

          <div v-if="c.skillTags && c.skillTags.length" class="cand-skills">
            <span v-for="t in c.skillTags.slice(0, 6)" :key="t" class="skill-pill">{{ t }}</span>
            <span v-if="c.skillTags.length > 6" class="skill-more">+{{ c.skillTags.length - 6 }}</span>
          </div>

          <div class="cand-metrics">
            <div class="metric">
              <span class="metric-value">{{ c.projectCount || 0 }}</span>
              <span class="metric-label">公开项目</span>
            </div>
            <div class="metric">
              <span class="metric-value">
                {{ c.avgScore > 0 ? Number(c.avgScore).toFixed(1) : '—' }}
              </span>
              <span class="metric-label">专家均分</span>
            </div>
            <div class="metric">
              <span class="metric-value">{{ c.portfolioViews || 0 }}</span>
              <span class="metric-label">作品集查阅</span>
            </div>
          </div>

          <div v-if="c.topProjects && c.topProjects.length" class="top-projects">
            <span class="top-label">代表项目</span>
            <div
              v-for="p in c.topProjects"
              :key="p.id"
              class="top-project"
              @click="router.push(`/project/${p.id}`)"
            >
              <span class="top-name">{{ p.name }}</span>
              <span class="top-meta">
                <template v-if="p.expertAvgScore > 0">
                  {{ Number(p.expertAvgScore).toFixed(1) }} 分 ·
                </template>
                {{ p.expertReviewCount || 0 }} 条点评
              </span>
            </div>
          </div>

          <footer class="card-foot">
            <div class="foot-tags">
              <el-tag v-if="c.favorited" size="small" type="warning" effect="plain">已收藏</el-tag>
              <el-tag v-if="c.invited" size="small" type="success" effect="plain">已邀约</el-tag>
            </div>
            <div class="foot-ops">
              <el-button
                size="small"
                :disabled="verifyStatus !== 2"
                @click="router.push(`/portfolio/${c.studentId}`)"
              >
                公开作品集
              </el-button>
              <el-button
                size="small"
                type="primary"
                :disabled="verifyStatus !== 2 || remainQuota <= 0"
                @click="router.push(`/company/candidate/${c.studentId}`)"
              >
                <el-icon><View /></el-icon>
                查看完整档案
              </el-button>
            </div>
          </footer>
        </article>
      </div>

      <el-empty
        v-else-if="!loading"
        description="没有找到匹配的候选人，试试放宽筛选条件"
      />

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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { companyApi } from '@/api'
import { CATEGORIES, CATEGORY_TEXT } from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const candidates = ref([])
const total = ref(0)
const onlyVerified = ref(false)
const remainQuota = ref(0)

const currentYear = new Date().getFullYear()
const gradYears = Array.from({ length: 6 }, (_, i) => currentYear + i - 1)

const quickSkills = ['Java', 'SpringBoot', 'Vue', 'Python', 'MySQL', 'Redis', 'Figma', '数据分析']

const query = reactive({
  pageNum: 1,
  pageSize: 9,
  keyword: '',
  majorCategory: '',
  degree: '',
  graduateYear: null,
  skillTags: ''
})

const skillFilter = computed(() =>
  (query.skillTags || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
)

const verifyStatus = computed(() => userStore.companyVerifyStatus)

/** 未认证时隐藏真实姓名，仅展示昵称 */
const displayName = (c) => {
  if (verifyStatus.value !== 2) return c.nickname
  return c.realName || c.nickname
}

const toggleSkill = (tag) => {
  const list = skillFilter.value
  query.skillTags = list.includes(tag)
    ? list.filter((t) => t !== tag).join(',')
    : [...list, tag].join(',')
  reload()
}

const load = async () => {
  loading.value = true
  try {
    const data = await companyApi.searchCandidates(
      {
        keyword: query.keyword || undefined,
        majorCategory: query.majorCategory || undefined,
        degree: query.degree || undefined,
        graduateYear: query.graduateYear || undefined,
        skillTags: query.skillTags || undefined,
        onlyVerified: onlyVerified.value ? 1 : undefined
      },
      { pageNum: query.pageNum, pageSize: query.pageSize }
    )
    candidates.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    candidates.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadQuota = async () => {
  try {
    const s = await companyApi.statistics()
    remainQuota.value = s.remainQuota || 0
  } catch (e) {
    /* 忽略 */
  }
}

const reload = () => {
  query.pageNum = 1
  load()
}

const resetQuery = () => {
  query.keyword = ''
  query.majorCategory = ''
  query.degree = ''
  query.graduateYear = null
  query.skillTags = ''
  onlyVerified.value = false
  reload()
}

const toggleFavorite = async (c) => {
  if (verifyStatus.value !== 2) {
    ElMessage.warning('请先完成企业认证')
    return
  }
  try {
    if (c.favorited) {
      await companyApi.unfavorite(c.studentId)
      ElMessage.success('已取消收藏')
    } else {
      await companyApi.favorite({ studentId: c.studentId, listName: '默认收藏夹' })
      ElMessage.success('已加入收藏夹')
    }
    load()
  } catch (e) {
    /* 已提示 */
  }
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
  max-width: 680px;
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
  font-size: 19px;
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

/* ---------- 筛选 ---------- */
.filter-panel {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 20px 22px;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-row.secondary {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--xz-border-light);
  justify-content: space-between;
}

.keyword-input {
  width: 300px;
}

.filter-select {
  width: 140px;
}

.skill-filter {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
  min-width: 280px;
}

.filter-label {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.skill-option {
  cursor: pointer;
  transition: all 0.18s ease;
}

.skill-manual {
  width: 190px;
}

.result-bar {
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

.result-count strong {
  color: var(--xz-primary);
  font-size: 15px;
}

.result-tip {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

.talent-body {
  min-height: 200px;
}

.candidate-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.candidate-card {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius);
  padding: 18px 20px;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.candidate-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--xz-shadow);
  border-color: #d5e3e2;
}

.card-head {
  display: flex;
  align-items: flex-start;
  gap: 13px;
}

.cand-avatar {
  background: var(--xz-primary);
  flex-shrink: 0;
}

.cand-head-info {
  flex: 1;
  min-width: 0;
}

.cand-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.verified-icon {
  color: var(--xz-success);
  font-size: 15px;
}

.pending-icon {
  color: var(--xz-warning);
  font-size: 14px;
}

.cand-school {
  font-size: 12px;
  color: var(--xz-text-secondary);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cand-degree {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 3px;
}

.cand-bio {
  margin-top: 13px;
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--xz-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.cand-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.skill-pill {
  font-size: 11px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 2px 8px;
  border-radius: 4px;
}

.skill-more {
  font-size: 11px;
  color: var(--xz-text-placeholder);
  background: #eef2f4;
  padding: 2px 8px;
  border-radius: 4px;
}

.cand-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1px;
  margin-top: 14px;
  background: var(--xz-border-light);
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius-sm);
  overflow: hidden;
}

.metric {
  background: #fbfcfc;
  padding: 10px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.metric-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.metric-label {
  font-size: 10.5px;
  color: var(--xz-text-placeholder);
}

.top-projects {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.top-label {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

.top-project {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 7px 11px;
  background: #f7fafa;
  border-radius: var(--xz-radius-sm);
  cursor: pointer;
  transition: background 0.18s ease;
}

.top-project:hover {
  background: var(--xz-primary-lighter);
}

.top-name {
  font-size: 12.5px;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-meta {
  font-size: 11px;
  color: var(--xz-text-placeholder);
  flex-shrink: 0;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: auto;
  padding-top: 14px;
  margin-top: 14px;
  border-top: 1px solid var(--xz-border-light);
  flex-wrap: wrap;
}

.foot-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.foot-ops {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.pagination-wrap {
  margin-top: 26px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1200px) {
  .candidate-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 760px) {
  .candidate-grid {
    grid-template-columns: 1fr;
  }

  .keyword-input,
  .filter-select,
  .skill-manual {
    width: 100%;
  }
}
</style>
