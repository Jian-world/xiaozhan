<template>
  <div class="student-reviews">
    <div class="page-head">
      <div>
        <h2 class="page-title">收到的点评</h2>
        <p class="page-sub">
          点评人给出了逐项评分与具体建议。如果认为某条点评失之偏颇，可以发起申诉，平台会介入复核。
        </p>
      </div>
      <div class="head-actions">
        <el-button plain @click="router.push('/student/projects')">
          <el-icon><Collection /></el-icon>
          管理求点评池
        </el-button>
      </div>
    </div>

    <!-- 概览 -->
    <section class="summary-strip">
      <div class="summary-item">
        <span class="summary-value">{{ total }}</span>
        <span class="summary-label">累计收到点评</span>
      </div>
      <div class="summary-item">
        <span class="summary-value">{{ avgScoreText }}</span>
        <span class="summary-label">平均综合评分</span>
      </div>
      <div class="summary-item">
        <span class="summary-value">{{ thanksCount }}</span>
        <span class="summary-label">已致谢</span>
      </div>
      <div class="summary-item">
        <span class="summary-value">{{ dimensionAvg.length }}</span>
        <span class="summary-label">已获得评价维度</span>
      </div>
    </section>

    <div class="filter-bar">
      <el-radio-group v-model="filter" @change="applyFilter">
        <el-radio-button value="ALL">全部</el-radio-button>
        <el-radio-button value="THANKS_PENDING">未致谢</el-radio-button>
        <el-radio-button value="APPEALED">已申诉</el-radio-button>
      </el-radio-group>
      <span class="result-count">共 {{ filtered.length }} 条</span>
    </div>

    <div v-loading="loading" class="reviews-body">
      <div class="reviews-layout">
        <div class="reviews-main">
          <div v-if="filtered.length" class="review-list">
            <article v-for="r in pagedReviews" :key="r.id" class="review-card">
              <header class="card-head">
                <el-avatar :size="44" :src="r.expertAvatar">
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
                    <span class="dot">·</span>
                    {{ formatDateTime(r.createTime) }}
                  </div>
                </div>
                <div class="card-score">
                  <span class="score-num">{{ Number(r.avgScore || 0).toFixed(1) }}</span>
                  <el-rate :model-value="Number(r.avgScore || 0)" disabled allow-half size="small" />
                </div>
              </header>

              <div class="project-line">
                <el-icon><FolderOpened /></el-icon>
                <router-link :to="`/project/${r.projectId}`" class="project-link">
                  {{ r.projectName }}
                </router-link>
                <el-tag v-if="r.inviteType === 1" size="small" type="warning" effect="plain">
                  求点评池
                </el-tag>
                <el-tag v-else size="small" type="info" effect="plain">定向邀请</el-tag>
              </div>

              <div class="score-row">
                <div v-for="d in SCORE_DIMENSIONS" :key="d.key" class="score-dim">
                  <span class="score-label">{{ d.label }}</span>
                  <div class="dim-bar">
                    <span class="dim-score">{{ r[d.key] || 0 }}</span>
                    <el-rate :model-value="r[d.key] || 0" disabled size="small" />
                  </div>
                </div>
              </div>

              <p class="card-comment">{{ r.comment }}</p>

              <div class="card-details">
                <div v-if="r.strength" class="detail-item is-strength">
                  <span class="detail-label">
                    <el-icon><Select /></el-icon>
                    做得好的地方
                  </span>
                  <p>{{ r.strength }}</p>
                </div>
                <div v-if="r.weakness" class="detail-item is-weakness">
                  <span class="detail-label">
                    <el-icon><Warning /></el-icon>
                    不足之处
                  </span>
                  <p>{{ r.weakness }}</p>
                </div>
                <div v-if="r.suggestion" class="detail-item is-suggestion">
                  <span class="detail-label">
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
                      <span class="reply-time">{{ fromNow(rp.createTime) }}</span>
                    </span>
                    <p class="reply-text">{{ rp.content }}</p>
                  </div>
                </div>
              </div>

              <footer class="card-foot">
                <el-button
                  v-if="r.thanksFlag !== 1"
                  size="small"
                  type="primary"
                  plain
                  @click="onThanks(r)"
                >
                  <el-icon><Star /></el-icon>
                  向点评人致谢
                </el-button>
                <el-tag v-else size="small" type="success" effect="plain">
                  <el-icon><CircleCheckFilled /></el-icon>
                  已致谢
                </el-tag>

                <el-button size="small" text @click="openReply(r)">
                  <el-icon><ChatLineSquare /></el-icon>
                  回应
                </el-button>

                <el-button
                  v-if="!r.appealStatus"
                  size="small"
                  text
                  type="warning"
                  @click="openAppeal(r)"
                >
                  <el-icon><Warning /></el-icon>
                  申诉
                </el-button>
                <el-tag v-else size="small" :type="APPEAL_STATUS[r.appealStatus]?.type" effect="plain">
                  {{ APPEAL_STATUS[r.appealStatus]?.text }}
                </el-tag>

                <el-button size="small" text @click="router.push(`/project/${r.projectId}`)">
                  查看项目
                </el-button>
              </footer>
            </article>
          </div>

          <el-empty v-else-if="!loading" description="还没有收到点评">
            <el-button type="primary" @click="router.push('/student/projects')">
              把项目投进求点评池
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

        <!-- 侧栏：能力画像 -->
        <aside class="reviews-side">
          <div class="side-card">
            <h3 class="side-title">能力雷达</h3>
            <div class="radar-wrap">
              <div ref="radarRef" class="radar-chart"></div>
            </div>
            <p v-if="!dimensionAvg.length" class="radar-empty">积累点评后即可生成能力雷达图</p>
          </div>

          <div class="side-card">
            <h3 class="side-title">各维度得分</h3>
            <div v-if="dimensionAvg.length" class="dim-list">
              <div v-for="d in dimensionAvg" :key="d.key" class="dim-row">
                <div class="dim-head">
                  <span class="dim-name">{{ d.label }}</span>
                  <span class="dim-val">{{ d.value }} / 5</span>
                </div>
                <el-progress
                  :percentage="d.value * 20"
                  :stroke-width="6"
                  :show-text="false"
                  color="var(--xz-primary)"
                />
                <p class="dim-tip">{{ d.tip }}</p>
              </div>
            </div>
            <el-empty v-else description="暂无评分数据" :image-size="60" />
          </div>

          <div class="side-card">
            <h3 class="side-title">收到点评最多的项目</h3>
            <div v-if="topProjects.length" class="top-list">
              <div
                v-for="p in topProjects"
                :key="p.projectId"
                class="top-item"
                @click="router.push(`/project/${p.projectId}`)"
              >
                <span class="top-name">{{ p.projectName }}</span>
                <span class="top-count">{{ p.count }} 条</span>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="60" />
          </div>
        </aside>
      </div>
    </div>

    <!-- 回应弹窗 -->
    <el-dialog v-model="replyVisible" title="回应点评" width="560px" destroy-on-close>
      <p class="dialog-hint">
        回应会展示在该条点评下方，用于补充说明或说明后续改进。请保持礼貌与客观。
      </p>
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="5"
        maxlength="500"
        show-word-limit
        placeholder="例如：感谢指出，我们已经补充了全局异常处理，并把订单表的索引做了优化。"
      />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="submitReply">提交回应</el-button>
      </template>
    </el-dialog>

    <!-- 申诉弹窗 -->
    <el-dialog v-model="appealVisible" title="发起点评申诉" width="600px" destroy-on-close>
      <el-alert type="warning" :closable="false" show-icon class="appeal-alert">
        <template #title>申诉前请确认</template>
        <template #default>
          申诉成立后该点评会被隐藏并移出项目均分。请仅在对评分明显失当、或点评人未认真阅读作品时使用，
          滥用申诉会影响你的信用记录。
        </template>
      </el-alert>

      <p class="appeal-target">
        申诉对象：<strong>{{ appealTarget?.expertName }}</strong> 对「{{ appealTarget?.projectName }}」的点评
      </p>

      <el-form label-position="top">
        <el-form-item label="申诉理由" required>
          <el-input
            v-model="appealReason"
            type="textarea"
            :rows="5"
            maxlength="600"
            show-word-limit
            placeholder="请具体说明理由，例如：该点评提到的「未做参数校验」在源码包的 validator 包中已有完整实现，见 README 第 3 节。"
          />
        </el-form-item>

        <el-form-item label="证据材料（可选）">
          <el-upload
            :show-file-list="false"
            accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.zip"
            :before-upload="beforeEvidence"
            :http-request="doEvidenceUpload"
          >
            <el-button :loading="evidenceUploading">
              <el-icon><Upload /></el-icon>
              上传证据
            </el-button>
          </el-upload>
          <div v-if="appealEvidence" class="uploaded-file">
            <el-icon><Document /></el-icon>
            <a :href="appealEvidence" target="_blank" rel="noopener">已上传，点击查看</a>
            <el-button size="small" text type="danger" @click="appealEvidence = ''">移除</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="appealVisible = false">取消</el-button>
        <el-button type="primary" :loading="appealing" @click="submitAppeal">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { fileApi, reviewApi } from '@/api'
