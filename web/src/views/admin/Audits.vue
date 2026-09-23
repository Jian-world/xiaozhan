<template>
  <div class="admin-audits" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>内容审核</h2>
        <p class="desc">
          系统自动识别疑似敷衍点评、违规内容，运营人工复核后决定处理方式
        </p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="query.status" @change="onSearch">
        <el-radio-button :label="1">待处理</el-radio-button>
        <el-radio-button :label="2">已通过</el-radio-button>
        <el-radio-button :label="3">已下架</el-radio-button>
        <el-radio-button :label="null">全部</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="list.length" class="audit-list">
      <div v-for="item in list" :key="item.id" class="audit-card">
        <div class="card-head">
          <div class="type-tag">
            <el-tag size="small" effect="plain">{{ targetText(item.targetType) }}</el-tag>
            <el-tag
              size="small"
              :type="autoColor(item.autoResult)"
              effect="light"
            >
              {{ autoText(item.autoResult) }}
            </el-tag>
            <el-tag size="small" :type="statusColor(item.status)" effect="light">
              {{ statusText(item.status) }}
            </el-tag>
          </div>
          <span class="time">{{ formatDate(item.createTime) }}</span>
        </div>

        <div class="card-body">
          <div v-if="item.coverUrl" class="cover">
            <img :src="item.coverUrl" alt="cover" />
          </div>
          <div class="body-main">
            <h4>{{ item.title || '（无标题）' }}</h4>
            <div class="author" v-if="item.authorName">
              <el-icon><User /></el-icon> {{ item.authorName }}
            </div>
            <p class="summary">{{ item.summary || item.snapshot || '—' }}</p>
          </div>
        </div>

        <div class="card-foot">
          <div class="remark" v-if="item.remark">
            <el-icon><InfoFilled /></el-icon>
            处理意见：{{ item.remark }}
          </div>
          <div class="actions" v-if="item.status === 1">
            <el-button size="small" type="success" @click="handle(item, true)">
              通过（保留）
            </el-button>
            <el-button size="small" type="danger" plain @click="openReject(item)">
              下架处理
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="暂无待审核内容" />

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

    <el-dialog v-model="rejectDialog" title="下架处理" width="480px">
      <p class="reject-tip">
        下架后该内容将不再对外展示，作者会收到处理说明。
      </p>
      <el-input
        v-model="rejectRemark"
        type="textarea"
        :rows="3"
        maxlength="200"
        show-word-limit
        placeholder="请填写下架原因"
      />
      <template #footer>
        <el-button @click="rejectDialog = false">取消</el-button>
        <el-button type="danger" :loading="handling" @click="confirmReject">确认下架</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, User, InfoFilled } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const loading = ref(false)
const handling = ref(false)
const list = ref([])
const total = ref(0)

const rejectDialog = ref(false)
const rejectRemark = ref('')
const current = ref(null)

const query = ref({
  status: 1,
  pageNum: 1,
  pageSize: 10
})

function targetText(t) {
  return { REVIEW: '专家点评', PROJECT: '项目作品', PROFILE: '个人自述' }[t] || t || '内容'
}

function autoText(r) {
  return { PASS: '自动通过', SUSPECT: '疑似异常' }[r] || '待机审'
}

function autoColor(r) {
  return { PASS: 'success', SUSPECT: 'warning' }[r] || 'info'
}

function statusText(s) {
  return { 1: '待处理', 2: '已通过', 3: '已下架' }[s] || '—'
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
    const params = {
      pageNum: query.value.pageNum,
      pageSize: query.value.pageSize
    }
    if (query.value.status !== null && query.value.status !== '') params.status = query.value.status

    const res = await adminApi.contentAudits(params)
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

async function handle(item, pass, remark = '') {
  handling.value = true
  try {
    await adminApi.handleAudit(item.id, pass, remark)
    ElMessage.success('处理完成')
    load()
  } catch (e) {
    /* 已提示 */
  } finally {
    handling.value = false
  }
}

function openReject(item) {
  current.value = item
  rejectRemark.value = ''
  rejectDialog.value = true
}

async function confirmReject() {
  if (!rejectRemark.value.trim()) {
    ElMessage.warning('请填写下架原因')
    return
  }
  await handle(current.value, false, rejectRemark.value)
  rejectDialog.value = false
}

onMounted(load)
</script>

<style scoped lang="scss">
.admin-audits {
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
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.audit-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.audit-card {
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 20px 24px;
  box-shadow: var(--xz-shadow-sm);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;

  .type-tag {
    display: flex;
    gap: 8px;
  }

  .time {
    font-size: 12px;
    color: #9aa8a7;
  }
}

.card-body {
  display: flex;
  gap: 16px;
}

.cover {
  width: 140px;
  height: 92px;
  border-radius: 10px;
  overflow: hidden;
  background: #f4f7f7;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.body-main {
  flex: 1;
  min-width: 0;

  h4 {
    margin: 0;
    font-size: 15px;
    color: var(--xz-text-primary);
  }

  .author {
    margin-top: 6px;
    font-size: 12px;
    color: #8a9a99;
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .summary {
    margin: 10px 0 0;
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

.card-foot {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed #eef2f2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;

  .remark {
    flex: 1;
    font-size: 12.5px;
    color: #8a6534;
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .actions {
    display: flex;
    gap: 10px;
    flex-shrink: 0;
  }
}

.reject-tip {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--xz-text-regular);
  line-height: 1.7;
}

.pager {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
