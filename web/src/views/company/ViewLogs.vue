<template>
  <div class="company-logs" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>查验记录</h2>
        <p class="desc">
          每一次候选人档案查验都会留档，平台与候选人均可追溯，请合规使用
        </p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="summary-strip">
      <div class="s-item">
        <span class="num">{{ total }}</span>
        <span class="lab">累计查验</span>
      </div>
      <div class="s-item">
        <span class="num">{{ monthCount }}</span>
        <span class="lab">本月新增</span>
      </div>
      <div class="s-item">
        <span class="num">{{ remainQuota }}</span>
        <span class="lab">剩余额度</span>
      </div>
    </div>

    <div class="table-wrap">
      <el-table :data="list" stripe style="width: 100%" empty-text="暂无查验记录">
        <el-table-column label="候选人" min-width="180">
          <template #default="{ row }">
            <div class="cell-person">
              <el-avatar :size="32" :src="row.studentAvatar">
                {{ (row.studentName || '?').charAt(0) }}
              </el-avatar>
              <span class="pname">{{ row.studentName || '—' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="学校 / 专业" min-width="220">
          <template #default="{ row }">
            <div class="cell-sub">{{ row.school || '—' }}</div>
            <div class="cell-sub light">{{ row.major || '—' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="查验内容" min-width="160">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">
              {{ row.viewType || '完整档案' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="查验时间" min-width="170">
          <template #default="{ row }">
            <span class="cell-sub">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="130" align="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              :disabled="!row.studentId"
              @click="goCandidate(row.studentId)"
            >
              查看档案
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="total > pageSize" class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="onPageChange"
      />
    </div>

    <div class="compliance-note">
      <el-icon><WarningFilled /></el-icon>
      <div>
        <b>合规提示</b>
        <p>
          查验记录用于保障候选人的知情权。企业不得将候选人档案用于招聘以外的用途，
          不得在未经许可的情况下对外传播候选人作品与点评内容。
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, WarningFilled } from '@element-plus/icons-vue'
import { companyApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(15)
const monthCount = ref(0)
const remainQuota = ref('—')
const compStat = ref({})

function formatDate(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const res = await companyApi.viewLogs({ pageNum: pageNum.value, pageSize: pageSize.value })
    const data = res || {}
    if (Array.isArray(data)) {
      list.value = data
      total.value = data.length
    } else {
      list.value = data.records || data.list || []
      total.value = data.total || list.value.length
    }

    const now = new Date()
    const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    monthCount.value = list.value.filter((i) =>
      String(i.createTime || '').startsWith(ym)
    ).length
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadStat() {
  try {
    const s = await companyApi.statistics()
    compStat.value = s || {}
    remainQuota.value = `${s.remainQuota ?? 0} / ${s.monthQuota ?? 0}`
  } catch (e) {
    remainQuota.value = '—'
  }
}

function onPageChange(p) {
  pageNum.value = p
  load()
}

function goCandidate(studentId) {
  router.push(`/company/candidate/${studentId}`)
}

onMounted(() => {
  load()
  loadStat()
})
</script>

<style scoped lang="scss">
.company-logs {
  padding-bottom: 30px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 22px;
    color: var(--xz-text-primary);
  }

  .desc {
    margin: 8px 0 0;
    font-size: 13px;
    color: #8a9a99;
  }
}

.summary-strip {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;

  .s-item {
    flex: 1;
    background: #fff;
    border-radius: var(--xz-radius-lg);
    padding: 18px 22px;
    box-shadow: var(--xz-shadow-sm);

    .num {
      display: block;
      font-size: 26px;
      font-weight: 700;
      color: var(--xz-text-primary);
    }

    .lab {
      font-size: 12px;
      color: #8a9a99;
    }
  }
}

.table-wrap {
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 8px 16px 16px;
  box-shadow: var(--xz-shadow-sm);
}

.cell-person {
  display: flex;
  align-items: center;
  gap: 10px;

  .pname {
    font-size: 13px;
    color: var(--xz-text-primary);
    font-weight: 500;
  }
}

.cell-sub {
  font-size: 12.5px;
  color: var(--xz-text-regular);

  &.light {
    color: #9aa8a7;
    margin-top: 2px;
  }
}

.pager {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}

.compliance-note {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: #fff8ee;
  border: 1px solid #f6e2c4;
  border-radius: 12px;
  color: #8a6534;
  font-size: 13px;

  .el-icon {
    font-size: 18px;
    color: #e6a23c;
    margin-top: 2px;
  }

  b {
    display: block;
    margin-bottom: 4px;
  }

  p {
    margin: 0;
    line-height: 1.75;
  }
}
</style>
