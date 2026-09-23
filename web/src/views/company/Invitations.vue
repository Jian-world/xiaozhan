<template>
  <div class="company-invitations" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>发出的邀约</h2>
        <p class="desc">跟踪每一位候选人对面试邀约的回应情况</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <!-- 状态统计 -->
    <div class="summary-strip">
      <div class="s-item">
        <span class="num">{{ stat.total }}</span>
        <span class="lab">累计发出</span>
      </div>
      <div class="s-item">
        <span class="num pending">{{ stat.pending }}</span>
        <span class="lab">待回应</span>
      </div>
      <div class="s-item">
        <span class="num accepted">{{ stat.accepted }}</span>
        <span class="lab">已接受</span>
      </div>
      <div class="s-item">
        <span class="num refused">{{ stat.refused }}</span>
        <span class="lab">已婉拒</span>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="filter" size="default">
        <el-radio-button label="ALL">全部</el-radio-button>
        <el-radio-button label="1">待回应</el-radio-button>
        <el-radio-button label="2">已接受</el-radio-button>
        <el-radio-button label="3">已婉拒</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="keyword"
        placeholder="搜索候选人 / 岗位"
        :prefix-icon="Search"
        clearable
        style="width: 240px"
      />
    </div>

    <!-- 列表 -->
    <div v-if="filtered.length" class="inv-list">
      <div v-for="item in filtered" :key="item.id" class="inv-card" :class="'st-' + item.status">
        <div class="stripe" />

        <div class="inv-main">
          <div class="inv-head">
            <div class="who">
              <el-avatar :size="42" :src="item.studentAvatar">
                {{ (item.studentName || '?').charAt(0) }}
              </el-avatar>
              <div class="who-meta">
                <div class="name">{{ item.studentName || '候选人' }}</div>
                <div class="job">{{ item.jobTitle }}</div>
              </div>
            </div>
            <el-tag :type="statusType(item.status)" size="small" effect="light">
              {{ statusText(item.status) }}
            </el-tag>
          </div>

          <p class="content">{{ item.content }}</p>

          <div class="inv-foot">
            <span class="time">
              <el-icon><Clock /></el-icon>
              发出于 {{ formatDate(item.createTime) }}
            </span>
            <span v-if="item.replyTime" class="time">
              <el-icon><Select /></el-icon>
              回应于 {{ formatDate(item.replyTime) }}
            </span>
          </div>

          <!-- 接受后展示联系方式 -->
          <div v-if="item.status === 2 && (item.replyContact || item.contactInfo)" class="contact-box">
            <el-icon><Phone /></el-icon>
            <span class="label">候选人联系方式：</span>
            <b>{{ item.replyContact || item.contactInfo }}</b>
            <el-button
              link
              type="primary"
              size="small"
              style="margin-left: 8px"
              @click="copyText(item.replyContact || item.contactInfo)"
            >
              复制
            </el-button>
          </div>
          <div v-else-if="item.status === 3" class="contact-box refuse">
            <el-icon><CircleClose /></el-icon>
            候选人已婉拒本次邀约。
          </div>
        </div>

        <div class="inv-side">
          <el-button size="small" @click="goCandidate(item.studentId)">查看档案</el-button>
          <el-button size="small" plain @click="resend(item)">重新发起</el-button>
        </div>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="暂无邀约记录">
      <el-button type="primary" @click="$router.push('/company/talent')">前往人才检索</el-button>
    </el-empty>

    <!-- 重新发起弹窗 -->
    <el-dialog v-model="dialog" title="重新发起邀约" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="候选人">
          <el-input :model-value="form.studentName" disabled />
        </el-form-item>
        <el-form-item label="应聘岗位" required>
          <el-input v-model="form.jobTitle" placeholder="如：前端开发工程师（校招）" />
        </el-form-item>
        <el-form-item label="邀约内容" required>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="简要说明岗位职责、团队情况与联系方式"
          />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contactInfo" placeholder="邮箱 / 电话 / 微信" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">发送邀约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh, Search, Clock, Select, Phone, CircleClose
} from '@element-plus/icons-vue'
import { companyApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const rawList = ref([])
const filter = ref('ALL')
const keyword = ref('')

const dialog = ref(false)
const saving = ref(false)
const form = ref({
  studentId: null, studentName: '', jobTitle: '', content: '', contactInfo: ''
})

const stat = computed(() => {
  const arr = rawList.value
  return {
    total: arr.length,
    pending: arr.filter((i) => i.status === 1).length,
    accepted: arr.filter((i) => i.status === 2).length,
    refused: arr.filter((i) => i.status === 3).length
  }
})

const filtered = computed(() => {
  let arr = rawList.value
  if (filter.value !== 'ALL') {
    arr = arr.filter((i) => String(i.status) === filter.value)
  }
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    arr = arr.filter(
      (i) =>
        (i.studentName || '').toLowerCase().includes(kw) ||
        (i.jobTitle || '').toLowerCase().includes(kw)
    )
  }
  return arr
})