import {
  APPEAL_STATUS,
  EXPERT_LEVEL_TEXT,
  EXPERT_LEVEL_TYPE,
  EXPERT_TYPE_TEXT,
  ROLE_TEXT,
  SCORE_DIMENSIONS,
  formatDateTime,
  fromNow
} from '@/utils/dict'

const router = useRouter()

const loading = ref(true)
const allReviews = ref([])
const filter = ref('ALL')
const pageNum = ref(1)
const pageSize = ref(5)

const radarRef = ref(null)
let radarChart = null

/* ---------- 派生数据 ---------- */
const filtered = computed(() => {
  const list = allReviews.value
  if (filter.value === 'THANKS_PENDING') return list.filter((r) => r.thanksFlag !== 1)
  if (filter.value === 'APPEALED') return list.filter((r) => r.appealStatus && r.appealStatus > 0)
  return list
})

const pagedReviews = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

const total = computed(() => allReviews.value.length)

const avgScoreText = computed(() => {
  const scores = allReviews.value.map((r) => Number(r.avgScore || 0)).filter((s) => s > 0)
  if (!scores.length) return '—'
  return (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
})

const thanksCount = computed(() => allReviews.value.filter((r) => r.thanksFlag === 1).length)

const dimensionAvg = computed(() => {
  if (!allReviews.value.length) return []
  const sum = { scoreCompletion: 0, scoreNormative: 0, scoreInnovation: 0, scoreTechnical: 0 }
  allReviews.value.forEach((r) => {
    SCORE_DIMENSIONS.forEach((d) => {
      sum[d.key] += r[d.key] || 0
    })
  })
  const n = allReviews.value.length
  return SCORE_DIMENSIONS.map((d) => {
    const value = Math.round((sum[d.key] / n) * 10) / 10
    return {
      ...d,
      value,
      tip:
        value >= 4.5
          ? '这一项表现突出，可作为作品集的亮点来讲。'
          : value >= 3.5
            ? '表现良好，继续打磨细节会更出色。'
            : value >= 2.5
              ? '基本达标，是这个项目最值得投入改进的方向。'
              : '明显短板，建议优先补强后再投递。'
    }
  })
})

const topProjects = computed(() => {
  const counter = {}
  allReviews.value.forEach((r) => {
    if (!r.projectName) return
    const key = `${r.projectId}`
    if (!counter[key]) counter[key] = { projectId: r.projectId, projectName: r.projectName, count: 0 }
    counter[key].count += 1
  })
  return Object.values(counter).sort((a, b) => b.count - a.count).slice(0, 5)
})

const applyFilter = () => {
  pageNum.value = 1
}

/* ---------- 加载 ---------- */
const load = async () => {
  loading.value = true
  try {
    const data = await reviewApi.received({ pageNum: 1, pageSize: 200 })
    allReviews.value = data.records || []
  } catch (e) {
    allReviews.value = []
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

/* ---------- 致谢 ---------- */
const onThanks = async (r) => {
  try {
    await reviewApi.thanks(r.id)
    ElMessage.success('已向点评人致谢')
    load()
  } catch (e) {
    /* 已提示 */
  }
}

/* ---------- 回应 ---------- */
const replyVisible = ref(false)
const replying = ref(false)
const replyContent = ref('')
const replyTarget = ref(null)

const openReply = (r) => {
  replyTarget.value = r
  replyContent.value = ''
  replyVisible.value = true
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请填写回应内容')
    return
  }
  replying.value = true
  try {
    await reviewApi.reply({ reviewId: replyTarget.value.id, content: replyContent.value.trim() })
    ElMessage.success('回应已提交')
    replyVisible.value = false
    load()
  } catch (e) {
    /* 已提示 */
  } finally {
    replying.value = false
  }
}

/* ---------- 申诉 ---------- */
const appealVisible = ref(false)
const appealing = ref(false)
const appealReason = ref('')
const appealEvidence = ref('')
const appealTarget = ref(null)
const evidenceUploading = ref(false)

const openAppeal = (r) => {
  appealTarget.value = r
  appealReason.value = ''
  appealEvidence.value = ''
  appealVisible.value = true
}

const beforeEvidence = (file) => {
  if (file.size / 1024 / 1024 > 20) {
    ElMessage.error('证据材料不超过 20MB')
    return false
  }
  return true
}

const doEvidenceUpload = async (options) => {
  evidenceUploading.value = true
  try {
    const res = await fileApi.upload(options.file, 'DOC')
    appealEvidence.value = res.url
    ElMessage.success('证据已上传')
  } catch (e) {
    /* 已提示 */
  } finally {
    evidenceUploading.value = false
  }
}

const submitAppeal = async () => {
  if (appealReason.value.trim().length < 20) {
    ElMessage.warning('申诉理由请至少填写 20 字，说明具体依据')
    return
  }
  appealing.value = true
  try {
    await reviewApi.appeal({
      reviewId: appealTarget.value.id,
      reason: appealReason.value.trim(),
      evidenceUrl: appealEvidence.value || undefined
    })
    ElMessage.success('申诉已提交，平台会尽快复核')
    appealVisible.value = false
    load()
  } catch (e) {
    /* 已提示 */
  } finally {
    appealing.value = false
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
  max-width: 680px;
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

.reviews-body {
  min-height: 200px;
}

.reviews-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 22px 26px;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 13px;
}

.reviewer {
  flex: 1;
  min-width: 0;
}

.reviewer-name {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 15px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.reviewer-org {
  font-size: 12px;
  color: var(--xz-text-secondary);
  margin-top: 3px;
}

.dot {
  opacity: 0.6;
}

.card-score {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}

.score-num {
  font-size: 24px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.project-line {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 15px;
  padding: 9px 14px;
  background: #f8fbfb;
  border-radius: var(--xz-radius-sm);
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.project-link {
  font-weight: 600;
  color: var(--xz-primary);
}

.project-link:hover {
  text-decoration: underline;
}

.score-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px 20px;
  margin-top: 15px;
  padding: 14px 18px;
  background: #fbfcfc;
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius-sm);
}

.score-dim {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.score-label {
  font-size: 12px;
  color: var(--xz-text-placeholder);
}

.dim-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dim-score {
  font-size: 15px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
  min-width: 12px;
}

.card-comment {
  margin-top: 16px;
  font-size: 14px;
  line-height: 1.9;
  color: var(--xz-text-primary);
  white-space: pre-wrap;
}

.card-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 15px;
}

.detail-item {
  padding: 12px 16px;
  border-radius: var(--xz-radius-sm);
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  font-weight: 600;
}

.detail-item p {
  margin-top: 7px;
  font-size: 13px;
  line-height: 1.85;
  color: var(--xz-text-primary);
  white-space: pre-wrap;
}

.is-strength {
  background: #f0f8f3;
}

.is-strength .detail-label {
  color: var(--xz-success);
}

.is-weakness {
  background: #fdf4f4;
}

.is-weakness .detail-label {
  color: var(--xz-danger);
}

.is-suggestion {
  background: var(--xz-accent-light);
}

.is-suggestion .detail-label {
  color: #b0642c;
}

.reply-list {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed var(--xz-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
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

.reply-time {
  font-weight: 400;
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

.reply-text {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--xz-text-secondary);
  white-space: pre-wrap;
}

.card-foot {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--xz-border-light);
  flex-wrap: wrap;
}

.pagination-wrap {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}

/* ---------- 侧栏 ---------- */
.reviews-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 84px;
}

.side-card {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 18px 20px;
}

.side-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
  margin-bottom: 14px;
  padding-left: 9px;
  border-left: 3px solid var(--xz-primary);
  line-height: 1.3;
}

.radar-wrap {
  width: 100%;
  height: 220px;
}

.radar-chart {
  width: 100%;
  height: 100%;
}

.radar-empty {
  font-size: 11.5px;
  text-align: center;
  color: var(--xz-text-placeholder);
  line-height: 1.7;
  margin-top: 6px;
}

.dim-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.dim-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 6px;
}

.dim-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--xz-text-primary);
}

