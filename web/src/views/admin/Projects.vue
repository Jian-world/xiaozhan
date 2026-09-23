<template>
  <div class="admin-projects" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>项目管理</h2>
        <p class="desc">查看全平台项目作品，处理违规内容，维护作品集质量</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="summary-strip">
      <div class="s-item">
        <span class="num">{{ stat.total }}</span>
        <span class="lab">项目总数</span>
      </div>
      <div class="s-item">
        <span class="num pub">{{ stat.published }}</span>
        <span class="lab">已公开</span>
      </div>
      <div class="s-item">
        <span class="num draft">{{ stat.draft }}</span>
        <span class="lab">草稿/私密</span>
      </div>
      <div class="s-item">
        <span class="num pool">{{ stat.pool }}</span>
        <span class="lab">求点评池</span>
      </div>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索项目名称"
        :prefix-icon="Search"
        clearable
        style="width: 240px"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="query.projectType" placeholder="项目类型" clearable style="width: 140px" @change="onSearch">
        <el-option v-for="t in PROJECT_TYPES" :key="t" :label="t" :value="t" />
      </el-select>
      <el-select v-model="query.visibility" placeholder="可见性" clearable style="width: 130px" @change="onSearch">
        <el-option label="公开" value="PUBLIC" />
        <el-option label="私密" value="PRIVATE" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <div class="table-wrap">
      <el-table :data="list" stripe style="width: 100%" empty-text="暂无项目数据">
        <el-table-column label="项目" min-width="300">
          <template #default="{ row }">
            <div class="cell-project">
              <div class="thumb">
                <img v-if="row.coverUrl" :src="row.coverUrl" :alt="row.name" />
                <div v-else class="thumb-ph"><el-icon><Picture /></el-icon></div>
              </div>
              <div class="pinfo">
                <div class="pname">{{ row.name }}</div>
                <div class="psummary">{{ row.summary }}</div>
                <div class="ptech" v-if="techText(row.techStack)">
                  {{ techText(row.techStack) }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="作者" min-width="140">
          <template #default="{ row }">
            <div class="cell-sub">{{ row.studentName || '—' }}</div>
            <div class="cell-sub light">{{ row.studentSchool || '—' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.projectType || '项目' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="数据" width="170">
          <template #default="{ row }">
            <div class="stat-line">
              <span><el-icon><View /></el-icon>{{ row.viewCount || 0 }}</span>
              <span><el-icon><ChatDotRound /></el-icon>{{ row.expertReviewCount || 0 }}</span>
              <span v-if="row.expertAvgScore" class="score">
                <el-icon><StarFilled /></el-icon>{{ Number(row.expertAvgScore).toFixed(1) }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="发布状态" width="110">
          <template #default="{ row }">
            <el-tag
              :type="row.publishStatus === 1 ? 'success' : 'info'"
              size="small"
              effect="light"
            >
              {{ row.publishStatus === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="可见性" width="100">
          <template #default="{ row }">
            <el-tag :type="row.visibility === 'PUBLIC' ? 'success' : 'info'" size="small" effect="light">
              {{ row.visibility === 'PUBLIC' ? '公开' : '私密' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">
            <span class="cell-sub">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="right" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewProject(row)">
              查看详情
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, Search, Picture, View, ChatDotRound, StarFilled } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const router = useRouter()

const PROJECT_TYPES = ['课程设计', '竞赛项目', '实习项目', '开源项目', '毕业设计', '个人作品']

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = ref({
  keyword: '',
  projectType: '',
  visibility: null,
  pageNum: 1,
  pageSize: 12
})

const stat = computed(() => {
  const arr = list.value
  return {
    total: total.value,
    published: arr.filter((i) => i.publishStatus === 1).length,
    draft: arr.filter((i) => i.publishStatus !== 1).length,
    pool: arr.filter((i) => i.inReviewPool === 1).length
  }
})

function techText(t) {
  if (!t) return ''
  if (Array.isArray(t)) return t.join(' · ')
  return String(t)
}

function formatDate(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const params = { pageNum: query.value.pageNum, pageSize: query.value.pageSize }
    if (query.value.keyword) params.keyword = query.value.keyword
    if (query.value.projectType) params.projectType = query.value.projectType
    if (query.value.visibility !== null && query.value.visibility !== '') {
      params.visibility = query.value.visibility
    }

    const res = await adminApi.projects(params)
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

function viewProject(row) {
  router.push(`/project/${row.id}`)
}

onMounted(load)
</script>

<style scoped lang="scss">
.admin-projects {
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

      &.pub {
        color: #52b788;
      }

      &.draft {
        color: #9aa8a7;
      }

      &.pool {
        color: var(--xz-accent);
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
  gap: 10px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.table-wrap {
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 8px 16px 16px;
  box-shadow: var(--xz-shadow-sm);
}

.cell-project {
  display: flex;
  gap: 12px;
}

.thumb {
  width: 96px;
  height: 62px;
  border-radius: 8px;
  overflow: hidden;
  background: #f4f7f7;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .thumb-ph {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #c3cfce;
  }
}

.pinfo {
  min-width: 0;

  .pname {
    font-size: 13.5px;
    font-weight: 600;
    color: var(--xz-text-primary);
  }

  .psummary {
    margin-top: 4px;
    font-size: 12px;
    color: #8a9a99;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .ptech {
    margin-top: 4px;
    font-size: 11.5px;
    color: #7d8f8e;
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

.stat-line {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #8a9a99;

  span {
    display: inline-flex;
    align-items: center;
    gap: 3px;
  }

  .score {
    color: var(--xz-accent);
    font-weight: 600;
  }
}

.pager {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}
</style>
