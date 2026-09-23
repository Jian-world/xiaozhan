<template>
  <div v-loading="pageLoading" class="project-edit-page">
    <div class="page-head">
      <div class="head-left">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div>
          <h2 class="page-title">{{ isEdit ? '编辑项目' : '新建项目' }}</h2>
          <p class="page-sub">
            {{ isEdit ? '修改后如项目已发布，将重新进入内容审核' : '先保存为草稿，整理好素材后再发布' }}
          </p>
        </div>
      </div>

      <div class="head-actions">
        <el-button :loading="saving" @click="onSave(false)">保存草稿</el-button>
        <el-button type="primary" :loading="saving" @click="onSave(true)">
          保存并发布
        </el-button>
      </div>
    </div>

    <div class="edit-body">
      <div class="edit-main">
        <!-- 基本信息 -->
        <section class="form-block">
          <h3 class="block-title">基本信息</h3>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="项目名称" prop="name">
              <el-input
                v-model="form.name"
                placeholder="如：校园二手交易平台 —— 让闲置物品流转起来"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>

            <div class="form-row">
              <el-form-item label="项目方向" prop="category">
                <el-select v-model="form.category" placeholder="请选择方向" class="full-width">
                  <el-option v-for="c in CATEGORIES" :key="c.value" :label="c.label" :value="c.value" />
                </el-select>
              </el-form-item>

              <el-form-item label="项目类型" prop="projectType">
                <el-select v-model="form.projectType" placeholder="请选择类型" class="full-width">
                  <el-option
                    v-for="t in PROJECT_TYPES"
                    :key="t.value"
                    :label="t.label"
                    :value="t.value"
                  />
                </el-select>
              </el-form-item>
            </div>

            <el-form-item label="一句话简介" prop="summary">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="2"
                maxlength="200"
                show-word-limit
                placeholder="用一句话说清这个项目解决了什么问题，如：面向校园的二手交易平台，支持信用评价与线下面交"
              />
            </el-form-item>

            <el-form-item label="技术栈" prop="techStack">
              <div class="tag-editor">
                <el-tag
                  v-for="tag in techTags"
                  :key="tag"
                  closable
                  class="tech-chip"
                  @close="removeTech(tag)"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="techInputVisible"
                  ref="techInputRef"
                  v-model="techInput"
                  size="small"
                  class="tech-input"
                  placeholder="输入后回车"
                  @keyup.enter="confirmTech"
                  @blur="confirmTech"
                />
                <el-button v-else size="small" class="tech-add-btn" @click="showTechInput">
                  <el-icon><Plus /></el-icon>
                  添加技术
                </el-button>
              </div>
              <div class="field-hint">建议填写 3-8 个，如 SpringBoot、Redis、Vue3</div>
            </el-form-item>

            <div class="form-row">
              <el-form-item label="开始时间" prop="startDate">
                <el-date-picker
                  v-model="form.startDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择开始日期"
                  class="full-width"
                />
              </el-form-item>

              <el-form-item label="结束时间" prop="endDate">
                <el-date-picker
                  v-model="form.endDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择结束日期（进行中可不填）"
                  class="full-width"
                />
              </el-form-item>
            </div>

            <el-form-item label="担任角色 / 你的具体分工" prop="roleDesc">
              <el-input
                v-model="form.roleDesc"
                placeholder="如：3 人小组，我负责后端接口设计与订单模块开发"
              />
            </el-form-item>
          </el-form>
        </section>

        <!-- 技术亮点 -->
        <section class="form-block">
          <h3 class="block-title">技术难点与亮点</h3>
          <p class="block-desc">
            这是点评人最关注的部分。写清「遇到什么问题 → 怎么解决 → 效果如何」，
            比罗列功能更有说服力。支持插入图片、代码块与表格。
          </p>

          <div class="editor-wrap">
            <Toolbar
              class="editor-toolbar"
              :editor="editorRef"
              :default-config="toolbarConfig"
              mode="default"
            />
            <Editor
              v-model="form.highlight"
              class="editor-body"
              :default-config="editorConfig"
              mode="default"
              @on-created="onEditorCreated"
            />
          </div>
        </section>

        <!-- 素材 -->
        <section class="form-block">
          <h3 class="block-title">项目素材</h3>
          <p class="block-desc">
            源码包、设计文档、演示视频都可以上传。发布前至少需要上传一份素材。
            文件存储在平台服务器，单个视频最大 1GB。
          </p>

          <div class="asset-tabs">
            <div v-for="type in assetTypes" :key="type.value" class="asset-tab">
              <div class="asset-tab-head">
                <div class="asset-tab-title">
                  <el-icon><component :is="ASSET_TYPE_ICON[type.value]" /></el-icon>
                  <span>{{ type.label }}</span>
                  <span class="asset-limit">{{ currentCount(type.value) }} / {{ type.limit }}</span>
                </div>

                <el-upload
                  :show-file-list="false"
                  :accept="ASSET_TYPE_ACCEPT[type.value]"
                  :disabled="!canAddMore(type.value)"
                  :before-upload="(file) => beforeAssetUpload(file, type.value)"
                  :http-request="(opt) => doAssetUpload(opt, type.value)"
                >
                  <el-button
                    size="small"
                    :disabled="!canAddMore(type.value)"
                    :loading="uploading[type.value]"
                  >
                    <el-icon><Upload /></el-icon>
                    上传{{ type.label }}
                  </el-button>
                </el-upload>
              </div>

              <div v-if="assetsOf(type.value).length" class="asset-items">
                <div v-for="a in assetsOf(type.value)" :key="a.id" class="asset-item">
                  <el-icon class="asset-icon"><component :is="ASSET_TYPE_ICON[type.value]" /></el-icon>
                  <div class="asset-info">
                    <span class="asset-name">{{ a.fileName }}</span>
                    <span class="asset-meta">
                      {{ a.fileExt ? a.fileExt.toUpperCase() : '' }}
                      <template v-if="a.readableSize"> · {{ a.readableSize }}</template>
                    </span>
                  </div>
                  <div class="asset-ops">
                    <el-button size="small" text type="primary" @click="previewAsset(a, type.value)">
                      预览
                    </el-button>
                    <el-button size="small" text type="danger" @click="removeAsset(a)">删除</el-button>
                  </div>
                </div>
              </div>
              <p v-else class="asset-empty">还没有上传{{ type.label }}</p>
            </div>
          </div>
        </section>

        <!-- 发布设置 -->
        <section class="form-block">
          <h3 class="block-title">发布设置</h3>

          <el-form label-position="top">
            <el-form-item label="可见范围">
              <el-radio-group v-model="form.visibility" class="visibility-group">
                <el-radio v-for="v in VISIBILITY_OPTIONS" :key="v.value" :value="v.value" class="visibility-radio">
                  <div class="vis-body">
                    <span class="vis-label">{{ v.label }}</span>
                    <span class="vis-desc">{{ v.desc }}</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="求点评设置">
              <div class="pool-switch">
                <el-switch
                  v-model="poolEnabled"
                  active-text="投进求点评池"
                  inactive-text="暂不求点评"
                  inline-prompt
                />
                <span class="pool-hint">
                  投进后，项目会出现在专家的求点评池中，按「未被点评优先」排序被领取。
                </span>
              </div>
            </el-form-item>

            <el-form-item label="代码仓库地址">
              <div class="repo-row">
                <el-select v-model="form.repoPlatform" placeholder="平台" class="repo-platform">
                  <el-option label="GitHub" value="GITHUB" />
                  <el-option label="Gitee" value="GITEE" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
                <el-input
                  v-model="form.repoUrl"
                  placeholder="https://github.com/yourname/project"
                  class="repo-url"
                />
              </div>
              <div class="field-hint">
                如代码不便公开，可留空，改为在技术亮点中说明关键实现思路。
              </div>
            </el-form-item>

            <el-form-item label="封面图">
              <div class="cover-uploader">
                <div class="cover-preview" :style="form.coverUrl ? {} : { background: '#f6f8f9' }">
                  <img v-if="form.coverUrl" :src="form.coverUrl" alt="封面" />
                  <div v-else class="cover-placeholder">
                    <el-icon :size="22"><Picture /></el-icon>
                    <span>建议 16:9，用于列表卡片</span>
                  </div>
                </div>
                <div class="cover-ops">
                  <el-upload
                    :show-file-list="false"
                    accept=".jpg,.jpeg,.png,.gif,.webp,.bmp,.svg"
                    :before-upload="beforeCoverUpload"
                    :http-request="doCoverUpload"
                  >
                    <el-button size="small" :loading="coverUploading">上传封面</el-button>
                  </el-upload>
                  <el-button v-if="form.coverUrl" size="small" text type="danger" @click="form.coverUrl = ''">
                    移除
                  </el-button>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </section>
      </div>

      <!-- 右侧提示 -->
      <aside class="edit-side">
        <div class="side-card">
          <h4 class="side-title">写好一个项目的三个要点</h4>
          <ol class="tip-list">
            <li>
              <strong>说清你的贡献</strong>
              <span>团队项目要写明你负责的模块，避免笼统的「参与开发」。</span>
            </li>
            <li>
              <strong>展示取舍过程</strong>
              <span>为什么选 Redis 而不是本地缓存？说清权衡会大幅加分。</span>
            </li>
            <li>
              <strong>补上量化结果</strong>
              <span>接口响应从 800ms 降到 120ms，比「性能优化」有说服力得多。</span>
            </li>
          </ol>
        </div>

        <div class="side-card">
          <h4 class="side-title">点评人会看什么</h4>
          <div class="dimension-list">
            <div v-for="d in SCORE_DIMENSIONS" :key="d.key" class="dimension-item">
              <span class="dim-name">{{ d.label }}</span>
              <span class="dim-hint">{{ d.hint }}</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <el-dialog v-model="previewVisible" :title="previewTitle" width="820px" destroy-on-close>
      <video v-if="previewUrl && isVideo" :src="previewUrl" controls autoplay class="preview-video"></video>
      <img v-else-if="previewUrl" :src="previewUrl" class="preview-image" :alt="previewTitle" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { fileApi, projectApi } from '@/api'