.dim-val {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
}

.dim-tip {
  margin-top: 6px;
  font-size: 11px;
  line-height: 1.6;
  color: var(--xz-text-placeholder);
}

.top-list {
  display: flex;
  flex-direction: column;
}

.top-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 4px;
  cursor: pointer;
  border-radius: var(--xz-radius-sm);
  transition: background 0.18s ease;
}

.top-item:hover {
  background: var(--xz-bg-hover);
}

.top-item + .top-item {
  border-top: 1px solid var(--xz-border-light);
}

.top-name {
  font-size: 13px;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-count {
  font-size: 12px;
  color: var(--xz-text-placeholder);
  flex-shrink: 0;
}

.dialog-hint {
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--xz-text-secondary);
  margin-bottom: 12px;
}

.appeal-alert {
  margin-bottom: 16px;
  border-radius: var(--xz-radius-sm);
}

.appeal-target {
  font-size: 13px;
  color: var(--xz-text-secondary);
  margin-bottom: 16px;
}

.uploaded-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 8px 14px;
  background: #f0f8f3;
  border-radius: var(--xz-radius-sm);
  font-size: 12.5px;
}

.uploaded-file a {
  color: var(--xz-primary);
  text-decoration: underline;
}

@media (max-width: 1080px) {
  .reviews-layout {
    grid-template-columns: 1fr;
  }

  .reviews-side {
    position: static;
  }

  .summary-strip {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 680px) {
  .score-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .review-card {
    padding: 18px;
  }
}
</style>
