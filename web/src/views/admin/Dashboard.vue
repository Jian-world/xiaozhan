<template>
  <div class="admin-dashboard" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>平台总览</h2>
        <p class="desc">校栈平台核心运营指标实时概览</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新数据</el-button>
    </div>

    <!-- 六项核心指标 -->
    <div class="kpi-grid">
      <div class="kpi" v-for="k in kpis" :key="k.label" :style="{ '--c': k.color }">
        <div class="kpi-icon"><el-icon :size="22"><component :is="k.icon" /></el-icon></div>
        <div class="kpi-body">
          <span class="num">{{ k.value }}</span>
          <span class="lab">{{ k.label }}</span>
        </div>
        <span v-if="k.extra" class="extra">{{ k.extra }}</span>
      </div>
    </div>

    <!-- 待办统计 -->
    <div class="alert-row" v-if="overview.pendingAuditCount || overview.poolProjectCount">
      <div class="alert-card" @click="$router.push('/admin/verifies')">
        <el-icon><Bell /></el-icon>
        <div>
          <b>待审核认证 {{ overview.pendingAuditCount || 0 }} 条</b>
          <p>学生学籍认证、专家认证、企业认证待处理</p>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="alert-card" @click="$router.push('/admin/audits')">
        <el-icon><DocumentChecked /></el-icon>
        <div>
          <b>求点评池待领取 {{ overview.poolProjectCount || 0 }} 个</b>
          <p>建议引导专家认领，提升平台背书覆盖率</p>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <section class="panel">
        <h3>专业大类分布</h3>
        <div ref="majorChartRef" class="chart"></div>
      </section>
      <section class="panel">
        <h3>项目类型分布</h3>
        <div ref="typeChartRef" class="chart"></div>
      </section>
    </div>

    <!-- 平台健康度 -->
    <div class="health-grid">
      <section class="panel">
        <h3>认证通过率</h3>
        <div class="health-body">
          <el-progress
            type="dashboard"
            :percentage="verifyRateNum"
            :width="150"
            color="#1f6f6b"
          />
          <div class="health-text">
            <div class="line">
              <span>已认证学生</span>
              <b>{{ overview.verifiedStudentCount || 0 }} 人</b>
            </div>
            <div class="line">
              <span>学生总数</span>
              <b>{{ overview.studentCount || 0 }} 人</b>
            </div>
            <div class="line">
              <span>认证通过率</span>
              <b class="hl">{{ verifyRateNum }}%</b>
            </div>
          </div>
        </div>
      </section>

      <section class="panel">
        <h3>供需结构</h3>
        <div class="supply-body">
          <div class="supply-item">
            <div class="supply-top">
              <span>学生</span><b>{{ overview.studentCount || 0 }}</b>
            </div>
            <el-progress
              :percentage="supplyPct(overview.studentCount)"
              :show-text="false"
              :stroke-width="10"
              color="#1f6f6b"
            />
          </div>
          <div class="supply-item">
            <div class="supply-top">
              <span>专家点评人</span><b>{{ overview.expertCount || 0 }}</b>
            </div>
            <el-progress
              :percentage="supplyPct(overview.expertCount)"
              :show-text="false"
              :stroke-width="10"
              color="#e07a3f"
            />
          </div>
          <div class="supply-item">
            <div class="supply-top">
              <span>企业</span><b>{{ overview.companyCount || 0 }}</b>
            </div>
            <el-progress
              :percentage="supplyPct(overview.companyCount)"
              :show-text="false"
              :stroke-width="10"
              color="#52b788"
            />
          </div>
          <div class="supply-item">
            <div class="supply-top">
              <span>项目作品</span><b>{{ overview.totalProjects || 0 }}</b>
            </div>
            <el-progress
              :percentage="supplyPct(overview.totalProjects)"
              :show-text="false"
              :stroke-width="10"
              color="#7c9fd4"
            />
          </div>
        </div>
      </section>

      <section class="panel">
        <h3>内容供需</h3>
        <div class="gap-body">
          <div class="gap-item">
            <span class="lab">公开项目</span>
            <span class="num">{{ overview.publishedProjects || 0 }}</span>
            <span class="of">/ {{ overview.totalProjects || 0 }}</span>
          </div>
          <div class="gap-item">
            <span class="lab">专家点评总数</span>
            <span class="num accent">{{ overview.reviewCount || 0 }}</span>
          </div>
          <div class="gap-item">
            <span class="lab">平均每项目点评</span>
            <span class="num">{{ avgReviewPerProject }}</span>
          </div>
          <div class="gap-item">
            <span class="lab">评价覆盖率</span>
            <span class="num">{{ reviewCoverage }}%</span>
          </div>
          <p class="gap-note">
            评价覆盖率 = 已被点评项目数 / 公开项目数，是平台背书能力的核心指标。
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Refresh, Bell, ArrowRight, DocumentChecked } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api'

