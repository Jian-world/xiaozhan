<template>
  <div class="admin-verifies" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>认证审核</h2>
        <p class="desc">审核学生学籍、专家资质与企业资质，认证是平台可信度的基础</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="query.bizType" @change="onSearch">
        <el-radio-button label="">全部类型</el-radio-button>
        <el-radio-button label="STUDENT">学籍认证</el-radio-button>
        <el-radio-button label="EXPERT">专家认证</el-radio-button>
        <el-radio-button label="COMPANY">企业认证</el-radio-button>
      </el-radio-group>

      <el-radio-group v-model="query.status" @change="onSearch">
        <el-radio-button :label="1">待审核</el-radio-button>
        <el-radio-button :label="2">已通过</el-radio-button>
        <el-radio-button :label="3">已驳回</el-radio-button>
        <el-radio-button :label="null">全部</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-wrap">
      <el-table :data="list" stripe style="width: 100%" empty-text="暂无认证申请">
        <el-table-column label="申请人" min-width="200">
          <template #default="{ row }">
            <div class="cell-person">
              <el-avatar :size="34">
                {{ (row.applicantName || '?').charAt(0) }}
              </el-avatar>
              <div class="pinfo">
                <div class="pname">{{ row.applicantName || '—' }}</div>
                <div class="paccount">ID: {{ row.applicantId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="认证类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeColor(row.bizType)" size="small" effect="light">
              {{ typeText(row.bizType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="认证信息" min-width="300">
          <template #default="{ row }">
            <div class="cell-sub">{{ row.summary || '—' }}</div>
            <div class="cell-sub light">业务主体 ID: {{ row.bizId ?? '—' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="证明材料" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.submitFile"
              link
              type="primary"
              size="small"
              @click="previewMaterial(row)"
            >
              查看材料
            </el-button>
            <span v-else class="cell-sub">未上传</span>
          </template>
        </el-table-column>

        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">
            <span class="cell-sub">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusColor(row.status)" size="small" effect="light">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="审核意见" min-width="180">
          <template #default="{ row }">
            <span class="cell-sub">{{ row.remark || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" align="right" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 1">
              <el-button link type="success" size="small" @click="openAudit(row, 'PASS')">
                通过
              </el-button>
              <el-button link type="danger" size="small" @click="openAudit(row, 'REJECT')">
                驳回
              </el-button>
            </template>
            <span v-else class="cell-sub">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

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

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditDialog" :title="auditForm.status === 'PASS' ? '通过认证' : '驳回认证'" width="520px">
      <div class="audit-summary">
        <div class="row">
          <span>申请人</span><b>{{ current?.applicantName }}</b>
        </div>
        <div class="row">
          <span>认证类型</span><b>{{ typeText(current?.bizType) }}</b>
        </div>
        <div class="row" v-if="current?.summary">
          <span>认证信息</span><b>{{ current.summary }}</b>
        </div>
        <div class="row">
          <span>提交时间</span><b>{{ formatDate(current?.createTime) }}</b>
        </div>
      </div>

      <el-form :model="auditForm" label-position="top">
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            :placeholder="
              auditForm.status === 'PASS'
                ? '可填写备注（选填）'
                : '请说明驳回原因，将展示给申请人'
            "
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditDialog = false">取消</el-button>
        <el-button
          :type="auditForm.status === 'PASS' ? 'success' : 'danger'"
          :loading="auditing"
          @click="submitAudit"
        >
          确认{{ auditForm.status === 'PASS' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { verifyApi } from '@/api'
const loading = ref(false)
const list = ref([])
const total = ref(0)

const auditDialog = ref(false)
const auditing = ref(false)
const current = ref(null)
const auditForm = ref({ status: 'PASS', remark: '' })

const query = ref({
  bizType: '',
  status: 1,
  pageNum: 1,
  pageSize: 15
})

const MATERIAL_BASE = '/files/'

function typeText(t) {
  return { STUDENT: '学籍认证', EXPERT: '专家认证', COMPANY: '企业认证' }[t] || t || '—'
}

function typeColor(t) {
  return { STUDENT: 'primary', EXPERT: 'warning', COMPANY: 'success' }[t] || 'info'
}

function statusText(s) {
  return { 1: '待审核', 2: '已通过', 3: '已驳回' }[s] || '—'
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
    if (query.value.bizType) params.bizType = query.value.bizType
    if (query.value.status !== null && query.value.status !== '') params.status = query.value.status

    const res = await verifyApi.pageRecords(params)
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

function previewMaterial(row) {
  if (!row.submitFile) return
  const url = /^https?:/.test(row.submitFile)
    ? row.submitFile
    : MATERIAL_BASE + row.submitFile.replace(/^\/?(files\/)?/, '')
  window.open(url, '_blank')
}

function openAudit(row, status) {
  current.value = row
  auditForm.value = { status, remark: '' }
  auditDialog.value = true
}

async function submitAudit() {
  if (auditForm.value.status === 'REJECT' && !auditForm.value.remark.trim()) {
    ElMessage.warning('驳回时必须填写原因')
    return
  }
  auditing.value = true
  try {
    await verifyApi.audit({
      recordId: current.value.id,
      status: auditForm.value.status,
      remark: auditForm.value.remark
    })
    ElMessage.success(auditForm.value.status === 'PASS' ? '已通过认证' : '已驳回认证')
    auditDialog.value = false
    load()
  } catch (e) {
    /* 已提示 */
  } finally {
    auditing.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.admin-verifies {
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
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
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
    font-size: 13.5px;
    font-weight: 500;
    color: var(--xz-text-primary);
  }

  .paccount {
    font-size: 12px;
    color: #9aa8a7;
    margin-top: 1px;
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

.audit-summary {
  background: #f7fafa;
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 18px;

  .row {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    font-size: 13px;

    span {
      color: #8a9a99;
    }

    b {
      color: var(--xz-text-primary);
      font-weight: 500;
    }
  }
}
</style>
