import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/surveys' },
      { path: 'surveys', name: 'AdminSurveys', component: () => import('../views/admin/SurveyManage.vue') },
      { path: 'surveys/:id/questions', name: 'QuestionManage', component: () => import('../views/admin/QuestionManage.vue') },
      { path: 'surveys/:id/data', name: 'SurveyData', component: () => import('../views/admin/SurveyData.vue') },
      { path: 'surveys/:id/statistics', name: 'SurveyStatistics', component: () => import('../views/admin/SurveyStatistics.vue') },
      { path: 'users', name: 'UserManage', component: () => import('../views/admin/UserManage.vue') }
    ]
  },
  {
    path: '/',
    component: () => import('../views/user/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/surveys' },
      { path: 'surveys', name: 'SurveyList', component: () => import('../views/user/SurveyList.vue') },
      { path: 'surveys/:id/fill', name: 'FillSurvey', component: () => import('../views/user/FillSurvey.vue') },
      { path: 'my-answers', name: 'MyAnswers', component: () => import('../views/user/MyAnswers.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.role === 'ADMIN' && !userStore.isAdmin) {
    next('/')
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next(userStore.isAdmin ? '/admin' : '/')
  } else {
    next()
  }
})

export default router