const loading = ref(false)
const overview = ref({})

const majorChartRef = ref()
const typeChartRef = ref()
let majorChart = null
let typeChart = null

const kpis = computed(() => {
  const o = overview.value
  return [
    { label: '注册学生', value: o.studentCount ?? 0, icon: 'User', color: '#1f6f6b' },
    { label: '专家点评人', value: o.expertCount ?? 0, icon: 'Medal', color: '#e07a3f' },
    { label: '认证企业', value: o.companyCount ?? 0, icon: 'OfficeBuilding', color: '#52b788' },
    { label: '项目作品', value: o.totalProjects ?? 0, icon: 'Folder', color: '#7c9fd4' },
    { label: '专家点评', value: o.reviewCount ?? 0, icon: 'ChatDotRound', color: '#b07cd4' },
    { label: '已认证学生', value: o.verifiedStudentCount ?? 0, icon: 'CircleCheck', color: '#e6a23c' }
  ]
})

const verifyRateNum = computed(() => {
  const r = overview.value.verifyRate
  if (r === undefined || r === null) {
    const s = overview.value.studentCount || 0
    const v = overview.value.verifiedStudentCount || 0
    return s ? Math.round((v / s) * 100) : 0
  }
  return r <= 1 ? Math.round(r * 100) : Math.round(r)
})

const maxSupply = computed(() => {
  const o = overview.value
  return Math.max(
    o.studentCount || 0, o.expertCount || 0,
    o.companyCount || 0, o.totalProjects || 0, 1
  )
})

const avgReviewPerProject = computed(() => {
  const t = overview.value.publishedProjects || 0
  const r = overview.value.reviewCount || 0
  if (!t) return '—'
  return (r / t).toFixed(2)
})

const reviewCoverage = computed(() => {
  const t = overview.value.publishedProjects || 0
  const r = overview.value.reviewCount || 0
  if (!t) return 0
  return Math.min(100, Math.round((r / t) * 100))
})

function supplyPct(v) {
  return Math.round(((v || 0) / maxSupply.value) * 100)
}

function toChartData(dist) {
  if (!dist) return { labels: [], values: [] }
  if (Array.isArray(dist)) {
    return {
      labels: dist.map((d) => d.name || d.label || d.key || '其他'),
      values: dist.map((d) => d.value ?? d.count ?? 0)
    }
  }
  return { labels: Object.keys(dist), values: Object.values(dist) }
}

function renderCharts() {
  const palette = ['#1f6f6b', '#35968f', '#52b788', '#8ec9a8', '#e07a3f', '#eaa97c', '#7c9fd4', '#b07cd4']

  const major = toChartData(overview.value.majorDistribution)
  if (majorChartRef.value) {
    if (!majorChart) majorChart = echarts.init(majorChartRef.value)
    majorChart.setOption({
      tooltip: { trigger: 'item' },
      legend: {
        bottom: 0,
        icon: 'circle',
        textStyle: { color: '#55706e', fontSize: 12 }
      },
      series: [
        {
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '44%'],
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { color: '#55706e', fontSize: 12 },
          data: major.labels.length
            ? major.labels.map((l, i) => ({
                name: l,
                value: major.values[i],
                itemStyle: { color: palette[i % palette.length] }
              }))
            : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e8efee' } }]
        }
      ]
    })
  }

  const type = toChartData(overview.value.projectTypeDistribution)
  if (typeChartRef.value) {
    if (!typeChart) typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      grid: { left: 8, right: 16, top: 16, bottom: 8, containLabel: true },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#eef2f2' } },
        axisLabel: { color: '#9aa8a7' }
      },
      yAxis: {
        type: 'category',
        data: type.labels,
        axisLine: { lineStyle: { color: '#e4ebea' } },
        axisLabel: { color: '#55706e', fontSize: 12 }
      },
      series: [
        {
          type: 'bar',
          barWidth: 16,
          itemStyle: {
            borderRadius: [0, 8, 8, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#2a8a84' },
              { offset: 1, color: '#8ec9a8' }
            ])
          },
          data: type.values
        }
      ]
    })
  }
}

