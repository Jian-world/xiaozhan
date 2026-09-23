<template>
  <div class="expert-history">
    <div class="page-head">
      <div>
        <h2 class="page-title">点评记录</h2>
        <p class="page-sub">
          这里沉淀了你给出的每一条点评。被学生致谢、被判为高质量的点评会提升你的质量分与等级。
        </p>
      </div>
      <el-button type="primary" :disabled="verifyStatus !== 2" @click="router.push('/expert/pool')">
        <el-icon><Plus /></el-icon>
        去点评新项目
      </el-button>
    </div>

    <section class="metric-strip">
      <div v-for="m in metrics" :key="m.label" class="metric-item">
        <span class="metric-value">{{ m.value }}</span>
        <span class="metric-label">{{ m.label }}</span>
      </div>
    </section>

    <div class="filter-bar">
      <el-radio-group v-model="filter" @change="applyFilter">
        <el-radio-button value="ALL">全部点评</el-radio-button>
        <el-radio-button value="THANKS">已获致谢</el-radio-button>
        <el-radio-button value="PUBLIC">公开点评</el-radio-button>
        <el-radio-button value="SUSPECT">嫌疑敷衍</el-radio-button>
      </el-radio-group>
      <span class="result-count">共 {{ total }} 条记录</span>
    </div>

    <div v-loading="loading" class="history-body">
      <div v-if="reviews.length" class="review-list">
        <article v-for="r in reviews" :key="r.id" class="review-card">
          <header class="card-head">
            <div class="head-left">
              <router-link :to="`/project/${r.projectId}`" class="project-link">
                <el-icon><FolderOpened /></el-icon>
                {{ r.projectName }}
              </router-link>
              <span class="head-time">{{ formatDateTime(r.createTime) }}</span>
            </div>
            <div class="head-right">
              <el-tag v-if="r.thanksFlag === 1" size="small" type="success" effect="plain">
                <el-icon><Star /></el-icon>
                已获致谢
              </el-tag>
              <el-tag v-if="r.qualityStatus === 1" size="small" type="warning" effect="plain">
                嫌疑敷衍
              </el-tag>
              <el-tag v-if="r.isPublic === 0" size="small" type="info" effect="plain">
                未公开
              </el-tag>
              <el-tag
                v-if="r.appealStatus === 1"
                size="small"
                type="warning"
                effect="plain"
              >
                申诉中
              </el-tag>
              <el-tag v-else-if="r.appealStatus === 2" size="small" type="danger" effect="plain">
                申诉成立
              </el-tag>
              <span class="card-score">{{ Number(r.avgScore || 0).toFixed(1) }}</span>
            </div>
          </header>

          <div class="score-row">
            <div v-for="d in SCORE_DIMENSIONS" :key="d.key" class="score-dim">
              <span class="score-label">{{ d.label }}</span>
              <el-rate :model-value="r[d.key] || 0" disabled size="small" />
            </div>
          </div>

          <p class="card-comment">{{ r.comment }}</p>

          <div v-if="r.strength || r.weakness || r.suggestion" class="card-details">
            <details v-if="r.strength" class="detail-item is-strength">
              <summary>
                <el-icon><Select /></el-icon>
                做得好的地方
              </summary>
              <p>{{ r.strength }}</p>
            </details>
            <details v-if="r.weakness" class="detail-item is-weakness">
              <summary>
                <el-icon><Warning /></el-icon>
                不足之处
              </summary>
              <p>{{ r.weakness }}</p>
            </details>
            <details v-if="r.suggestion" class="detail-item is-suggestion">
              <summary>
                <el-icon><Opportunity /></el-icon>
                改进建议
              </summary>
              <p>{{ r.suggestion }}</p>
            </details>
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
            <el-button size="small" text type="primary" @click="openReply(r)">
              <el-icon><ChatLineSquare /></el-icon>
              追加回应
            </el-button>
            <el-button size="small" text @click="router.push(`/project/${r.projectId}`)">
              查看项目
            </el-button>
          </footer>
        </article>
      </div>

      <el-empty v-else-if="!loading" description="还没有点评记录">
        <el-button
          v-if="verifyStatus === 2"
          type="primary"
          @click="router.push('/expert/pool')"
        >
          领取第一个项目
        </el-button>
        <el-button v-else @click="router.push('/expert/profile')">先完成专家认证</el-button>
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

    <!-- 回应弹窗 -->
    <el-dialog v-model="replyVisible" title="追加回应" width="560px" destroy-on-close>
      <p class="reply-target">
        对「{{ replyTarget?.projectName }}」的点评补充说明：
      </p>
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="5"
        maxlength="500"
        show-word-limit
        placeholder="例如：补充一点，这里也可以用消息队列削峰，具体要看业务量级。"
      />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="submitReply">提交回应</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { reviewApi } from '@/api'
