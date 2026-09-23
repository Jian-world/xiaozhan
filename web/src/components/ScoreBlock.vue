<template>
  <div class="score-block">
    <div class="overall">
      <div class="overall-value">{{ displayAvg }}</div>
      <div class="overall-label">
        <span>{{ scoreText(review.avgScore) }}</span>
        <span class="overall-count">{{ count }} 位专家点评</span>
      </div>
    </div>

    <div v-if="showDimensions" class="dimensions">
      <div v-for="dim in SCORE_DIMENSIONS" :key="dim.key" class="dimension">
        <div class="dim-head">
          <span class="dim-label">{{ dim.label }}</span>
          <span class="dim-score">{{ review[dim.key] || 0 }} / 5</span>
        </div>
        <el-progress
          :percentage="(review[dim.key] || 0) * 20"
          :stroke-width="6"
          :show-text="false"
          color="var(--xz-primary)"
        />
        <p v-if="showHints" class="dim-hint">{{ dim.hint }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { SCORE_DIMENSIONS, scoreText } from '@/utils/dict'

const props = defineProps({
  review: { type: Object, required: true },
  count: { type: Number, default: 1 },
  showDimensions: { type: Boolean, default: true },
  showHints: { type: Boolean, default: false }
})

const displayAvg = computed(() => {
  const v = Number(props.review.avgScore)
  return v ? v.toFixed(1) : '—'
})
</script>

<style scoped>
.score-block {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.overall {
  display: flex;
  align-items: center;
  gap: 14px;
}

.overall-value {
  font-size: 40px;
  font-weight: 700;
  line-height: 1;
  color: var(--xz-primary);
  font-variant-numeric: tabular-nums;
}

.overall-label {
  display: flex;
  flex-direction: column;
  font-size: 13px;
  color: var(--xz-text-primary);
  font-weight: 500;
}

.overall-count {
  font-size: 12px;
  color: var(--xz-text-placeholder);
  font-weight: 400;
}

.dimensions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px 22px;
}

.dim-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 6px;
}

.dim-label {
  font-size: 13px;
  color: var(--xz-text-primary);
  font-weight: 500;
}

.dim-score {
  font-size: 12px;
  color: var(--xz-text-secondary);
  font-variant-numeric: tabular-nums;
}

.dim-hint {
  margin-top: 5px;
  font-size: 11px;
  color: var(--xz-text-placeholder);
  line-height: 1.5;
}
</style>