import {
  ASSET_TYPE_ACCEPT,
  ASSET_TYPE_ICON,
  ASSET_TYPE_TEXT,
  CATEGORIES,
  PROJECT_TYPES,
  SCORE_DIMENSIONS,
  VISIBILITY_OPTIONS
} from '@/utils/dict'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const saving = ref(false)
const coverUploading = ref(false)
const uploading = reactive({})

const formRef = ref()

const projectId = computed(() => route.params.id)
const isEdit = computed(() => !!projectId.value)

const form = reactive({
  id: null,
  name: '',
  category: 'CS',
  projectType: 'COURSE',
  summary: '',
  roleDesc: '',
  techStack: '',
  highlight: '',
  repoUrl: '',
  repoPlatform: 'GITHUB',
  startDate: '',
  endDate: '',
  coverUrl: '',
  visibility: 'PUBLIC',
  inReviewPool: 0
})

const poolEnabled = ref(false)

const assets = ref([])

const assetTypes = [
  { value: 'SOURCE', label: '源码包', limit: 3 },
  { value: 'DOC', label: '设计文档', limit: 10 },
  { value: 'VIDEO', label: '演示视频', limit: 3 },
  { value: 'IMAGE', label: '效果图', limit: 20 }
]

/* ---------- 技术栈标签 ---------- */
const techInputVisible = ref(false)
const techInput = ref('')
const techInputRef = ref()

