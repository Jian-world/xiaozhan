<template>
  <div class="admin-appeals" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>申诉处理</h2>
        <p class="desc">
          学生对专家点评提出申诉时，由平台运营复核并给出结论
        </p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="query.status" @change="onSearch">
        <el-radio-button :label="1">待处理</el-radio-button>
        <el-radio-button :label="2">已受理</el-radio-button>
        <el-radio-button :label="3">已驳回</el-radio-button>
        <el-radio-button :label="null">全部</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="list.length" class="appeal-list">
      <div v-for="item in list" :key="item.id" class="appeal-card">
        <div class="card-head">
          <div class="who">
            <el-avatar :size="34">
              {{ (item.studentName || '?').charAt(0) }}
            </el-avatar>
            <div>
              <div class="name">{{ item.studentName || '学生' }}</div>
              <div class="ref">申诉点评 ID：{{ item.reviewId }}</div>
            </div>
          </div>
          <el-tag :type="statusColor(item.status)" size="small" effect="light">
            {{ statusText(item.status) }}
          </el-tag>
        </div>

        <!-- 被申诉的点评 -->
        <div class="review-ref" v-if="item.comment">
          <div class="ref-head">
            <span class="tag">被申诉点评</span>
            <span class="proj">{{ item.projectName || '未知项目' }}</span>
            <span class="score" v-if="item.avgScore">
              <el-icon><StarFilled /></el-icon>{{ Number(item.avgScore).toFixed(1) }}
            </span>
          </div>
          <p class="ref-text">{{ item.comment }}</p>
        </div>

        <div class="reason-box">
          <div class="label">申诉理由</div>
          <p class="text">{{ item.reason || '—' }}</p>
          <div v-if="item.evidenceUrl" class="evidence">
            <el-button link type="primary" size="small" @click="openEvidence(item.evidenceUrl)">
              查看证据材料
            </el-button>
          </div>
        </div>

        <div v-if="item.status !== 1 && item.handleRemark" class="result-box">
          <div class="label">处理结论</div>
          <p class="text">{{ item.handleRemark }}</p>
        </div>

        <div class="card-foot">
          <span class="time">提交于 {{ formatDate(item.createTime) }}</span>
          <div class="actions" v-if="item.status === 1">
            <el-button size="small" type="success" @click="openHandle(item, 'ACCEPT')">
              受理申诉
            </el-button>
            <el-button size="small" type="danger" plain @click="openHandle(item, 'REJECT')">
              驳回申诉
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="暂无申诉记录" />

    <div v-if="total > query.pageSize" class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="onPageChange"
      />
    </div>

    <el-dialog
      v-model="handleDialog"
      :title="handleForm.status === 'ACCEPT' ? '受理申诉' : '驳回申诉'"
      width="500px"
    >
      <p class="handle-tip">
        {{
          handleForm.status === 'ACCEPT'
            ? '受理后该条点评将被隐藏，不再对外展示。'
            : '驳回时请说明理由，申诉人将看到处理结论。'
        }}
      </p>
      <el-input
        v-model="handleForm.remark"
        type="textarea"
        :rows="4"
        maxlength="300"
        show-word-limit
        placeholder="请填写处理结论"
      />
      <template #footer>
        <el-button @click="handleDialog = false">取消</el-button>
        <el-button
          :type="handleForm.status === 'ACCEPT' ? 'success' : 'danger'"
          :loading="handling"
          @click="submitHandle"
        >
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, StarFilled } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const loading = ref(false)
const handling = ref(false)
const list = ref([])
const total = ref(0)

const handleDialog = ref(false)
const current = ref(null)
const handleForm = ref({ status: 'ACCEPT', remark: '' })

const query = ref({ status: 1, pageNum: 1, pageSize: 10 })

function statusText(s) {
  return { 1: '待处理', 2: '已受理', 3: '已驳回' }[s] || '—'
}

function statusColor(s) {
  return { 1: 'warning', 2: 'success', 3: 'danger' }[s] || 'info'
}

function formatDate(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const params = { pageNum: query.value.pageNum, pageSize: query.value.pageSize }
    if (query.value.status !== null && query.value.status !== '') params.status = query.value.status

    const res = await adminApi.appeals(params)
    const data = res || {}
    if (Array.isArray(data)) {
      list.value = data
      total.value = data.length
    } else {
      list.value = data.records || data.list || []
      total.value = data.total || list.value.length
    }
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.value.pageNum = 1
  load()
}

function onPageChange(p) {
  query.value.pageNum = p
  load()
}

function openEvidence(url) {
  const real = /^https?:/.test(url) ? url : '/files/' + url.replace(/^\/?(files\/)?/, '')
  window.open(real, '_blank')
}

function openHandle(item, status) {
  current.value = item
  handleForm.value = { status, remark: '' }
  handleDialog.value = true
}

async function submitHandle() {
  if (!handleForm.value.remark.trim()) {
    ElMessage.warning('请填写处理结论')
    return
  }
  handling.value = true
  try {
    await adminApi.handleAppeal(current.value.id, handleForm.value.status, handleForm.value.remark)
    ElMessage.success('处理完成')
    handleDialog.value = false
    load()
  } catch (e) {
    /* 已提示 */
  } finally {
    handling.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.admin-appeals {
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

.filter-bar {
  margin-bottom: 18px;
}

.appeal-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.appeal-card {
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 20px 24px;
  box-shadow: var(--xz-shadow-sm);
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;

  .who {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .name {
    font-size: 14.5px;
    font-weight: 600;
    color: var(--xz-text-primary);
  }

  .ref {
    margin-top: 4px;
    font-size: 12px;
    color: #8a9a99;
  }
}

.review-ref {
  padding: 14px 18px;
  border-radius: 10px;
  background: #fbfcfc;
  border: 1px solid #eef2f2;
  margin-bottom: 12px;

  .ref-head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;

    .tag {
      font-size: 12px;
      color: var(--xz-accent);
      background: #fdf1e8;
      padding: 2px 8px;
      border-radius: 10px;
    }

    .proj {
      font-size: 12.5px;
      color: var(--xz-text-primary);
      font-weight: 500;
    }

    .score {
      margin-left: auto;
      display: inline-flex;
      align-items: center;
      gap: 3px;
      font-size: 13px;
      font-weight: 600;
      color: var(--xz-accent);
    }
  }

  .ref-text {
    margin: 0;
    font-size: 13px;
    line-height: 1.8;
    color: var(--xz-text-regular);
    display: -webkit-box;
    -webkit-line-clamp: 4;
    -webkit-box-orient: vertical;
    overflow: hidden;
    white-space: pre-wrap;
  }
}

.reason-box,
.result-box {
  padding: 14px 18px;
  border-radius: 10px;
  margin-bottom: 12px;

  .label {
    font-size: 12px;
    color: #8a9a99;
    margin-bottom: 7px;
  }

  .text {
    margin: 0;
    font-size: 13.5px;
    line-height: 1.8;
    color: var(--xz-text-regular);
    white-space: pre-wrap;
  }
}

.reason-box {
  background: #f7fafa;
}

.result-box {
  background: #eef8f4;
}

.evidence {
  margin-top: 8px;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px dashed #eef2f2;

  .time {
    font-size: 12px;
    color: #9aa8a7;
  }

  .actions {
    display: flex;
    gap: 10px;
  }
}

.handle-tip {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.75;
  color: var(--xz-text-regular);
}

.pager {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