import {
  ROLE_TEXT,
  SCORE_DIMENSIONS,
  formatDateTime,
  fromNow
} from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const reviews = ref([])
const total = ref(0)
const stats = ref({
  totalReviews: 0,
  todayReviews: 0,
  dailyQuota: 0,
  avgGivenScore: 0,
  thanksCount: 0,
  level: 'BRONZE',
  points: 0
})

const query = reactive({ pageNum: 1, pageSize: 8 })
const filter = ref('ALL')

const verifyStatus = computed(() => userStore.expertVerifyStatus)

const metrics = computed(() => [
  { label: '累计点评', value: stats.value.totalReviews || 0 },
  { label: '给出均分', value: stats.value.avgGivenScore > 0 ? Number(stats.value.avgGivenScore).toFixed(1) : '—' },
  { label: '收到致谢', value: stats.value.thanksCount || 0 },
  { label: '今日已点评', value: `${stats.value.todayReviews || 0} / ${stats.value.dailyQuota || 0}` },
  { label: '累计积分', value: stats.value.points || 0 }
])

/** 前端筛选：接口返回全部本人点评，按需过滤 */
const allReviews = ref([])

const applyFilter = () => {
  const list = allReviews.value
  const filtered =
    filter.value === 'ALL'
      ? list
      : filter.value === 'THANKS'
        ? list.filter((r) => r.thanksFlag === 1)
        : filter.value === 'PUBLIC'
          ? list.filter((r) => r.isPublic === 1)
          : list.filter((r) => r.qualityStatus === 1)
  reviews.value = filtered
  total.value = filtered.length
}

const load = async () => {
  loading.value = true
  try {
    const [page, s] = await Promise.all([
      reviewApi.my({ pageNum: 1, pageSize: 200 }),
      reviewApi.statistics().catch(() => null)
    ])
    allReviews.value = page.records || []
    if (s) stats.value = { ...stats.value, ...s }
    applyFilter()
  } catch (e) {
    allReviews.value = []
    reviews.value = []
  } finally {
    loading.value = false
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
  max-width: 640px;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1px;
  background: var(--xz-border-light);
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius);
  overflow: hidden;
  margin-bottom: 18px;
}

.metric-item {
  background: #ffffff;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.metric-value {
  font-size: 21px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.metric-label {
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

.history-body {
  min-height: 200px;
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
  padding: 20px 24px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.project-link {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 15px;
  font-weight: 600;
  color: var(--xz-text-primary);
  transition: color 0.18s ease;
}

.project-link:hover {
  color: var(--xz-primary);
}

.head-time {
  font-size: 12px;
  color: var(--xz-text-placeholder);
}

.head-right {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-wrap: wrap;
}

.card-score {
  font-size: 21px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
  margin-left: 4px;
}

.score-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px 20px;
  margin-top: 14px;
  padding: 12px 16px;
  background: #f8fbfb;
  border-radius: var(--xz-radius-sm);
}

.score-dim {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.score-label {
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.card-comment {
  margin-top: 14px;
  font-size: 13.5px;
  line-height: 1.9;
  color: var(--xz-text-primary);
  white-space: pre-wrap;
}

.card-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 13px;
}

.detail-item {
  border-radius: var(--xz-radius-sm);
  padding: 10px 14px;
}

.detail-item summary {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  list-style: none;
}

.detail-item summary::-webkit-details-marker {
  display: none;
}

.detail-item p {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--xz-text-primary);
  white-space: pre-wrap;
}

.is-strength {
  background: #f0f8f3;
}

.is-strength summary {
  color: var(--xz-success);
}

.is-weakness {
  background: #fdf4f4;
}

.is-weakness summary {
  color: var(--xz-danger);
}

.is-suggestion {
  background: var(--xz-accent-light);
}

.is-suggestion summary {
  color: #b0642c;
}

.reply-list {
  margin-top: 14px;
  padding-top: 14px;
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
  gap: 6px;
  margin-top: 14px;
  padding-top: 13px;
  border-top: 1px solid var(--xz-border-light);
}

.pagination-wrap {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.reply-target {
  font-size: 13px;
  color: var(--xz-text-secondary);
  margin-bottom: 12px;
}

@media (max-width: 900px) {
  .metric-strip {
    grid-template-columns: repeat(2, 1fr);
  }

  .score-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .metric-strip {
    grid-template-columns: 1fr;
  }

  .score-row {
    grid-template-columns: 1fr;
  }
}
</style>
