import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/public/Home.vue'), meta: { title: '首页' } },
      {
        path: 'square',
        name: 'Square',
        component: () => import('@/views/public/Square.vue'),
        meta: { title: '项目广场' }
      },
      {
        path: 'project/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/public/ProjectDetail.vue'),
        meta: { title: '项目详情' }
      },
      {
        path: 'portfolio/:studentId',
        name: 'Portfolio',
        component: () => import('@/views/public/Portfolio.vue'),
        meta: { title: '学生作品集' }
      },
      {
        path: 'experts',
        name: 'ExpertHall',
        component: () => import('@/views/public/ExpertHall.vue'),
        meta: { title: '导师与工程师' }
      }
    ]
  },

  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', guestOnly: true }
  },

  /* ---- 学生端 ---- */
  {
    path: '/student',
    component: () => import('@/layouts/ConsoleLayout.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT'] },
    children: [
      {
        path: '',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'projects',
        name: 'StudentProjects',
        component: () => import('@/views/student/ProjectList.vue'),
        meta: { title: '我的项目' }
      },
      {
        path: 'projects/edit/:id?',
        name: 'ProjectEdit',
        component: () => import('@/views/student/ProjectEdit.vue'),
        meta: { title: '编辑项目' }
      },
      {
        path: 'reviews',
        name: 'StudentReviews',
        component: () => import('@/views/student/Reviews.vue'),
        meta: { title: '收到的点评' }
      },
      {
        path: 'invitations',
        name: 'StudentInvitations',
        component: () => import('@/views/student/Invitations.vue'),
        meta: { title: '企业邀约' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '我的档案' }
      }
    ]
  },

  /* ---- 专家端 ---- */
  {
    path: '/expert',
    component: () => import('@/layouts/ConsoleLayout.vue'),
    meta: { requiresAuth: true, roles: ['EXPERT'] },
    children: [
      {
        path: '',
        name: 'ExpertDashboard',
        component: () => import('@/views/expert/Dashboard.vue'),
        meta: { title: '点评工作台' }
      },
      {
        path: 'pool',
        name: 'ExpertPool',
        component: () => import('@/views/expert/ReviewPool.vue'),
        meta: { title: '求点评池' }
      },
      {
        path: 'review/:projectId',
        name: 'ExpertReviewSubmit',
        component: () => import('@/views/expert/ReviewSubmit.vue'),
        meta: { title: '撰写点评' }
      },
      {
        path: 'history',
        name: 'ExpertHistory',
        component: () => import('@/views/expert/History.vue'),
        meta: { title: '点评记录' }
      },
      {
        path: 'profile',
        name: 'ExpertProfile',
        component: () => import('@/views/expert/Profile.vue'),
        meta: { title: '专家档案' }
      }
    ]
  },

  /* ---- 企业端 ---- */
  {
    path: '/company',
    component: () => import('@/layouts/ConsoleLayout.vue'),
    meta: { requiresAuth: true, roles: ['COMPANY'] },
    children: [
      {
        path: '',
        name: 'CompanyDashboard',
        component: () => import('@/views/company/Dashboard.vue'),
        meta: { title: '招聘工作台' }
      },
      {
        path: 'talent',
        name: 'CompanyTalent',
        component: () => import('@/views/company/Talent.vue'),
        meta: { title: '人才检索' }
      },
      {
        path: 'candidate/:studentId',
        name: 'CompanyCandidate',
        component: () => import('@/views/company/CandidateDetail.vue'),
        meta: { title: '候选人档案' }
      },
      {
        path: 'favorites',
        name: 'CompanyFavorites',
        component: () => import('@/views/company/Favorites.vue'),
        meta: { title: '收藏夹' }
      },
      {
        path: 'invitations',
        name: 'CompanyInvitations',
        component: () => import('@/views/company/Invitations.vue'),
        meta: { title: '发出的邀约' }
      },
      {
        path: 'logs',
        name: 'CompanyLogs',
        component: () => import('@/views/company/ViewLogs.vue'),
        meta: { title: '查验记录' }
      },
      {
        path: 'profile',
        name: 'CompanyProfile',
        component: () => import('@/views/company/Profile.vue'),
        meta: { title: '企业档案' }
      }
    ]
  },

  /* ---- 管理端 ---- */
  {
    path: '/admin',
    component: () => import('@/layouts/ConsoleLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '平台总览' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'verifies',
        name: 'AdminVerifies',
        component: () => import('@/views/admin/Verifies.vue'),
        meta: { title: '认证审核' }
      },
      {
        path: 'audits',
        name: 'AdminAudits',
        component: () => import('@/views/admin/Audits.vue'),
        meta: { title: '内容审核' }
      },
      {
        path: 'appeals',
        name: 'AdminAppeals',
        component: () => import('@/views/admin/Appeals.vue'),
        meta: { title: '申诉处理' }
      },
      {
        path: 'projects',
        name: 'AdminProjects',
        component: () => import('@/views/admin/Projects.vue'),
        meta: { title: '项目管理' }
      }
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: (to, from, savedPosition) => savedPosition || { top: 0 }
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} · 校栈` : '校栈'

  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLogin) {
    ElMessage.warning('请先登录')
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  if (to.meta.guestOnly && userStore.isLogin) {
    return next('/')
  }

  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(userStore.role)) {
      ElMessage.error('当前身份无权访问该页面')
      return next('/')
    }
  }

  next()
})

export default router
