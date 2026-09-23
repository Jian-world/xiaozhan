<template>
  <div class="expert-hall">
    <div class="page-hero">
      <div class="xz-container">
        <h1 class="page-title">导师与工程师</h1>
        <p class="page-desc">
          这些点评人来自高校与一线企业。他们了解课程设计的评分逻辑，也了解真实工程里的取舍，
          能给出「既有用又可执行」的改进建议。
        </p>

        <div class="type-filter">
          <el-radio-group v-model="expertType" @change="reload">
            <el-radio-button value="">全部点评人</el-radio-button>
            <el-radio-button value="TEACHER">高校导师</el-radio-button>
            <el-radio-button value="ENGINEER">企业工程师</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <div class="xz-container hall-body">
      <div v-loading="loading" class="expert-grid">
        <article v-for="e in experts" :key="e.expertUserId" class="expert-card">
          <div class="card-top">
            <el-avatar :size="60" :src="e.avatar" class="expert-avatar">
              {{ (e.name || '师').slice(0, 1) }}
            </el-avatar>
            <div class="expert-head">
              <h3 class="expert-name">
                {{ e.name }}
                <el-tag v-if="e.level" size="small" :type="EXPERT_LEVEL_TYPE[e.level]" effect="plain">
                  {{ EXPERT_LEVEL_TEXT[e.level] }}
                </el-tag>
              </h3>
              <p class="expert-org">
                <template v-if="e.orgName">
                  {{ e.orgName }}<template v-if="e.position"> · {{ e.position }}</template>
                </template>
                <template v-else>{{ EXPERT_TYPE_TEXT[e.expertType] || '认证点评人' }}</template>
              </p>
              <el-tag size="small" effect="plain" class="type-tag">
                {{ EXPERT_TYPE_TEXT[e.expertType] || '点评人' }}
              </el-tag>
            </div>
          </div>

          <div v-if="e.domainTags && e.domainTags.length" class="domain-tags">
            <span v-for="t in e.domainTags" :key="t">{{ t }}</span>
          </div>

          <div class="card-metrics">
            <div class="metric">
              <span class="metric-value">{{ e.totalReviews || 0 }}</span>
              <span class="metric-label">累计点评</span>
            </div>
            <div class="metric">
              <span class="metric-value">{{ e.thanksCount || 0 }}</span>
              <span class="metric-label">收到致谢</span>
            </div>
            <div class="metric">
              <span class="metric-value">
                {{ e.qualityScore ? Number(e.qualityScore).toFixed(1) : '—' }}
              </span>
              <span class="metric-label">质量分</span>
            </div>
          </div>
        </article>
      </div>

      <el-empty v-if="!loading && !experts.length" description="暂无已通过认证的点评人" />

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="load"
        />
      </div>

      <div class="join-cta">
        <div class="cta-copy">
          <h2 class="cta-title">你也想为学生的真实能力背书吗？</h2>
          <p class="cta-desc">
            无论你是高校教师、课程助教，还是有一线项目经验的企业工程师，
            都可以申请成为校栈点评人。每次点评都会沉淀为可查的专业记录。
          </p>
        </div>
        <el-button type="primary" size="large" @click="goJoin">申请成为点评人</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { commonApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { EXPERT_LEVEL_TEXT, EXPERT_LEVEL_TYPE, EXPERT_TYPE_TEXT } from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const experts = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const expertType = ref('')

const load = async () => {
  loading.value = true
  try {
    const data = await commonApi.expertHall({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      expertType: expertType.value || undefined
    })
    experts.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    experts.value = []
  } finally {
    loading.value = false
  }
}

const reload = () => {
  pageNum.value = 1
  load()
}

const goJoin = () => {
  if (userStore.isLogin) {
    if (userStore.isExpert) {
      router.push('/expert/profile')
    } else {
      router.push('/')
    }
  } else {
    router.push('/register')
  }
}

onMounted(load)
</script>

<style scoped>
.page-hero {
  padding: 40px 0 32px;
  background: linear-gradient(168deg, #f7fbfa 0%, #eef6f5 100%);
  border-bottom: 1px solid var(--xz-border-light);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--xz-text-primary);
}

.page-desc {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.85;
  color: var(--xz-text-secondary);
  max-width: 760px;
}

.type-filter {
  margin-top: 24px;
}

.hall-body {
  padding-top: 30px;
  padding-bottom: 20px;
}

.expert-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 200px;
}

.expert-card {
  background: #ffffff;
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius-lg);
  padding: 22px 22px 20px;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.expert-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--xz-shadow);
}

.card-top {
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.expert-avatar {
  background: var(--xz-primary);
  font-size: 22px;
  flex-shrink: 0;
}

.expert-head {
  flex: 1;
  min-width: 0;
}

.expert-name {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 16px;
  font-weight: 600;
  color: var(--xz-text-primary);
  flex-wrap: wrap;
}

.expert-org {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--xz-text-secondary);
  line-height: 1.55;
}

.type-tag {
  margin-top: 7px;
}

.domain-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 15px;
}

.domain-tags span {
  font-size: 11.5px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 2px 9px;
  border-radius: 4px;
}

.card-metrics {
  display: flex;
  justify-content: space-between;
  margin-top: 17px;
  padding-top: 15px;
  border-top: 1px solid var(--xz-border-light);
}

.metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.metric-value {
  font-size: 17px;
  font-weight: 700;
  color: var(--xz-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.metric-label {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 2px;
}

.pagination-wrap {
  margin-top: 34px;
  display: flex;
  justify-content: center;
}

.join-cta {
  margin: 44px 0 20px;
  padding: 34px 40px;
  border-radius: var(--xz-radius-lg);
  background: linear-gradient(140deg, #f2f9f8 0%, #eaf3f2 100%);
  border: 1px solid #dcebe9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}

.cta-title {
  font-size: 21px;
  font-weight: 700;
  color: var(--xz-text-primary);
}

.cta-desc {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.85;
  color: var(--xz-text-secondary);
  max-width: 620px;
}

.join-cta :deep(.el-button) {
  height: 46px;
  padding: 0 28px;
  flex-shrink: 0;
}

@media (max-width: 1080px) {
  .expert-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 680px) {
  .expert-grid {
    grid-template-columns: 1fr;
  }

  .join-cta {
    padding: 26px 22px;
  }
}
</style>
