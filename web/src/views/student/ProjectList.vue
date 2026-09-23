<template>
  <div class="project-list-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">我的项目</h2>
        <p class="page-sub">课程设计、竞赛作品、实习产出都可以在这里沉淀成作品集</p>
      </div>
      <el-button type="primary" @click="router.push('/student/projects/edit')">
        <el-icon><Plus /></el-icon>
        新建项目
      </el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="query.publishStatus" @change="reload">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="1">已发布</el-radio-button>
        <el-radio-button :value="0">草稿箱</el-radio-button>
      </el-radio-group>
      <span class="result-count">共 {{ total }} 个项目</span>
    </div>

    <div v-loading="loading" class="project-table-wrap">
      <el-table
        v-if="projects.length"
        :data="projects"
        class="project-table"
        @row-click="(row) => router.push(`/student/projects/edit/${row.id}`)"
      >
        <el-table-column label="项目" min-width="280">
          <template #default="{ row }">
            <div class="cell-project">
              <div class="cell-cover" :style="coverStyle(row)">
                <img v-if="row.coverUrl" :src="row.coverUrl" :alt="row.name" />
                <el-icon v-else><Files /></el-icon>
              </div>
              <div class="cell-info">
                <span class="cell-name">{{ row.name }}</span>
                <span class="cell-summary">{{ row.summary || '未填写简介' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="方向 / 类型" width="160">
          <template #default="{ row }">
            <div class="cell-tags">
              <el-tag size="small" effect="plain">{{ CATEGORY_TEXT[row.category] || '未分类' }}</el-tag>
              <span class="cell-type">{{ PROJECT_TYPE_TEXT[row.projectType] || '其他' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <div class="cell-status">
              <el-tag size="small" :type="REVIEW_PUBLISH_STATUS[row.publishStatus]?.type" effect="plain">
                {{ REVIEW_PUBLISH_STATUS[row.publishStatus]?.text }}
              </el-tag>
              <el-tag
                v-if="row.publishStatus === 1"
                size="small"
                :type="REVIEW_AUDIT_STATUS[row.reviewStatus]?.type"
                effect="plain"
              >
                {{ REVIEW_AUDIT_STATUS[row.reviewStatus]?.text }}
              </el-tag>
              <el-tag v-if="row.inReviewPool === 1" size="small" type="warning" effect="plain">
                求点评中
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="数据" width="170">
          <template #default="{ row }">
            <div class="cell-data">
              <span class="data-item">
                <el-icon><ChatDotSquare /></el-icon>
                {{ row.expertReviewCount || 0 }}
              </span>
              <span class="data-item">
                <el-icon><View /></el-icon>
                {{ row.viewCount || 0 }}
              </span>
              <span class="data-score">
                {{ row.expertAvgScore > 0 ? Number(row.expertAvgScore).toFixed(1) : '—' }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="更新时间" width="120">
          <template #default="{ row }">
            <span class="cell-time">{{ fromNow(row.updateTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="cell-ops" @click.stop>
              <el-button size="small" text type="primary" @click="router.push(`/student/projects/edit/${row.id}`)">
                编辑
              </el-button>

              <el-button
                v-if="row.publishStatus === 0"
                size="small"
                text
                type="success"
                @click="onPublish(row)"
              >
                发布
              </el-button>
              <el-button v-else size="small" text @click="onUnpublish(row)">下架</el-button>

              <el-dropdown trigger="click" @command="(cmd) => onMore(cmd, row)">
                <el-button size="small" text>
                  更多 <el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="view">
                      <el-icon><View /></el-icon> 查看前台
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="row.publishStatus === 1"
                      command="pool"
                    >
                      <el-icon><Collection /></el-icon>
                      {{ row.inReviewPool === 1 ? '移出求点评池' : '投进求点评池' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon> 删除项目
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-else-if="!loading"
        :description="query.publishStatus === 0 ? '草稿箱是空的' : '还没有创建项目'"
      >
        <el-button type="primary" @click="router.push('/student/projects/edit')">
          创建第一个项目
        </el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api'
import {
  CATEGORY_TEXT,
  PROJECT_TYPE_TEXT,
  REVIEW_AUDIT_STATUS,
  REVIEW_PUBLISH_STATUS,
  fromNow
} from '@/utils/dict'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const projects = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  publishStatus: undefined
})

const coverStyle = (p) => {
  const palette = ['#e6f2f1', '#fdf0e7', '#eef2f4', '#f3eef7']
  return { background: palette[(p.id || 0) % palette.length] }
}

const load = async () => {
  loading.value = true
  try {
    const data = await projectApi.my({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      publishStatus: query.publishStatus
    })
    projects.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    projects.value = []
  } finally {
    loading.value = false
  }
}

const reload = () => {
  query.pageNum = 1
  load()
}

const onPublish = async (row) => {
  try {
    await projectApi.publish(row.id)
    ElMessage.success('项目已发布，等待平台内容审核')
    load()
  } catch (e) {
    /* 已提示 */
  }
}

const onUnpublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      '下架后项目将从广场与企业检索中移除，确定继续吗？',
      '下架项目',
      { type: 'warning' }
    )
    await projectApi.unpublish(row.id)
    ElMessage.success('已下架')
    load()
  } catch (e) {
    /* 取消或失败 */
  }
}

const onMore = async (cmd, row) => {
  if (cmd === 'view') {
    window.open(`/project/${row.id}`, '_blank')
  } else if (cmd === 'pool') {
    const join = row.inReviewPool !== 1
    try {
      await projectApi.togglePool(row.id, join)
      ElMessage.success(join ? '已投进求点评池' : '已移出求点评池')
      load()
    } catch (e) {
      /* 已提示 */
    }
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm(
        `确定删除「${row.name}」吗？项目素材将一并删除，此操作不可恢复。`,
        '删除项目',
        { type: 'warning', confirmButtonText: '确认删除' }
      )
      await projectApi.remove(row.id)
      ElMessage.success('项目已删除')
      load()
    } catch (e) {
      /* 取消或失败 */
    }
  }
}

onMounted(() => {
  // 支持从工作台跳转带 status 参数
  if (route.query.status !== undefined) {
    query.publishStatus = Number(route.query.status)
  }
  load()
})
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
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

.project-table-wrap {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  overflow: hidden;
  min-height: 200px;
}

.project-table :deep(.el-table__row) {
  cursor: pointer;
}

.cell-project {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cell-cover {
  width: 52px;
  height: 42px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--xz-primary);
  overflow: hidden;
  flex-shrink: 0;
}

.cell-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cell-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.cell-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.cell-summary {
  font-size: 12px;
  color: var(--xz-text-placeholder);
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 260px;
}

.cell-tags {
  display: flex;
  flex-direction: column;
  gap: 5px;
  align-items: flex-start;
}

.cell-type {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
}

.cell-status {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.cell-data {
  display: flex;
  align-items: center;
  gap: 12px;
}

.data-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.data-score {
  font-size: 14px;
  font-weight: 700;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
}

.cell-time {
  font-size: 12.5px;
  color: var(--xz-text-placeholder);
}

.cell-ops {
  display: flex;
  align-items: center;
}

.pagination-wrap {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}
</style>
