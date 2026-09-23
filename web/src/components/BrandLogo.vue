<template>
  <div class="xz-brand" :class="{ 'is-compact': compact }" @click="goHome">
    <div class="brand-mark">
      <svg viewBox="0 0 32 32" width="100%" height="100%" aria-hidden="true">
        <rect x="1.5" y="1.5" width="29" height="29" rx="8" fill="var(--xz-primary)" />
        <path
          d="M9 11.2h14M9 16h14M9 20.8h9"
          stroke="#ffffff"
          stroke-width="2"
          stroke-linecap="round"
        />
        <circle cx="22.5" cy="20.8" r="2.4" fill="var(--xz-accent)" />
      </svg>
    </div>
    <div v-if="!compact" class="brand-text">
      <span class="brand-name">校栈</span>
      <span class="brand-slogan">作品即能力背书</span>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  compact: { type: Boolean, default: false }
})

const router = useRouter()

const goHome = () => router.push('/')
</script>

<style scoped>
.xz-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.brand-mark {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.brand-name {
  font-size: 19px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--xz-text-primary);
}

.brand-slogan {
  font-size: 11px;
  color: var(--xz-text-placeholder);
  letter-spacing: 0.5px;
}

.is-compact .brand-mark {
  width: 32px;
  height: 32px;
}
</style>
