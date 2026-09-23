<template>
  <div v-loading="loading" class="review-submit-page">
    <div class="page-head">
      <div class="head-left">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div>
          <h2 class="page-title">撰写点评</h2>
          <p class="page-sub">
            点评将展示在项目详情页与学生的作品集中，请尽量给出具体、可执行的建议。
          </p>
        </div>
      </div>

      <div class="head-actions">
        <el-button @click="router.push(`/project/${projectId}`)">
          <el-icon><View /></el-icon>
          查看项目全文
        </el-button>
        <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="onSubmit">
          提交点评
        </el-button>
      </div>
    </div>

    <div v-if="project" class="submit-body">
      <div class="submit-main">
        <!-- 被点评项目速览 -->
        <section class="form-block project-brief">
          <div class="brief-head">
            <div class="brief-cover" :style="coverStyle">
              <img v-if="project.coverUrl" :src="project.coverUrl" :alt="project.name" />
              <el-icon v-else :size="22"><Files /></el-icon>
            </div>
            <div class="brief-info">
              <h3 class="brief-name">{{ project.name }}</h3>
              <p class="brief-summary">{{ project.summary || '作者暂未填写简介' }}</p>
              <div class="brief-tags">
                <el-tag size="small" effect="plain">
                  {{ CATEGORY_TEXT[project.category] || '未分类' }}
                </el-tag>
                <el-tag size="small" type="info" effect="plain">
                  {{ PROJECT_TYPE_TEXT[project.projectType] || '其他' }}
                </el-tag>
                <el-tag v-if="project.inReviewPool === 1" size="small" type="warning" effect="plain">
                  主动求点评
                </el-tag>
              </div>
            </div>
          </div>

          <div v-if="techList.length" class="brief-tech">
            <span v-for="t in techList" :key="t" class="tech-pill">{{ t }}</span>
          </div>

          <div v-if="project.highlight" class="brief-highlight">
            <span class="highlight-label">作者自述的技术亮点</span>
            <div class="xz-rich-text highlight-content" v-html="project.highlight"></div>
          </div>
          <el-alert
            v-else
            type="info"
            :closable="false"
            show-icon
            title="作者尚未填写技术难点说明，建议先阅读源码包与文档再评分"
            class="brief-alert"
          />

          <div v-if="assetSummary.length" class="brief-assets">
            <span v-for="g in assetSummary" :key="g.type" class="asset-chip">
              <el-icon><component :is="ASSET_TYPE_ICON[g.type]" /></el-icon>
              {{ ASSET_TYPE_TEXT[g.type] }}
              <span class="asset-count">{{ g.count }}</span>
            </span>
          </div>
        </section>

        <!-- 四维评分 -->
        <section class="form-block">
          <div class="block-head">
            <h3 class="block-title">四维评分</h3>
            <span class="block-note">每项 1-5 分，综合分由四项自动取平均</span>
          </div>

          <div class="dimension-list">
            <div v-for="d in SCORE_DIMENSIONS" :key="d.key" class="dimension-row">
              <div class="dim-copy">
                <span class="dim-label">{{ d.label }}</span>
                <span class="dim-hint">{{ d.hint }}</span>
              </div>
              <div class="dim-input">
                <el-rate
                  v-model="form[d.key]"
                  :max="5"
                  show-score
                  :score-template="scoreTemplate(d.key)"
                  class="dim-rate"
                />
              </div>
            </div>
          </div>

          <div class="avg-preview">
            <span class="avg-label">综合评分</span>
            <span class="avg-value">{{ avgScore }}</span>
            <span class="avg-text">{{ scoreText(avgScore) }}</span>
          </div>
        </section>

        <!-- 总评 -->
        <section class="form-block">
          <div class="block-head">
            <h3 class="block-title">总评</h3>
            <span class="block-note" :class="{ 'is-warn': commentLen < minCommentLength }">
              {{ commentLen }} / {{ minCommentLength }} 字{{ commentLen < minCommentLength ? '（未达标）' : '' }}
            </span>
          </div>
          <p class="block-desc">
            说清这个项目解决了什么问题、实现到哪一步、你判断的依据是什么。
            避免「整体不错，继续加油」这类无信息量的评语。
          </p>
          <el-input
            v-model="form.comment"
            type="textarea"
            :rows="7"
            :maxlength="2000"
            show-word-limit
            placeholder="例如：项目完整实现了二手交易的发布、搜索与订单闭环，鉴权用 JWT 无状态方案比较合理。但在并发下单场景没有做幂等校验，压测时出现重复扣减库存，建议引入分布式锁或乐观锁 + 唯一索引兜底。"
          />
        </section>

        <!-- 优缺点 -->
        <section class="form-block">
          <div class="block-head">
            <h3 class="block-title">亮点 / 不足 / 建议</h3>
            <span class="block-note">填写得越具体，学生越容易定位改进方向</span>
          </div>

          <el-form label-position="top" class="parts-form">
            <el-form-item>
              <template #label>
                <span class="part-label is-strength">
                  <el-icon><Select /></el-icon>
                  做得好的地方
                </span>
              </template>
              <el-input
                v-model="form.strength"
                type="textarea"
                :rows="3"
                maxlength="600"
                show-word-limit
                placeholder="如：分层清晰，service 与 controller 职责边界明确；文档附了完整的接口说明与 ER 图。"
              />
            </el-form-item>

            <el-form-item>
              <template #label>
                <span class="part-label is-weakness">
                  <el-icon><Warning /></el-icon>
                  不足之处
                </span>
              </template>
              <el-input
                v-model="form.weakness"
                type="textarea"
                :rows="3"
                maxlength="600"
                show-word-limit
                placeholder="如：缺少全局异常处理与参数校验；部分 SQL 未走索引；没有单元测试。"
              />
            </el-form-item>

            <el-form-item>
              <template #label>
                <span class="part-label is-suggestion">
                  <el-icon><Opportunity /></el-icon>
                  改进建议
                </span>
              </template>
              <el-input
                v-model="form.suggestion"
                type="textarea"
                :rows="3"
                maxlength="600"
                show-word-limit
                placeholder="如：引入 @RestControllerAdvice 统一处理异常；为订单表加 (user_id, item_id) 联合唯一索引；补充 service 层单测。"
              />
            </el-form-item>
          </el-form>
        </section>
      </div>

      <!-- 侧栏 -->
      <aside class="submit-side">
        <div class="side-card">
          <h4 class="side-title">点评须知</h4>
          <ul class="rule-list">
            <li>
              <strong>评语至少 {{ minCommentLength }} 字</strong>
              <span>平台会校验评语信息量，过短会被判定为无效点评。</span>
            </li>
            <li>
              <strong>同一项目 30 天内仅可点评一次</strong>
              <span>如需补充意见，可在原点评下追问或等待冷却期结束。</span>
            </li>
            <li>
              <strong>四项评分完全一致 + 无优缺点拆分 + 评语过短</strong>
              <span>会触发嫌疑敷衍标记，该点评不计入项目均分。</span>
            </li>
            <li>
              <strong>学生可对点评发起申诉</strong>
              <span>申诉成立后该点评将被隐藏并移出计分，请客观评分。</span>
            </li>
          </ul>
        </div>

        <div class="side-card">
          <h4 class="side-title">我的点评额度</h4>
          <div class="quota-row">
            <span class="quota-value">{{ todayReviews }} / {{ dailyQuota }}</span>
            <span class="quota-label">今日已点评</span>
          </div>
          <el-progress
            :percentage="quotaPercent"
            :stroke-width="7"
            :show-text="false"
            :color="quotaColor"
          />
          <p class="quota-tip">
            每日额度按平台规则发放，用完次日重置。保持稳定输出有助于提升点评质量分与等级。
          </p>
        </div>

        <div class="side-card">
          <h4 class="side-title">当前得分</h4>
          <ScoreBlock :review="form" :count="1" :show-hints="false" />
        </div>

        <div class="side-card side-publish">
          <div class="publish-head">
            <span class="side-title no-border">公开展示</span>
            <el-switch v-model="isPublic" active-text="公开" inactive-text="仅作者可见" inline-prompt />
          </div>
          <p class="publish-tip">
            公开后，点评会展示在项目详情页与学生的公开作品集中，可作为你专业背书的记录。
            选择不公开时，仅项目作者本人与平台运营可见。
          </p>
        </div>
      </aside>
    </div>

    <el-empty v-else-if="!loading" description="项目不存在或已被删除">
      <el-button @click="router.push('/expert/pool')">返回求点评池</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ScoreBlock from '@/components/ScoreBlock.vue'
