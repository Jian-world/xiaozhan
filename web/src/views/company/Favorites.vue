<template>
  <div class="company-favorites" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>候选人收藏夹</h2>
        <p class="desc">集中管理你关注的候选人，随时回到档案继续评估</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div v-if="list.length" class="fav-grid">
      <div v-for="item in list" :key="item.studentId" class="fav-card">
        <div class="card-head">
          <el-avatar :size="52" :src="item.avatar">
            {{ (item.nickname || '?').charAt(0) }}
          </el-avatar>
          <div class="who">
            <div class="name-row">
              <span class="name">{{ displayName(item) }}</span>
              <el-icon v-if="item.eduVerified" class="verified"><CircleCheckFilled /></el-icon>
            </div>
            <div class="sub">{{ item.school || '—' }} · {{ item.major || '—' }}</div>
            <div class="tags" v-if="tagsOf(item).length">
              <el-tag v-for="t in tagsOf(item)" :key="t" size="small" effect="plain">{{ t }}</el-tag>
            </div>
          </div>
        </div>

        <p class="bio">{{ item.bio || '暂无自述' }}</p>

        <div class="metrics">
          <div class="m">
            <b>{{ item.projectCount || 0 }}</b>
            <span>公开项目</span>
          </div>
          <div class="m">
            <b>{{ item.avgScore ? Number(item.avgScore).toFixed(1) : '—' }}</b>
            <span>专家均分</span>
          </div>
          <div class="m">
            <b>{{ item.totalReviews || 0 }}</b>
            <span>收到点评</span>
          </div>
        </div>

        <div class="fav-time">
          <el-icon><Clock /></el-icon>
          收藏于 {{ formatDate(item.favoriteTime) }}
        </div>

        <div class="card-actions">
          <el-button size="small" @click="goDetail(item.studentId)">查看档案</el-button>
          <el-button size="small" type="danger" plain @click="remove(item)">移出收藏</el-button>
        </div>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="收藏夹还是空的，去人才检索发现合适的候选人吧">
      <el-button type="primary" @click="$router.push('/company/talent')">前往人才检索</el-button>
    </el-empty>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, CircleCheckFilled, Clock } from '@element-plus/icons-vue'
import { companyApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)

function displayName(item) {
  return item.eduVerified ? (item.realName || item.nickname) : item.nickname
}

function tagsOf(item) {
  if (!item.skillTags) return []
  return String(item.skillTags)
    .split(/[,，、\s]+/)
    .filter(Boolean)
    .slice(0, 4)
}

function formatDate(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const res = await companyApi.favorites({ pageNum: pageNum.value, pageSize: pageSize.value })
    // 兼容: 数组 / {records,total} / {list,total}
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

function onPageChange(p) {
  pageNum.value = p
  load()
}

function goDetail(studentId) {
  router.push(`/company/candidate/${studentId}`)
}

async function remove(item) {
  try {
    await ElMessageBox.confirm(
      `确认将「${displayName(item)}」移出收藏夹？`,
      '移出收藏',
      { type: 'warning' }
    )
  } catch (e) {
    return
  }
  try {
    await companyApi.unfavorite(item.studentId)
    ElMessage.success('已移出收藏夹')
    load()
  } catch (e) {
    /* 已提示 */
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.company-favorites {
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

.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;

  @media (max-width: 1280px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.fav-card {
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 20px;
  box-shadow: var(--xz-shadow-sm);
  display: flex;
  flex-direction: column;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 26px rgba(31, 111, 107, 0.12);
  }
}

.card-head {
  display: flex;
  gap: 14px;
}

.who {
  flex: 1;
  min-width: 0;

  .name-row {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .name {
    font-size: 16px;
    font-weight: 600;
    color: var(--xz-text-primary);
  }

  .verified {
    color: #52b788;
    font-size: 15px;
  }

  .sub {
    margin-top: 5px;
    font-size: 12px;
    color: #8a9a99;
  }

  .tags {
    margin-top: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
  }
}

.bio {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--xz-text-regular);
  min-height: 44px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.metrics {
  display: flex;
  margin-top: 16px;
  padding: 12px 0;
  border-top: 1px dashed #eef2f2;
  border-bottom: 1px dashed #eef2f2;

  .m {
    flex: 1;
    text-align: center;

    b {
      display: block;
      font-size: 17px;
      color: var(--xz-text-primary);
    }

    span {
      font-size: 11px;
      color: #9aa8a7;
    }
  }
}

.fav-time {
  margin-top: 12px;
  font-size: 12px;
  color: #9aa8a7;
  display: flex;
  align-items: center;
  gap: 5px;
}

.card-actions {
  margin-top: 14px;
  display: flex;
  gap: 10px;

  .el-button {
    flex: 1;
  }
}

.pager {
  margin-top: 26px;
  display: flex;
  justify-content: center;
}
</style>