const techTags = computed(() =>
  (form.techStack || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
)

const showTechInput = () => {
  techInputVisible.value = true
  setTimeout(() => techInputRef.value?.focus(), 30)
}

const confirmTech = () => {
  const value = techInput.value.trim()
  if (value && !techTags.value.includes(value)) {
    form.techStack = [...techTags.value, value].join(',')
  }
  techInput.value = ''
  techInputVisible.value = false
}

const removeTech = (tag) => {
  form.techStack = techTags.value.filter((t) => t !== tag).join(',')
}

/* ---------- 富文本编辑器 ---------- */
const editorRef = shallowRef()

const toolbarConfig = {
  excludeKeys: ['group-video', 'insertTable', 'codeBlock']
}

const editorConfig = {
  placeholder: '例如：\n问题：订单高峰期出现超卖。\n方案：用 Redis + Lua 脚本做原子扣减，并把库存校验前置。\n结果：压测 500 并发下零超卖，下单接口 P99 从 420ms 降到 180ms。',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        try {
          const res = await fileApi.upload(file, 'RICH_TEXT')
          insertFn(res.url, file.name, res.url)
        } catch (e) {
          ElMessage.error('图片上传失败')
        }
      }
    }
  }
}

const onEditorCreated = (editor) => {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})

/* ---------- 素材 ---------- */
const assetsOf = (type) => assets.value.filter((a) => a.assetType === type)
const currentCount = (type) => assetsOf(type).length
const canAddMore = (type) => {
  const conf = assetTypes.find((t) => t.value === type)
  return conf ? currentCount(type) < conf.limit : false
}

