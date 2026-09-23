/**
 * 全局枚举与展示映射
 */

export const ROLE_TEXT = {
  STUDENT: '学生',
  EXPERT: '专家',
  COMPANY: '企业',
  ADMIN: '运营'
}

export const ROLE_HOME = {
  STUDENT: '/student',
  EXPERT: '/expert',
  COMPANY: '/company',
  ADMIN: '/admin'
}

export const PROJECT_TYPES = [
  { value: 'COURSE', label: '课程设计' },
  { value: 'COMPETITION', label: '竞赛项目' },
  { value: 'INTERNSHIP', label: '实习作品' },
  { value: 'PERSONAL', label: '个人项目' },
  { value: 'GRADUATION', label: '毕业设计' },
  { value: 'RESEARCH', label: '科研项目' }
]

export const CATEGORIES = [
  { value: 'CS', label: '计算机 / 软件' },
  { value: 'DESIGN', label: '设计 / 视觉' },
  { value: 'ECON', label: '经管 / 商科' },
  { value: 'OTHER', label: '其他方向' }
]

export const CATEGORY_TEXT = CATEGORIES.reduce((acc, c) => {
  acc[c.value] = c.label
  return acc
}, {})

export const PROJECT_TYPE_TEXT = PROJECT_TYPES.reduce((acc, t) => {
  acc[t.value] = t.label
  return acc
}, {})

export const VISIBILITY_OPTIONS = [
  { value: 'PUBLIC', label: '公开可见', desc: '任何人可在项目广场看到' },
  { value: 'LINK', label: '仅链接可见', desc: '不进入广场，知道链接即可访问' },
  { value: 'PRIVATE', label: '仅自己可见', desc: '仅本人与平台运营可见' }
]

export const VISIBILITY_TEXT = {
  PUBLIC: '公开可见',
  LINK: '仅链接可见',
  PRIVATE: '仅自己可见'
}

export const VERIFY_STATUS = {
  0: { text: '未认证', type: 'info' },
  1: { text: '待审核', type: 'warning' },
  2: { text: '已认证', type: 'success' },
  3: { text: '已驳回', type: 'danger' }
}

export const EXPERT_TYPE_TEXT = {
  TEACHER: '高校导师',
  ENGINEER: '企业工程师'
}

export const EXPERT_LEVEL_TEXT = {
  BRONZE: '青铜点评人',
  SILVER: '白银点评人',
  GOLD: '金牌点评人'
}

export const EXPERT_LEVEL_TYPE = {
  BRONZE: 'info',
  SILVER: 'warning',
  GOLD: 'success'
}

export const PACKAGE_TEXT = {
  FREE: '免费版',
  BASIC: '基础版',
  PRO: '专业版'
}

export const INVITATION_STATUS = {
  1: { text: '待回应', type: 'warning' },
  2: { text: '已同意', type: 'success' },
  3: { text: '已拒绝', type: 'info' }
}

export const APPEAL_STATUS = {
  0: { text: '无申诉', type: 'info' },
  1: { text: '申诉中', type: 'warning' },
  2: { text: '申诉成立', type: 'success' },
  3: { text: '申诉驳回', type: 'info' }
}

export const REVIEW_PUBLISH_STATUS = {
  0: { text: '草稿', type: 'info' },
  1: { text: '已发布', type: 'success' }
}

export const REVIEW_AUDIT_STATUS = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已驳回', type: 'danger' }
}

export const ASSET_TYPE_TEXT = {
  SOURCE: '源码包',
  DOC: '文档',
  VIDEO: '演示视频',
  IMAGE: '图片'
}

export const ASSET_TYPE_ICON = {
  SOURCE: 'FolderOpened',
  DOC: 'Document',
  VIDEO: 'VideoCamera',
  IMAGE: 'Picture'
}

export const ASSET_TYPE_ACCEPT = {
  SOURCE: '.zip,.rar,.7z,.tar,.gz',
  DOC: '.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.md,.txt,.zip,.rar,.7z',
  VIDEO: '.mp4,.webm,.mov,.m4v,.avi,.mkv',
  IMAGE: '.jpg,.jpeg,.png,.gif,.webp,.bmp,.svg'
}

/** 评分维度文案 */
export const SCORE_DIMENSIONS = [
  { key: 'scoreCompletion', label: '完成度', hint: '功能是否完整落地、需求覆盖是否充分' },
  { key: 'scoreNormative', label: '规范性', hint: '代码结构、文档齐全度、命名与提交规范' },
  { key: 'scoreInnovation', label: '创新性', hint: '方案是否有独到之处、是否解决真实痛点' },
  { key: 'scoreTechnical', label: '专业质量', hint: '技术选型合理性、工程实现深度、表现力' }
]

/** 将评分转成文字 */
export function scoreText(score) {
  const n = Number(score)
  if (!n) return '暂无评分'
  if (n >= 4.5) return '优秀'
  if (n >= 3.5) return '良好'
  if (n >= 2.5) return '合格'
  return '待提升'
}

/** 格式化日期 */
export function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 格式化日期时间 */
export function formatDateTime(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(
    d.getMinutes()
  )}`
}

/** 相对时间 */
export function fromNow(value) {
  if (!value) return '—'
  const d = new Date(value).getTime()
  const diff = Date.now() - d
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 30 * day) return `${Math.floor(diff / day)} 天前`
  return formatDate(value)
}

/** 可读文件体积 */
export function readableSize(bytes) {
  if (!bytes && bytes !== 0) return '—'
  const units = ['B', 'KB', 'MB', 'GB']
  let value = Number(bytes)
  let i = 0
  while (value >= 1024 && i < units.length - 1) {
    value /= 1024
    i += 1
  }
  return `${value.toFixed(i === 0 ? 0 : 1)} ${units[i]}`
}