import { projectApi, reviewApi } from '@/api'
import {
  ASSET_TYPE_ICON,
  ASSET_TYPE_TEXT,
  CATEGORY_TEXT,
  PROJECT_TYPE_TEXT,
  SCORE_DIMENSIONS,
  scoreText
} from '@/utils/dict'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const project = ref(null)
const todayReviews = ref(0)
const dailyQuota = ref(0)
const isPublic = ref(true)

const minCommentLength = 50

const projectId = computed(() => Number(route.params.projectId))

const form = reactive({
  projectId: null,
  scoreCompletion: 0,
  scoreNormative: 0,
  scoreInnovation: 0,
  scoreTechnical: 0,
  comment: '',
  strength: '',
  weakness: '',
  suggestion: '',
  isPublic: 1
})

const techList = computed(() => {
  const t = project.value?.techStack
  if (Array.isArray(t)) return t
  if (typeof t === 'string' && t) return t.split(',').map((s) => s.trim()).filter(Boolean)
  return []
})

const coverStyle = computed(() => {
  const palette = ['#e6f2f1', '#fdf0e7', '#eef2f4', '#f3eef7']
  return { background: palette[(project.value?.id || 0) % palette.length] }
})

const assetSummary = computed(() => {
  const assets = project.value?.assets || []
  return ['SOURCE', 'DOC', 'VIDEO', 'IMAGE']
    .map((type) => ({ type, count: assets.filter((a) => a.assetType === type).length }))
    .filter((g) => g.count > 0)
})