const beforeAssetUpload = (file, type) => {
  const maxSizes = { SOURCE: 500, DOC: 100, VIDEO: 1024, IMAGE: 20 }
  const maxMb = maxSizes[type] || 100
  if (file.size / 1024 / 1024 > maxMb) {
    ElMessage.error(`文件体积超出限制（最大 ${maxMb}MB）`)
    return false
  }
  return true
}

const doAssetUpload = async (options, type) => {
  uploading[type] = true
  try {
    const res = await fileApi.upload(options.file, type)
    // 需要先有项目 ID 才能登记素材
    const id = await ensureProjectSaved()
    if (!id) return

    await projectApi.saveAsset(id, {
      assetType: type,
      fileName: res.fileName,
      fileUrl: res.url,
      fileSize: res.size,
      fileExt: res.ext
    })
    ElMessage.success(`${ASSET_TYPE_TEXT[type]}上传成功`)
    await reloadAssets(id)
  } catch (e) {
    /* 已提示 */
  } finally {
    uploading[type] = false
  }
}

/** 上传素材前确保项目已保存并拿到 ID */
const ensureProjectSaved = async () => {
  if (form.id) return form.id
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请先填写项目名称、方向与类型')
    return null
  }
  const id = await projectApi.create({ ...form })
  form.id = id
  router.replace(`/student/projects/edit/${id}`)
  return id
}

const reloadAssets = async (id) => {
  const detail = await projectApi.detail(id)
  assets.value = detail.assets || []
  if (detail.coverUrl) form.coverUrl = detail.coverUrl
}

const removeAsset = async (asset) => {
  try {
    await ElMessageBox.confirm(`确定删除「${asset.fileName}」吗？`, '删除素材', { type: 'warning' })
    await projectApi.deleteAsset(asset.id)
    ElMessage.success('已删除')
    await reloadAssets(form.id)
  } catch (e) {
    /* 取消或失败 */
  }
}

const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const isVideo = ref(false)

const previewAsset = (asset, type) => {
  previewUrl.value = asset.fileUrl
  previewTitle.value = asset.fileName
  isVideo.value = type === 'VIDEO'
  if (isVideo.value || type === 'IMAGE') {
    previewVisible.value = true
  } else {
    window.open(asset.fileUrl, '_blank', 'noopener')
  }
}

/* ---------- 封面 ---------- */
const beforeCoverUpload = (file) => {
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('封面图不超过 5MB')
    return false
  }
  return true
}

