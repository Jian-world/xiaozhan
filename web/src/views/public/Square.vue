<template>
  <div class="square-page">
    <div class="page-hero">
      <div class="xz-container">
        <h1 class="page-title">项目广场</h1>
        <p class="page-desc">
          这里汇集了同学们真实提交的项目作品。点开任意一个，你能看到源码、文档、演示视频，
          以及导师和企业工程师留下的逐项点评。
        </p>
      </div>
    </div>

    <div class="xz-container">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="query.keyword"
            placeholder="搜索项目名称、技术栈或关键词"
            :prefix-icon="Search"
            clearable
            class="search-input"
            @keyup.enter="reload"
            @clear="reload"
          />

          <el-select v-model="query.category" placeholder="全部方向" clearable class="filter-select" @change="reload">
            <el-option v-for="c in CATEGORIES" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>

          <el-select v-model="query.sortBy" class="filter-select" @change="reload">
            <el-option label="最新更新" value="latest" />
            <el-option label="评分最高" value="score" />
            <el-option label="浏览最多" value="views" />
          </el-select>

          <el-button type="primary" @click="reload">搜索</el-button>
        </div>

        <div class="filter-right">
          <span class="result-count">共 {{ total }} 个项目</span>
        </div>
      </div>

      <div v-if="loading" class="card-grid">
        <el-skeleton v-for="i in 6" :key="i" animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 148px" />
            <div style="padding: 14px 16px">
              <el-skeleton-item variant="h3" style="width: 60%" />
              <el-skeleton-item variant="text" style="margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 80%" />
            </div>
          </template>
        </el-skeleton>
      </div>

      <div v-else-if="projects.length" class="card-grid">
        <ProjectCard v-for="p in projects" :key="p.id" :project="p" show-meta />
      </div>

      <el-empty v-else description="没有找到匹配的项目，换个关键词试试" />

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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import ProjectCard from '@/components/ProjectCard.vue'
import { projectApi } from '@/api'
import { CATEGORIES } from '@/utils/dict'

const loading = ref(true)
const projects = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: '',
  category: '',
  sortBy: 'latest'
})

const load = async () => {
  loading.value = true
  try {
    const data = await projectApi.search({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      category: query.category || undefined,
      sortBy: query.sortBy
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

onMounted(load)
</script>

<style scoped>
.page-hero {
  padding: 40px 0 28px;
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
  line-height: 1.8;
  color: var(--xz-text-secondary);
  max-width: 720px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 26px 0 22px;
  flex-wrap: wrap;
}

.filter-left {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 280px;
}

.filter-select {
  width: 140px;
}

.result-count {
  font-size: 13px;
  color: var(--xz-text-secondary);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.pagination-wrap {
  margin-top: 36px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1200px) {
  .card-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .search-input {
    width: 100%;
  }

  .filter-left {
    width: 100%;
  }
}

@media (max-width: 600px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