const commentLen = computed(() => (form.comment || '').replace(/\s/g, '').length)

const allScored = computed(() => SCORE_DIMENSIONS.every((d) => Number(form[d.key]) > 0))

const avgScore = computed(() => {
  if (!allScored.value) return 0
  const sum = SCORE_DIMENSIONS.reduce((acc, d) => acc + Number(form[d.key] || 0), 0)
  return Math.round((sum / SCORE_DIMENSIONS.length) * 10) / 10
})

const canSubmit = computed(() => allScored.value && commentLen.value >= minCommentLength)

const quotaPercent = computed(() => {
  if (!dailyQuota.value) return 0
  return Math.min(100, Math.round((todayReviews.value / dailyQuota.value) * 100))
})

const quotaColor = computed(() =>
  quotaPercent.value >= 100 ? '#cc4b4b' : quotaPercent.value >= 80 ? '#d99a2b' : '#1f6f6b'
)

const scoreTemplate = (key) => {
  const v = Number(form[key] || 0)
  return `${v} 分 · ${scoreText(v)}`
}

/** 提示：四项相同且未拆优缺点时的敷衍风险 */
const sloppyRisk = computed(() => {
  const values = SCORE_DIMENSIONS.map((d) => Number(form[d.key] || 0))
  const allSame = values.every((v) => v > 0 && v === values[0])
  const noDetail = !form.strength?.trim() && !form.weakness?.trim()
  return allSame && noDetail && commentLen.value < 80
})

const load = async () => {
  loading.value = true
  try {
    const [detail, stats] = await Promise.all([
      projectApi.detail(projectId.value),
      reviewApi.statistics().catch(() => null)
    ])
    project.value = detail
    form.projectId = detail.id
    // 本人项目不可点评
    if (detail.owner) {
      ElMessage.warning('不能点评自己的项目')
      router.replace('/expert/pool')
      return
    }
    if (stats) {
      todayReviews.value = stats.todayReviews || 0
      dailyQuota.value = stats.dailyQuota || 0
    }
  } catch (e) {
    project.value = null
  } finally {
    loading.value = false
  }
}

const onSubmit = async () => {
  if (!allScored.value) {
    ElMessage.warning('请先完成四项评分')
    return
  }
  if (commentLen.value < minCommentLength) {
    ElMessage.warning(`总评至少需要 ${minCommentLength} 字`)
    return
  }

  if (sloppyRisk.value) {
    try {
      await ElMessageBox.confirm(
        '当前四项评分完全相同、未填写亮点与不足、且总评较短，可能被系统标记为「嫌疑敷衍」而不计入项目均分。确定继续提交吗？',
        '点评质量提醒',
        { type: 'warning', confirmButtonText: '仍然提交', cancelButtonText: '返回修改' }
      )
    } catch (e) {
      return
    }
  }

  submitting.value = true
  try {
    form.isPublic = isPublic.value ? 1 : 0
    await reviewApi.submit({ ...form })
    ElMessage.success('点评已提交，感谢你的专业输出')
    router.push('/expert/history')
  } catch (e) {
    /* 已提示 */
  } finally {
    submitting.value = false
  }
}

watch(() => route.params.projectId, load)
onMounted(load)
</script>

<style scoped>
.review-submit-page {
  min-height: 60vh;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.head-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--xz-text-primary);
}

.page-sub {
  margin-top: 5px;
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  max-width: 620px;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.submit-body {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

.form-block {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 22px 26px;
  margin-bottom: 18px;
}

.block-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--xz-text-primary);
  padding-left: 10px;
  border-left: 3px solid var(--xz-primary);
  line-height: 1.3;
}