const doCoverUpload = async (options) => {
  coverUploading.value = true
  try {
    const res = await fileApi.upload(options.file, 'COVER')
    form.coverUrl = res.url
    ElMessage.success('封面已上传')
  } catch (e) {
    /* 已提示 */
  } finally {
    coverUploading.value = false
  }
}

/* ---------- 校验 ---------- */
const rules = {
  name: [{ required: true, message: '请填写项目名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择项目方向', trigger: 'change' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  summary: [{ required: true, message: '请填写一句话简介', trigger: 'blur' }]
}

/* ---------- 保存 / 发布 ---------- */
const onSave = async (publish) => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请先补全必填信息')
    return
  }

  saving.value = true
  try {
    form.inReviewPool = poolEnabled.value ? 1 : 0
    const payload = { ...form }

    if (form.id) {
      await projectApi.update(payload)
    } else {
      const id = await projectApi.create(payload)
      form.id = id
      router.replace(`/student/projects/edit/${id}`)
    }

    if (publish) {
      await projectApi.publish(form.id)
      ElMessage.success('保存成功，项目已提交发布审核')
      router.push('/student/projects')
    } else {
      ElMessage.success('已保存为草稿')
      await reloadAssets(form.id)
    }
  } catch (e) {
    /* 已提示 */
  } finally {
    saving.value = false
  }
}

/* ---------- 初始化 ---------- */
const load = async () => {
  if (!isEdit.value) return
  pageLoading.value = true
  try {
    const detail = await projectApi.detail(projectId.value, false)
    Object.assign(form, {
      id: detail.id,
      name: detail.name,
      category: detail.category,
      projectType: detail.projectType,
      summary: detail.summary,
      roleDesc: detail.roleDesc,
      techStack: Array.isArray(detail.techStack) ? detail.techStack.join(',') : detail.techStack || '',
      highlight: detail.highlight || '',
      repoUrl: detail.repoUrl,
      repoPlatform: detail.repoPlatform || 'GITHUB',
      startDate: detail.startDate,
      endDate: detail.endDate,
      coverUrl: detail.coverUrl,
      visibility: detail.visibility || 'PUBLIC',
      inReviewPool: detail.inReviewPool || 0
    })
    poolEnabled.value = detail.inReviewPool === 1
    assets.value = detail.assets || []
  } catch (e) {
    ElMessage.error('项目加载失败')
  } finally {
    pageLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.project-edit-page {
  min-height: 60vh;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.head-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--xz-text-primary);
}

.page-sub {
  margin-top: 5px;
  font-size: 12.5px;
  color: var(--xz-text-secondary);
}

.head-actions {
  display: flex;
  gap: 10px;
}

.edit-body {
  display: grid;
  grid-template-columns: 1fr 290px;
  gap: 20px;
  align-items: start;
}

.form-block {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 24px 26px;
  margin-bottom: 20px;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--xz-text-primary);
  padding-left: 10px;
  border-left: 3px solid var(--xz-primary);
  line-height: 1.3;
  margin-bottom: 8px;
}

.block-desc {
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--xz-text-secondary);
  margin: 8px 0 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 18px;
}

.full-width {
  width: 100%;
}

.field-hint {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 6px;
  line-height: 1.6;
}

/* ---------- 技术栈标签 ---------- */
.tag-editor {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.tech-chip {
  background: var(--xz-primary-lighter);
  color: var(--xz-primary-dark);
  border-color: transparent;
}

.tech-input {
  width: 120px;
}

.tech-add-btn {
  border-style: dashed;
}

/* ---------- 编辑器 ---------- */
.editor-wrap {
  border: 1px solid var(--xz-border);
  border-radius: var(--xz-radius-sm);
  overflow: hidden;
}

.editor-toolbar {
  border-bottom: 1px solid var(--xz-border);
  background: #fbfcfc;
}

.editor-body {
  height: 380px;
  overflow-y: auto;
}

.editor-body :deep(.w-e-text-container) {
  background: #ffffff;
}

/* ---------- 素材 ---------- */
.asset-tabs {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.asset-tab {
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius-sm);
  padding: 14px 16px;
  background: #fdfefe;
}

.asset-tab-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.asset-tab-title {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--xz-text-primary);
}

