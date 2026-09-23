<template>
  <article class="project-card" @click="goDetail">
    <div class="card-cover" :style="coverStyle">
      <img v-if="project.coverUrl" :src="project.coverUrl" :alt="project.name" />
      <div v-else class="cover-placeholder">
        <el-icon :size="34"><Files /></el-icon>
        <span>{{ categoryText }}</span>
      </div>
      <div class="cover-badges">
        <span v-if="project.expertAvgScore > 0" class="badge badge-score">
          {{ Number(project.expertAvgScore).toFixed(1) }}
        </span>
        <span v-if="project.inReviewPool === 1" class="badge badge-pool">求点评</span>
      </div>
    </div>

    <div class="card-body">
      <h3 class="card-title" :title="project.name">{{ project.name }}</h3>
      <p class="card-summary">{{ project.summary || '作者暂未填写简介' }}</p>

      <div v-if="techList.length" class="card-tags">
        <span v-for="tag in techList.slice(0, 3)" :key="tag" class="tech-tag">{{ tag }}</span>
        <span v-if="techList.length > 3" class="tech-tag tech-tag-more">+{{ techList.length - 3 }}</span>
      </div>

      <div class="card-footer">
        <div class="author" @click.stop="goPortfolio">
          <el-avatar :size="24" :src="project.studentAvatar">
            {{ (project.studentName || '同').slice(0, 1) }}
          </el-avatar>
          <span class="author-name">{{ project.studentName || '匿名同学' }}</span>
          <el-tag v-if="project.eduVerified === 2" size="small" type="success" effect="plain" round>
            已认证
          </el-tag>
        </div>
        <div class="metrics">
          <span class="metric">
            <el-icon><ChatDotSquare /></el-icon>
            {{ project.expertReviewCount || 0 }}
          </span>
          <span class="metric">
            <el-icon><View /></el-icon>
            {{ project.viewCount || 0 }}
          </span>
        </div>
      </div>

      <div v-if="showMeta" class="card-meta">
        <span>{{ project.studentSchool || '—' }}</span>
        <span class="dot">·</span>
        <span>{{ project.studentMajor || '—' }}</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { CATEGORY_TEXT, formatDate } from '@/utils/dict'

const props = defineProps({
  project: { type: Object, required: true },
  showMeta: { type: Boolean, default: false }
})

const router = useRouter()

const categoryText = computed(() => CATEGORY_TEXT[props.project.category] || '项目')

const techList = computed(() => {
  const t = props.project.techStack
  if (Array.isArray(t)) return t
  if (typeof t === 'string' && t) return t.split(',').map((s) => s.trim()).filter(Boolean)
  return []
})

const coverStyle = computed(() => {
  const palette = ['#e6f2f1', '#fdf0e7', '#eef2f4', '#f3eef7']
  const seed = (props.project.id || 0) % palette.length
  return { background: palette[seed] }
})

const goDetail = () => router.push(`/project/${props.project.id}`)
const goPortfolio = () => {
  if (props.project.studentId) {
    router.push(`/portfolio/${props.project.studentId}`)
  }
}
</script>

<style scoped>
.project-card {
  display: flex;
  flex-direction: column;
  background: var(--xz-bg-card);
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.project-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--xz-shadow);
  border-color: #d5e3e2;
}

.card-cover {
  position: relative;
  height: 148px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--xz-primary);
  opacity: 0.7;
  font-size: 12px;
}

.cover-badges {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 6px;
}

.badge {
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.badge-score {
  background: rgba(31, 111, 107, 0.92);
  color: #fff;
}

.badge-pool {
  background: rgba(224, 122, 63, 0.92);
  color: #fff;
  font-weight: 500;
}

.card-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-summary {
  margin-top: 6px;
  font-size: 13px;
  color: var(--xz-text-secondary);
  line-height: 1.55;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.tech-tag {
  font-size: 11px;
  color: var(--xz-primary-dark);
  background: var(--xz-primary-lighter);
  padding: 2px 8px;
  border-radius: 4px;
}

.tech-tag-more {
  background: #eef2f4;
  color: var(--xz-text-secondary);
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--xz-border-light);
  margin-top: 12px;
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.author-name {
  font-size: 12px;
  color: var(--xz-text-secondary);
  max-width: 84px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metrics {
  display: flex;
  gap: 12px;
  color: var(--xz-text-placeholder);
  font-size: 12px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 3px;
}

.card-meta {
  margin-top: 8px;
  font-size: 12px;
  color: var(--xz-text-placeholder);
  display: flex;
  align-items: center;
  gap: 5px;
}

.dot {
  opacity: 0.6;
}
</style>