function statusText(s) {
  return { 1: '待回应', 2: '已接受', 3: '已婉拒' }[s] || '未知'
}

function statusType(s) {
  return { 1: 'warning', 2: 'success', 3: 'info' }[s] || 'info'
}

function formatDate(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const res = await companyApi.sentInvitations({ pageNum: 1, pageSize: 200 })
    const data = res || {}
    if (Array.isArray(data)) {
      rawList.value = data
    } else {
      rawList.value = data.records || data.list || []
    }
  } catch (e) {
    rawList.value = []
  } finally {
    loading.value = false
  }
}

function goCandidate(studentId) {
  if (!studentId) {
    ElMessage.info('该邀约未关联候选人档案')
    return
  }
  router.push(`/company/candidate/${studentId}`)
}

function resend(item) {
  form.value = {
    studentId: item.studentId,
    studentName: item.studentName,
    jobTitle: item.jobTitle || '',
    content: item.content || '',
    contactInfo: item.contactInfo || ''
  }
  dialog.value = true
}

async function submit() {
  if (!form.value.jobTitle.trim()) {
    ElMessage.warning('请填写应聘岗位')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请填写邀约内容')
    return
  }
  saving.value = true
  try {
    await companyApi.sendInvitation({
      studentId: form.value.studentId,
      jobTitle: form.value.jobTitle,
      content: form.value.content,
      contactInfo: form.value.contactInfo
    })
    ElMessage.success('邀约已发送')
    dialog.value = false
    load()
  } catch (e) {
    /* 已提示 */
  } finally {
    saving.value = false
  }
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text || '')
    ElMessage.success('已复制')
  } catch (e) {
    ElMessage.info(text)
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.company-invitations {
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

      &.pending {
        color: #e6a23c;
      }

      &.accepted {
        color: #52b788;
      }

      &.refused {
        color: #9aa8a7;
      }
    }

    .lab {
      font-size: 12px;
      color: #8a9a99;
    }
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.inv-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.inv-card {
  position: relative;
  display: flex;
  gap: 18px;
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 20px 22px 20px 28px;
  box-shadow: var(--xz-shadow-sm);
  overflow: hidden;

  .stripe {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 5px;
    background: #e6a23c;
  }

  &.st-2 .stripe {
    background: #52b788;
  }

  &.st-3 .stripe {
    background: #c8d2d1;
  }
}

.inv-main {
  flex: 1;
  min-width: 0;
}

.inv-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.who {
  display: flex;
  gap: 12px;
  align-items: center;

  .name {
    font-size: 16px;
    font-weight: 600;
    color: var(--xz-text-primary);
  }

  .job {
    margin-top: 4px;
    font-size: 13px;
    color: var(--xz-accent);
    font-weight: 500;
  }
}

.content {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.8;
  color: var(--xz-text-regular);
  white-space: pre-wrap;
}

.inv-foot {
  display: flex;
  gap: 20px;
  margin-top: 12px;

  .time {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    color: #9aa8a7;
  }
}

.contact-box {
  margin-top: 14px;
  padding: 11px 14px;
  border-radius: 10px;
  background: #eef8f4;
  border: 1px solid #d5ede2;
  font-size: 13px;
  color: #2c6e57;
  display: flex;
  align-items: center;
  gap: 6px;

  b {
    font-weight: 600;
  }

  &.refuse {
    background: #f7f8f8;
    border-color: #e8ecec;
    color: #8a9a99;
  }
}

.inv-side {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
  flex-shrink: 0;

  .el-button {
    margin: 0;
    width: 96px;
  }
}
</style>
