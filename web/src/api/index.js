import request from './request'

/* ============ 认证 ============ */
export const authApi = {
  register: (data) => request.post('/auth/register', data),
  login: (data) => request.post('/auth/login', data),
  me: () => request.get('/auth/me'),
  changePassword: (data) => request.post('/auth/change-password', data),
  logout: () => request.post('/auth/logout')
}

/* ============ 文件 ============ */
export const fileApi = {
  upload: (file, bizType = 'DOC', onProgress) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('bizType', bizType)
    return request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 600000,
      onUploadProgress: onProgress
    })
  },
  remove: (url) => request.delete('/file', { params: { url } })
}

/* ============ 认证审核 ============ */
export const verifyApi = {
  submitEdu: (data) => request.post('/verify/student', data),
  submitExpert: (data) => request.post('/verify/expert', data),
  submitCompany: (data) => request.post('/verify/company', data),
  myRecords: () => request.get('/verify/my'),
  pageRecords: (params) => request.get('/verify/page', { params }),
  audit: (data) => request.post('/verify/audit', data)
}

/* ============ 档案 ============ */
export const profileApi = {
  updateBase: (data) => request.put('/profile/base', data),
  updateStudent: (data) => request.put('/profile/student', data),
  updateExpert: (data) => request.put('/profile/expert', data),
  updateCompany: (data) => request.put('/profile/company', data)
}

/* ============ 项目 ============ */
export const projectApi = {
  create: (data) => request.post('/project', data),
  update: (data) => request.put('/project', data),
  remove: (id) => request.delete(`/project/${id}`),
  detail: (id, forCompany = false) => request.get(`/project/${id}`, { params: { forCompany } }),
  my: (params) => request.get('/project/my', { params }),
  portfolio: (studentId) => request.get(`/project/portfolio/${studentId}`),
  search: (params) => request.get('/project/search', { params }),
  reviewPool: (params) => request.get('/project/review-pool', { params }),
  publish: (id) => request.post(`/project/${id}/publish`),
  unpublish: (id) => request.post(`/project/${id}/unpublish`),
  togglePool: (id, join = true) => request.post(`/project/${id}/review-pool`, null, { params: { join } }),
  saveAsset: (id, data) => request.post(`/project/${id}/asset`, data),
  deleteAsset: (assetId) => request.delete(`/project/asset/${assetId}`),
  statistics: () => request.get('/project/statistics')
}

/* ============ 点评 ============ */
export const reviewApi = {
  submit: (data) => request.post('/review', data),
  my: (params) => request.get('/review/my', { params }),
  received: (params) => request.get('/review/received', { params }),
  statistics: () => request.get('/review/statistics'),
  thanks: (id) => request.post(`/review/${id}/thanks`),
  appeal: (data) => request.post('/review/appeal', data),
  invite: (data) => request.post('/review/invite', data),
  byProject: (projectId) => request.get(`/review/project/${projectId}`),
  detail: (id) => request.get(`/review/${id}`),
  reply: (data) => request.post('/review/reply', data),
  appealPage: (params) => request.get('/review/appeal/page', { params }),
  handleAppeal: (id, status, remark) =>
    request.post(`/review/appeal/${id}/handle`, null, { params: { status, remark } })
}

/* ============ 企业端 ============ */
export const companyApi = {
  searchCandidates: (data, params) => request.post('/company/candidates/search', data, { params }),
  candidateDetail: (studentId) => request.get(`/company/candidates/${studentId}`),
  favorite: (data) => request.post('/company/favorites', data),
  unfavorite: (studentId) => request.delete(`/company/favorites/${studentId}`),
  favorites: (params) => request.get('/company/favorites', { params }),
  sendInvitation: (data) => request.post('/company/invitations', data),
  sentInvitations: (params) => request.get('/company/invitations', { params }),
  statistics: () => request.get('/company/statistics'),
  viewLogs: (params) => request.get('/company/view-logs', { params })
}

/* ============ 邀约（学生端） ============ */
export const invitationApi = {
  received: (params) => request.get('/invitation/received', { params }),
  reply: (id, accept, contactInfo) =>
    request.post(`/invitation/${id}/reply`, null, { params: { accept, contactInfo } })
}

/* ============ 平台运营 ============ */
export const adminApi = {
  overview: () => request.get('/admin/overview'),
  users: (params) => request.get('/admin/users', { params }),
  toggleUserStatus: (id, enable) =>
    request.post(`/admin/users/${id}/status`, null, { params: { enable } }),
  resetPassword: (id) => request.post(`/admin/users/${id}/reset-password`),
  contentAudits: (params) => request.get('/admin/content-audits', { params }),
  handleAudit: (id, pass, remark) =>
    request.post(`/admin/content-audits/${id}/handle`, null, { params: { pass, remark } }),
  appeals: (params) => request.get('/review/appeal/page', { params }),
  handleAppeal: (id, status, remark) =>
    request.post(`/review/appeal/${id}/handle`, null, { params: { status, remark } }),
  projects: (params) => request.get('/admin/projects', { params }),
  dict: (params) => request.get('/admin/dict', { params }),
  trackEvents: (params) => request.get('/admin/track-events', { params })
}

/* ============ 公共 ============ */
export const commonApi = {
  stats: () => request.get('/common/stats'),
  health: () => request.get('/common/health'),
  studentProfile: (studentId) => request.get(`/common/student/${studentId}`),
  expertHall: (params) => request.get('/common/experts', { params })
}