.asset-tab-title .el-icon {
  color: var(--xz-primary);
}

.asset-limit {
  font-size: 11.5px;
  font-weight: 400;
  color: var(--xz-text-placeholder);
}

.asset-items {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.asset-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--xz-radius-sm);
  background: #ffffff;
  border: 1px solid var(--xz-border-light);
}

.asset-icon {
  color: var(--xz-primary);
  flex-shrink: 0;
}

.asset-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.asset-name {
  font-size: 13px;
  color: var(--xz-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.asset-meta {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 1px;
}

.asset-ops {
  display: flex;
  flex-shrink: 0;
}

.asset-empty {
  margin-top: 10px;
  font-size: 12.5px;
  color: var(--xz-text-placeholder);
}

/* ---------- 发布设置 ---------- */
.visibility-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.visibility-radio {
  width: 100%;
  height: auto;
  margin-right: 0;
  padding: 11px 14px;
  border: 1px solid var(--xz-border-light);
  border-radius: var(--xz-radius-sm);
  align-items: flex-start;
}

.visibility-radio.is-checked {
  border-color: var(--xz-primary);
  background: #fafcfc;
}

.visibility-radio :deep(.el-radio__label) {
  white-space: normal;
  padding-left: 8px;
}

.vis-body {
  display: flex;
  flex-direction: column;
}

.vis-label {
  font-size: 13.5px;
  font-weight: 500;
  color: var(--xz-text-primary);
}

.vis-desc {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  margin-top: 2px;
}

.pool-switch {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pool-hint {
  font-size: 11.5px;
  color: var(--xz-text-placeholder);
  line-height: 1.6;
}

.repo-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.repo-platform {
  width: 120px;
  flex-shrink: 0;
}

.repo-url {
  flex: 1;
}

.cover-uploader {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.cover-preview {
  width: 260px;
  height: 146px;
  border-radius: var(--xz-radius-sm);
  border: 1px solid var(--xz-border-light);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  color: var(--xz-text-placeholder);
  font-size: 11.5px;
}

.cover-ops {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* ---------- 侧栏 ---------- */
.edit-side {
  display: flex;
  flex-direction: column;
  gap: 18px;
  position: sticky;
  top: 84px;
}

.side-card {
  background: #ffffff;
  border-radius: var(--xz-radius-lg);
  border: 1px solid var(--xz-border-light);
  padding: 20px 22px;
}

.side-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--xz-text-primary);
  padding-left: 9px;
  border-left: 3px solid var(--xz-primary);
  line-height: 1.3;
  margin-bottom: 15px;
}

.tip-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  counter-reset: tip;
}

.tip-list li {
  position: relative;
  padding-left: 24px;
  counter-increment: tip;
}

.tip-list li::before {
  content: counter(tip);
  position: absolute;
  left: 0;
  top: 1px;
  width: 17px;
  height: 17px;
  border-radius: 50%;
  background: var(--xz-primary-lighter);
  color: var(--xz-primary-dark);
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tip-list strong {
  display: block;
  font-size: 13px;
  color: var(--xz-text-primary);
}

.tip-list span {
  display: block;
  font-size: 12px;
  line-height: 1.7;
  color: var(--xz-text-secondary);
  margin-top: 3px;
}

.dimension-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dimension-item {
  display: flex;
  flex-direction: column;
}

.dim-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--xz-text-primary);
}

.dim-hint {
  font-size: 11.5px;
  line-height: 1.6;
  color: var(--xz-text-placeholder);
  margin-top: 2px;
}

.preview-video {
  width: 100%;
  border-radius: var(--xz-radius-sm);
  background: #000;
}

.preview-image {
  width: 100%;
  border-radius: var(--xz-radius-sm);
}

@media (max-width: 1080px) {
  .edit-body {
    grid-template-columns: 1fr;
  }

  .edit-side {
    position: static;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