.block-note {
  font-size: 12px;
  color: var(--xz-text-placeholder);
}

.block-note.is-warn {
  color: var(--xz-danger);
}

.block-desc {
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--xz-text-secondary);
  margin-bottom: 14px;
}

/* ---------- 项目速览 ---------- */
.project-brief {
  background: linear-gradient(150deg, #f9fcfc 0%, #ffffff 60%);
}

.brief-head {
  display: flex;
  gap: 15px;
}

.brief-cover {
  width: 96px;
  height: 72px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--xz-primary);
  overflow: hidden;
  flex-shrink: 0;
}

.brief-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.brief-info {
  flex: 1;
  min-width: 0;
}

.brief-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--xz-text-primary);
  line-height: 1.4;
}

.brief-summary {
  margin-top: 5px;
  font-size: 12.5px;
  line-height: 1.65;
  color: var(--xz-text-secondary);
}

.brief-tags {
  display: flex;
  gap: 7px;
  margin-top: 9px;
  flex-wrap: wrap;
}

.brief-tech {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed var(--xz-border);
}

.tech-pill {
  font-size: 12px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 3px 10px;
  border-radius: 20px;
}

.brief-highlight {
  margin-top: 15px;
  padding: 14px 16px;
  background: #f7fafa;
  border-radius: var(--xz-radius-sm);
}

.highlight-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--xz-text-secondary);
  margin-bottom: 9px;
}

.highlight-content {
  font-size: 13px;
  max-height: 320px;
  overflow-y: auto;
}

.brief-alert {
  margin-top: 14px;
  border-radius: var(--xz-radius-sm);
}

.brief-assets {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
  margin-top: 15px;
}

.asset-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--xz-text-secondary);
  background: #f7fafa;
  border: 1px solid var(--xz-border-light);
  padding: 4px 11px;
  border-radius: 6px;
}

.asset-count {
  font-weight: 600;
  color: var(--xz-primary);
}

/* ---------- 评分 ---------- */
.dimension-list {
  display: flex;
  flex-direction: column;
}

.dimension-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 15px 0;
}

.dimension-row + .dimension-row {
  border-top: 1px solid var(--xz-border-light);
}

.dim-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.dim-label {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.dim-hint {
  font-size: 12px;
  color: var(--xz-text-placeholder);
  margin-top: 3px;
}

.dim-input {
  flex-shrink: 0;
}

.dim-rate :deep(.el-rate__text) {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.avg-preview {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-top: 18px;
  padding: 16px 20px;
  background: var(--xz-primary-lighter);
  border-radius: var(--xz-radius);
}

.avg-label {
  font-size: 13.5px;
  color: var(--xz-text-secondary);
}

.avg-value {
  font-size: 30px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.avg-text {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--xz-primary-dark);
}

/* ---------- 优缺点 ---------- */
.parts-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.parts-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.part-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13.5px;
  font-weight: 600;
}

.part-label.is-strength {
  color: var(--xz-success);
}

.part-label.is-weakness {
  color: var(--xz-danger);
}

.part-label.is-suggestion {
  color: #b0642c;
}

/* ---------- 侧栏 ---------- */
.submit-side {
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

.side-title.no-border {
  border-left: none;
  padding-left: 0;
  margin-bottom: 0;
}

.rule-list {
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.rule-list li {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.rule-list strong {
  font-size: 12.5px;
  color: var(--xz-text-primary);
  font-weight: 600;
}

.rule-list span {
  font-size: 11.5px;
  line-height: 1.65;
  color: var(--xz-text-secondary);
}

.quota-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.quota-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--xz-text-primary);
  font-variant-numeric: tabular-nums;
}

.quota-label {
  font-size: 12px;
  color: var(--xz-text-secondary);
}

.quota-tip {
  margin-top: 11px;
  font-size: 11.5px;
  line-height: 1.65;
  color: var(--xz-text-placeholder);
}

.side-card :deep(.score-block .overall-value) {
  font-size: 32px;
}

.side-publish .publish-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 11px;
}

.publish-tip {
  font-size: 11.5px;
  line-height: 1.7;
  color: var(--xz-text-secondary);
}

@media (max-width: 1080px) {
  .submit-body {
    grid-template-columns: 1fr;
  }

  .submit-side {
    position: static;
  }
}

@media (max-width: 680px) {
  .dimension-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .brief-head {
    flex-direction: column;
  }

  .form-block {
    padding: 18px;
  }
}
</style>
