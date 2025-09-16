import { createRouter, createWebHistory } from 'vue-router'
import login from '@/views/login.vue'
import register from '@/views/register.vue'
import home from '@/views/home.vue'
import LoginByEmail from '@/views/LoginByEmail.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: login,
      meta: { requiresGuest: true } // 只有未登录用户可访问
    },
    {
      path: "/register",
      name: "register",
      component: register,
      meta: { requiresGuest: true } // 只有未登录用户可访问
    },
    {
      path: "/home",
      name: "home",
      component: home,
      meta: { requiresAuth: true } // 需要登录
    },
    {
      path: "/LoginByEmail",
      name: "LoginByEmail",
      component: LoginByEmail,
      meta: { requiresGuest: true } // 只有未登录用户可访问
    },
    // 添加一个默认重定向
    {
      path: "/",
      redirect: () => {
        return localStorage.getItem("id") && localStorage.getItem("uuid") 
          ? { name: 'home' } 
          : { name: 'login' }
      }
    }
  ],
})

// 添加全局前置守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem("id") && localStorage.getItem("uuid")
  
  // 如果路由需要登录但用户未登录
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next({ name: 'login' })
  }
  
  // 如果路由需要未登录状态但用户已登录
  if (to.meta.requiresGuest && isAuthenticated && (to.name !== 'home') && (to.name !== 'LoginByEmail')
      && (to.name !== 'register') && (to.name !== 'login')) {
    return next({ name: 'home' })
  }
  
  // 其他情况正常放行
  next()
})

export default router