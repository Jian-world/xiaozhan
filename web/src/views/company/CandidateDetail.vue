<template>
  <div class="candidate-detail" v-loading="loading">
    <div class="page-head">
      <div>
        <el-button link :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        <h2>候选人完整档案</h2>
      </div>
      <div class="head-actions">
        <el-button
          :type="isFav ? 'warning' : 'default'"
          :icon="isFav ? StarFilled : Star"
          @click="toggleFavorite"
        >
          {{ isFav ? '已收藏' : '加入收藏' }}
        </el-button>
        <el-button type="primary" :icon="Promotion" @click="inviteDialog = true">
          发送邀约
        </el-button>
      </div>
    </div>

    <template v-if="candidate">
      <!-- 档案概览 -->
      <div class="profile-hero">
        <div class="hero-left">
          <el-avatar :size="76" :src="candidate.avatar">
            {{ (candidate.nickname || '?').charAt(0) }}
          </el-avatar>
          <div class="hero-meta">
            <div class="name-row">
              <span class="name">{{ displayName }}</span>
              <el-tag v-if="candidate.eduVerified" type="success" size="small" effect="light">
                学籍已认证
              </el-tag>
              <el-tag v-else size="small" effect="plain">学籍未认证</el-tag>
            </div>
            <div class="sub">
              {{ candidate.school }} · {{ candidate.major }}
              <span v-if="candidate.degree"> · {{ candidate.degree }}</span>
              <span v-if="candidate.graduateYear"> · {{ candidate.graduateYear }} 届</span>
            </div>
            <div class="tags" v-if="skillList.length">
              <el-tag
                v-for="t in skillList"
                :key="t"
                size="small"
                effect="plain"
                class="skill-tag"
              >
                {{ t }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="hero-metrics">
          <div class="metric">
            <span class="num">{{ candidate.projectCount || 0 }}</span>
            <span class="lab">公开项目</span>
          </div>
          <div class="metric">
            <span class="num">{{ recentReviews.length }}</span>
            <span class="lab">公开点评</span>
          </div>
          <div class="metric">
            <span class="num">{{ scoreText }}</span>
            <span class="lab">专家均分</span>
          </div>
          <div class="metric">
            <span class="num">{{ candidate.portfolioViews || 0 }}</span>
            <span class="lab">作品集查阅</span>
          </div>
        </div>
      </div>

      <div class="detail-grid">
        <div class="detail-main">
          <!-- 自述 -->
          <section class="panel">
            <h3>个人自述</h3>
            <p class="bio">{{ candidate.bio || '该同学暂未填写个人自述。' }}</p>
          </section>

          <!-- 项目作品 -->
          <section class="panel">
            <h3>
              项目作品
              <span class="count">{{ projects.length }}</span>
            </h3>
            <div v-if="projects.length" class="project-list">
              <div
                v-for="p in projects"
                :key="p.id"
                class="project-item"
                @click="$router.push(`/project/${p.id}`)"
              >
                <div class="cover">
                  <img v-if="p.coverUrl" :src="p.coverUrl" :alt="p.name" />
                  <div v-else class="cover-ph">
                    <el-icon><Picture /></el-icon>
                  </div>
                </div>
                <div class="info">
                  <div class="title-row">
                    <span class="title">{{ p.name }}</span>
                    <el-tag size="small" effect="plain">{{ p.projectType || '项目' }}</el-tag>
                  </div>
                  <p class="summary">{{ p.summary }}</p>
                  <div class="meta-row">
                    <span class="tech" v-if="techText(p.techStack)">{{ techText(p.techStack) }}</span>
                  </div>
                  <div class="stat-row">
                    <span>
                      <el-icon><View /></el-icon> {{ p.viewCount || 0 }}
                    </span>
                    <span>
                      <el-icon><ChatDotRound /></el-icon> {{ p.expertReviewCount || 0 }}
                    </span>
                    <span v-if="p.expertAvgScore" class="score">
                      <el-icon><StarFilled /></el-icon> {{ Number(p.expertAvgScore).toFixed(1) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="该同学暂无公开项目" :image-size="80" />
          </section>

          <!-- 能力背书 -->
          <section class="panel">
            <h3>
              能力背书
              <span class="count">{{ recentReviews.length }}</span>
            </h3>
            <div v-if="recentReviews.length" class="review-list">
              <div v-for="r in recentReviews" :key="r.id" class="review-item">
                <div class="review-head">
                  <div class="expert">
                    <el-avatar :size="32">
                      {{ (r.expertName || '专家').charAt(0) }}
                    </el-avatar>
                    <div class="expert-meta">
                      <div class="expert-name">
                        {{ r.expertName }}
                      </div>
                      <div class="expert-org">{{ r.projectName || '专家点评' }}</div>
                    </div>
                  </div>
                  <div class="review-score">
                    <span class="avg">{{ Number(r.avgScore || 0).toFixed(1) }}</span>
                    <span class="unit">/ 5</span>
                  </div>
                </div>
                <p class="comment">{{ r.comment }}</p>
                <div class="review-foot" v-if="r.strength || r.weakness || r.suggestion">
                  <div v-if="r.strength" class="foot-line">
                    <em>亮点</em>{{ r.strength }}
                  </div>
                  <div v-if="r.weakness" class="foot-line">
                    <em>不足</em>{{ r.weakness }}
                  </div>
                  <div v-if="r.suggestion" class="foot-line">
                    <em>建议</em>{{ r.suggestion }}
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无专家点评记录" :image-size="80" />
          </section>
        </div>

        <aside class="detail-side">
          <div class="panel">
            <h3>在校信息</h3>
            <ul class="side-list">
              <li>
                <span>学校</span>
                <b>{{ candidate.school || '—' }}</b>
              </li>
              <li>
                <span>专业</span>
                <b>{{ candidate.major || '—' }}</b>
              </li>
              <li>
                <span>专业大类</span>
                <b>{{ candidate.majorCategory || '—' }}</b>
              </li>
              <li>
                <span>学历</span>
                <b>{{ candidate.degree || '—' }}</b>
              </li>
              <li>
                <span>毕业年份</span>
                <b>{{ candidate.graduateYear || '—' }}</b>
              </li>
            </ul>
          </div>

          <div class="panel">
            <h3>查验信息</h3>
            <ul class="side-list">
              <li>
                <span>本次查验消耗</span>
                <b>1 次额度</b>
              </li>
              <li>
                <span>剩余额度</span>
                <b>{{ remainQuota }}</b>
              </li>
            </ul>
            <p class="side-note">
              查验记录已留档。候选人可查看哪些企业查阅过其档案。
            </p>
          </div>

          <div class="panel">
            <h3>快捷操作</h3>
            <div class="side-btns">
              <el-button
                style="width: 100%"
                :type="isFav ? 'warning' : 'default'"
                @click="toggleFavorite"
              >
                {{ isFav ? '取消收藏' : '加入收藏' }}
              </el-button>
              <el-button style="width: 100%; margin: 10px 0 0" type="primary" @click="inviteDialog = true">
                发送面试邀约
              </el-button>
            </div>
          </div>
        </aside>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="未找到该候选人" />

    <!-- 邀约弹窗 -->
    <el-dialog v-model="inviteDialog" title="发送面试邀约" width="520px">
      <el-form :model="inviteForm" label-width="90px">
        <el-form-item label="应聘岗位" required>
          <el-input v-model="inviteForm.jobTitle" placeholder="如：后端开发工程师（校招）" />
        </el-form-item>
        <el-form-item label="邀约内容" required>
          <el-input
            v-model="inviteForm.content"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="简要说明岗位职责、团队情况与联系方式"
          />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="inviteForm.contactInfo" placeholder="邮箱 / 电话 / 微信" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteDialog = false">取消</el-button>
        <el-button type="primary" :loading="inviting" @click="submitInvite">发送邀约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Star, StarFilled, Promotion, Picture, View, ChatDotRound
} from '@element-plus/icons-vue'
import { companyApi, invitationApi } from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const candidate = ref(null)
const projects = ref([])
const recentReviews = ref([])
const isFav = ref(false)
const remainQuota = ref('—')

const inviteDialog = ref(false)
const inviting = ref(false)
const inviteForm = ref({ jobTitle: '', content: '', contactInfo: '' })

const studentId = computed(() => route.params.studentId)

const displayName = computed(() => {
  const c = candidate.value
  if (!c) return ''
  return c.eduVerified ? (c.realName || c.nickname) : c.nickname
})

const skillList = computed(() => {
  const raw = candidate.value?.skillTags
  if (!raw) return []
  return String(raw)
    .split(/[,，、\s]+/)
    .filter(Boolean)
    .slice(0, 12)
})

const scoreText = computed(() => {
  const c = candidate.value
  if (!c || !c.avgScore) return '—'
  return Number(c.avgScore).toFixed(1)
})

function techText(t) {
  if (!t) return ''
  if (Array.isArray(t)) return t.join(' · ')
  return String(t)
}

async function load() {
  loading.value = true
  try {
    const res = await companyApi.candidateDetail(studentId.value)
    candidate.value = res.candidate || res
    projects.value = res.projects || []
    recentReviews.value = res.recentReviews || []
    isFav.value = !!candidate.value?.favorited

    try {
      const stat = await companyApi.statistics()
      remainQuota.value = `${stat.remainQuota ?? 0} / ${stat.monthQuota ?? 0}`
    } catch (e) {
      remainQuota.value = '—'
    }
  } catch (e) {
    candidate.value = null
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  try {
    if (isFav.value) {
      await companyApi.unfavorite(studentId.value)
      isFav.value = false
      ElMessage.success('已取消收藏')
    } else {
      await companyApi.favorite({ studentId: studentId.value })
      isFav.value = true
      ElMessage.success('已加入收藏')
    }
  } catch (e) {
    /* 已提示 */
  }
}

async function submitInvite() {
  if (!inviteForm.value.jobTitle.trim()) {
    ElMessage.warning('请填写应聘岗位')
    return
  }
  if (!inviteForm.value.content.trim()) {
    ElMessage.warning('请填写邀约内容')
    return
  }
  inviting.value = true
  try {
    await companyApi.sendInvitation({
      studentId: studentId.value,
      jobTitle: inviteForm.value.jobTitle,
      content: inviteForm.value.content,
      contactInfo: inviteForm.value.contactInfo
    })
    ElMessage.success('邀约已发送')
    inviteDialog.value = false
    inviteForm.value = { jobTitle: '', content: '', contactInfo: '' }
    router.push('/company/invitations')
  } catch (e) {
    /* 已提示 */
  } finally {
    inviting.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.candidate-detail {
  padding-bottom: 30px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;

  h2 {
    display: inline-block;
    margin: 8px 0 0;
    font-size: 22px;
    color: var(--xz-text-primary);
  }
}

/* 概览 */
.profile-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 26px 30px;
  background: linear-gradient(120deg, #1f6f6b 0%, #2a8a84 60%, #35968f 100%);
  border-radius: var(--xz-radius-lg);
  color: #fff;
  box-shadow: 0 10px 28px rgba(31, 111, 107, 0.22);
}

.hero-left {
  display: flex;
  gap: 20px;
  align-items: center;
}

.hero-meta {
  .name-row {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .name {
    font-size: 22px;
    font-weight: 700;
  }

  .sub {
    margin-top: 8px;
    font-size: 13px;
    opacity: 0.88;
  }

  .tags {
    margin-top: 10px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .skill-tag {
    background: rgba(255, 255, 255, 0.16) !important;
    border-color: rgba(255, 255, 255, 0.3) !important;
    color: #fff !important;
  }
}

.hero-metrics {
  display: flex;
  gap: 34px;

  .metric {
    text-align: center;

    .num {
      display: block;
      font-size: 26px;
      font-weight: 700;
      color: #ffe9c9;
    }

    .lab {
      font-size: 12px;
      opacity: 0.85;
    }
  }
}

/* 栅格 */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  margin-top: 20px;
}

.detail-main {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.detail-side {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel {
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 22px 24px;
  box-shadow: var(--xz-shadow-sm);

  h3 {
    margin: 0 0 16px;
    font-size: 16px;
    color: var(--xz-text-primary);
    display: flex;
    align-items: center;
    gap: 8px;

    .count {
      font-size: 12px;
      font-weight: 400;
      color: #8a9a99;
      background: #f1f5f5;
      padding: 1px 8px;
      border-radius: 10px;
    }
  }
}

.bio {
  margin: 0;
  font-size: 14px;
  line-height: 1.85;
  color: var(--xz-text-regular);
  white-space: pre-wrap;
}

/* 项目列表 */
.project-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.project-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  border: 1px solid #eef2f2;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--xz-primary);
    box-shadow: 0 6px 18px rgba(31, 111, 107, 0.1);
  }

  .cover {
    width: 120px;
    height: 78px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f4f7f7;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-ph {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #c3cfce;
      font-size: 24px;
    }
  }

  .info {
    flex: 1;
    min-width: 0;
  }

  .title-row {
    display: flex;
    align-items: center;
    gap: 8px;

    .title {
      font-weight: 600;
      color: var(--xz-text-primary);
      font-size: 15px;
    }
  }

  .summary {
    margin: 6px 0;
    font-size: 13px;
    color: var(--xz-text-regular);
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .tech {
    font-size: 12px;
    color: #7d8f8e;
  }

  .stat-row {
    display: flex;
    gap: 16px;
    margin-top: 8px;
    font-size: 12px;
    color: #8a9a99;

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }

    .score {
      color: var(--xz-accent);
      font-weight: 600;
    }
  }
}

/* 点评 */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 18px;
  border: 1px solid #eef2f2;
  border-radius: 12px;
  background: #fbfcfc;
}

.review-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.expert {
  display: flex;
  gap: 10px;
  align-items: center;

  .expert-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--xz-text-primary);

    .expert-title {
      margin-left: 6px;
      font-size: 12px;
      font-weight: 400;
      color: #8a9a99;
    }
  }

  .expert-org {
    font-size: 12px;
    color: #8a9a99;
    margin-top: 2px;
  }
}

.review-score {
  .avg {
    font-size: 22px;
    font-weight: 700;
    color: var(--xz-accent);
  }

  .unit {
    font-size: 12px;
    color: #9aa8a7;
  }
}

.project-ref {
  margin-top: 12px;
  font-size: 12px;
  color: #7d8f8e;
}

.dim-scores {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;

  span {
    font-size: 12px;
    color: #55706e;
    background: #eef5f4;
    padding: 3px 10px;
    border-radius: 12px;
  }
}

.comment {
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.85;
  color: var(--xz-text-regular);
  white-space: pre-wrap;
}

.review-foot {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e4ebea;

  .foot-line {
    font-size: 13px;
    line-height: 1.8;
    color: var(--xz-text-regular);
    margin-bottom: 4px;

    em {
      font-style: normal;
      display: inline-block;
      min-width: 40px;
      color: var(--xz-primary);
      font-weight: 600;
      margin-right: 6px;
    }
  }
}

/* 侧栏 */
.side-list {
  list-style: none;
  margin: 0;
  padding: 0;

  li {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 9px 0;
    font-size: 13px;
    border-bottom: 1px dashed #f0f4f4;

    &:last-child {
      border-bottom: none;
    }

    span {
      color: #8a9a99;
    }

    b {
      color: var(--xz-text-primary);
      font-weight: 600;
    }
  }
}

.side-note {
  margin: 12px 0 0;
  font-size: 12px;
  line-height: 1.7;
  color: #9aa8a7;
}

.side-btns {
  display: flex;
  flex-direction: column;
}
</style>
