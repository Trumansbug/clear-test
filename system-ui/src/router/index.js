import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '../layout/index.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/home/index.vue'),
        meta: { title: '首页' }
      }
    ]
  },
  {
    path: '/paper',
    component: Layout,
    redirect: '/paper/list',
    meta: { title: '试卷管理' },
    children: [
      {
        path: 'list',
        name: 'PaperList',
        component: () => import('../views/paper/Papers.vue'),
        meta: { title: '试卷列表' }
      },
      {
        path: 'add',
        name: 'PaperAdd',
        component: () => import('../views/paper/PaperAdd.vue'),
        meta: { title: '新增试卷' }
      },
      {
        path: 'edit/:id',
        name: 'PaperEdit',
        component: () => import('../views/paper/PaperEdit.vue'),
        meta: { title: '编辑试卷' }
      }
    ]
  },
  {
    path: '/share',
    component: Layout,
    redirect: '/share/list',
    meta: { title: '分享管理' },
    children: [
      {
        path: 'list',
        name: 'ShareList',
        component: () => import('../views/share/Shares.vue'),
        meta: { title: '分享列表' }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    meta: { title: '用户管理', roles: ['ROLE_ADMIN'] },
    children: [
      {
        path: 'list',
        name: 'UserList',
        component: () => import('../views/user/Users.vue'),
        meta: { title: '用户列表' }
      }
    ]
  },
  {
    path: '/role',
    component: Layout,
    redirect: '/role/list',
    meta: { title: '角色管理', roles: ['ROLE_ADMIN'] },
    children: [
      {
        path: 'list',
        name: 'RoleList',
        component: () => import('../views/role/Roles.vue'),
        meta: { title: '角色列表' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const roles = JSON.parse(localStorage.getItem('roles') || '[]')

  if (to.path === '/login') {
    if (token) {
      next('/')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (to.meta.roles && !roles.some(role => to.meta.roles.includes(role))) {
    next('/403')
    return
  }

  next()
})

export default router 