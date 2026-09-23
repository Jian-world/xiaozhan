<template>
  <div class="admin-users" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>用户管理</h2>
        <p class="desc">查询平台全量账号，执行启用 / 禁用与密码重置</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <!-- 角色筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="query.role" @change="onSearch">
        <el-radio-button label="">全部角色</el-radio-button>
        <el-radio-button label="STUDENT">学生</el-radio-button>
        <el-radio-button label="EXPERT">专家</el-radio-button>
        <el-radio-button label="COMPANY">企业</el-radio-button>
        <el-radio-button label="ADMIN">管理员</el-radio-button>
      </el-radio-group>

      <div class="filter-right">
        <el-input
          v-model="query.keyword"
          placeholder="搜索账号 / 昵称 / 企业名"
          :prefix-icon="Search"
          clearable
          style="width: 240px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="onSearch">
          <el-option label="正常" :value="1" />
          <el-option label="已禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>
    </div>

    <div class="table-wrap">
      <el-table :data="list" stripe style="width: 100%" empty-text="暂无用户数据">
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="cell-person">
              <el-avatar :size="34" :src="row.avatar">
                {{ (row.nickname || row.username || '?').charAt(0) }}
              </el-avatar>
              <div class="pinfo">
                <div class="pname">{{ row.nickname || row.username }}</div>
                <div class="paccount">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleColor(row.role)" size="small" effect="light">
              {{ roleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="单位 / 学校" min-width="180">
          <template #default="{ row }">
            <span class="cell-sub">{{ row.orgInfo || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" min-width="180">
          <template #default="{ row }">
            <div class="cell-sub">{{ row.phone || '—' }}</div>
            <div class="cell-sub light">{{ row.email || '—' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="认证 / 套餐" min-width="140">
          <template #default="{ row }">
            <template v-if="row.role === 'STUDENT' || row.role === 'EXPERT' || row.role === 'COMPANY'">
              <el-tag
                size="small"
                effect="plain"
                :type="row.verifyStatus === 1 ? 'success' : row.verifyStatus === 2 ? 'danger' : 'info'"
              >
                {{ verifyText(row.verifyStatus) }}
              </el-tag>
            </template>
            <span v-else class="cell-sub">—</span>
          </template>
        </el-table-column>

        <el-table-column label="最近登录" min-width="150">
          <template #default="{ row }">
            <span class="cell-sub">{{ formatDate(row.lastLoginAt) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="light">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="right" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="warning" size="small" @click="resetPwd(row)">
              重置密码
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = ref({
  role: '',
  keyword: '',
  status: null,
  pageNum: 1,
  pageSize: 15
})

function roleText(r) {
  return { STUDENT: '学生', EXPERT: '专家', COMPANY: '企业', ADMIN: '管理员' }[r] || r
}

function roleColor(r) {
  return { STUDENT: 'primary', EXPERT: 'warning', COMPANY: 'success', ADMIN: 'danger' }[r] || 'info'
}

function verifyText(s) {
  return { 0: '待认证', 1: '已认证', 2: '已驳回', 3: '未提交' }[s] ?? '—'
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
    if (query.value.role) params.role = query.value.role
    if (query.value.keyword) params.keyword = query.value.keyword
    if (query.value.status !== null && query.value.status !== '') params.status = query.value.status

    const res = await adminApi.users(params)
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

async function toggleStatus(row) {
  const target = row.status === 1 ? 0 : 1
  const word = target === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确认${word}账号「${row.nickname || row.username}」？${
        target === 0 ? '禁用后该账号将无法登录。' : ''
      }`,
      `${word}账号`,
      { type: 'warning' }
    )
  } catch (e) {
    return
  }
  try {
    await adminApi.toggleUserStatus(row.id, target)
    ElMessage.success(`已${word}`)
    load()
  } catch (e) {
    /* 已提示 */
  }
}

async function resetPwd(row) {
  try {
    await ElMessageBox.confirm(
      `确认将「${row.nickname || row.username}」的密码重置为默认密码？`,
      '重置密码',
      { type: 'warning' }
    )
  } catch (e) {
    return
  }
  try {
    const res = await adminApi.resetPassword(row.id)
    const pwd = res?.password || res?.newPassword || '123456'
    ElMessageBox.alert(`新密码：${pwd}，请提醒用户尽快修改。`, '重置成功', {
      confirmButtonText: '知道了'
    })
  } catch (e) {
    /* 已提示 */
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.admin-users {
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

  .filter-right {
    display: flex;
    gap: 10px;
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
</style>