function resizeCharts() {
  majorChart?.resize()
  typeChart?.resize()
}

async function load() {
  loading.value = true
  try {
    const res = await adminApi.overview()
    overview.value = res || {}
    await nextTick()
    renderCharts()
  } catch (e) {
    /* 已提示 */
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  majorChart?.dispose()
  typeChart?.dispose()
})
</script>

<style scoped lang="scss">
.admin-dashboard {
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

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;

  @media (max-width: 1400px) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.kpi {
  background: #fff;
  border-radius: var(--xz-radius-lg);
  padding: 20px;
  box-shadow: var(--xz-shadow-sm);
  display: flex;
  align-items: center;
  gap: 14px;
  border-top: 3px solid var(--c);

  .kpi-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: color-mix(in srgb, var(--c) 12%, #fff);
    color: var(--c);
    flex-shrink: 0;
  }

  .kpi-body {
    min-width: 0;

    .num {
      display: block;
      font-size: 24px;
      font-weight: 700;
      color: var(--xz-text-primary);
      line-height: 1.2;
    }

    .lab {
      font-size: 12px;
      color: #8a9a99;
    }
  }
}

.alert-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 18px;
}

.alert-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: #fff8ee;
  border: 1px solid #f6e2c4;
  border-radius: var(--xz-radius-lg);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 22px rgba(224, 122, 63, 0.14);
    transform: translateY(-1px);
  }

  .el-icon {
    font-size: 22px;
    color: #e6a23c;
  }

  b {
    display: block;
    font-size: 14px;
    color: #8a6534;
  }

  p {
    margin: 3px 0 0;
    font-size: 12px;
    color: #a8865c;
  }

  .arrow {
    margin-left: auto;
    color: #c9a978;
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
  margin-top: 18px;
}

.health-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-top: 18px;

  @media (max-width: 1400px) {
    grid-template-columns: 1fr;
  }
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
  }
}

.chart {
  height: 280px;
  width: 100%;
}

.health-body {
  display: flex;
  align-items: center;
  gap: 24px;
}

.health-text {
  flex: 1;

  .line {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
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

      &.hl {
        color: var(--xz-primary);
        font-size: 15px;
      }
    }
  }
}

.supply-body {
  .supply-item {
    margin-bottom: 18px;

    &:last-child {
      margin-bottom: 0;
    }

    .supply-top {
      display: flex;
      justify-content: space-between;
      margin-bottom: 7px;

      span {
        font-size: 13px;
        color: #8a9a99;
      }

      b {
        font-size: 14px;
        color: var(--xz-text-primary);
      }
    }
  }
}

.gap-body {
  .gap-item {
    display: flex;
    align-items: baseline;
    gap: 6px;
    padding: 11px 0;
    border-bottom: 1px dashed #f0f4f4;

    &:last-of-type {
      border-bottom: none;
    }

    .lab {
      flex: 1;
      font-size: 13px;
      color: #8a9a99;
    }

    .num {
      font-size: 20px;
      font-weight: 700;
      color: var(--xz-text-primary);

      &.accent {
        color: var(--xz-accent);
      }
    }

    .of {
      font-size: 12px;
      color: #b6c2c1;
    }
  }

  .gap-note {
    margin: 12px 0 0;
    font-size: 12px;
    line-height: 1.7;
    color: #9aa8a7;
  }
}
</style>
