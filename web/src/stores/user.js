import { defineStore } from 'pinia'
import { authApi } from '@/api'

const TOKEN_KEY = 'xz_token'
const USER_KEY = 'xz_user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),

  getters: {
    isLogin: (state) => !!state.token,
    role: (state) => state.user?.role || '',
    isStudent: (state) => state.user?.role === 'STUDENT',
    isExpert: (state) => state.user?.role === 'EXPERT',
    isCompany: (state) => state.user?.role === 'COMPANY',
    isAdmin: (state) => state.user?.role === 'ADMIN',
    displayName: (state) =>
      state.user?.nickname || state.user?.realName || state.user?.username || '未登录',
    avatar: (state) => state.user?.avatar || '',
    /** 学籍认证状态（学生） */
    eduVerified: (state) => state.user?.studentProfile?.eduVerified ?? 0,
    expertVerifyStatus: (state) => state.user?.expertProfile?.verifyStatus ?? 0,
    companyVerifyStatus: (state) => state.user?.companyProfile?.verifyStatus ?? 0
  },

  actions: {
    setSession({ token, user }) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },

    async login(payload) {
      const data = await authApi.login(payload)
      this.setSession(data)
      return data
    },

    async register(payload) {
      const data = await authApi.register(payload)
      this.setSession(data)
      return data
    },

    async fetchMe() {
      const user = await authApi.me()
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user))
      return user
    },

    async logout() {
      try {
        await authApi.logout()
      } catch (e) {
        /* 忽略退出异常 */
      }
      this.clear()
    },

    clear() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
