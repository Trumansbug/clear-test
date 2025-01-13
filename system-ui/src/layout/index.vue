<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container">
      <div class="logo">
        <!-- <img src="../assets/logo.png" alt="Logo"> -->
        <span>乘风测试</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :router="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/home">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>
        <el-submenu index="1">
          <template slot="title">
            <i class="el-icon-notebook-2"></i>
            <span>测试管理</span>
          </template>
          <el-menu-item-group>
            <el-menu-item index="/paper/list">
              <i class="el-icon-document"></i>
              <span slot="title">试卷管理</span>
            </el-menu-item>
            <el-menu-item index="/share/list">
              <i class="el-icon-share"></i>
              <span slot="title">分享管理</span>
            </el-menu-item>
          </el-menu-item-group>
        </el-submenu>
        <el-submenu index="2" v-if="hasRole('ROLE_ADMIN')">
          <template slot="title">
            <i class="el-icon-setting"></i>
            <span>权限管理</span>
          </template>
          <el-menu-item-group>
            <el-menu-item index="/user/list">
              <i class="el-icon-user"></i>
              <span slot="title">用户管理</span>
            </el-menu-item>
            <el-menu-item index="/role/list">
              <i class="el-icon-s-custom"></i>
              <span slot="title">角色管理</span>
            </el-menu-item>
          </el-menu-item-group>
        </el-submenu>
        <el-submenu index="3">
          <template slot="title">
            <i class="el-icon-setting"></i>
            <span>系统管理</span>
          </template>
          <el-menu-item-group>
            <el-menu-item index="/system/log">
              <i class="el-icon-user"></i>
              <span slot="title">日志管理</span>
            </el-menu-item>
            <el-menu-item index="/system/config">
              <i class="el-icon-s-custom"></i>
              <span slot="title">配置管理</span>
            </el-menu-item>
          </el-menu-item-group>
        </el-submenu>
      </el-menu>
    </div>

    <!-- 主要内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="right-menu">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">
              {{ username }}<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  computed: {
    ...mapGetters(['hasRole']),
    username() {
      return this.$store.state.user ? this.$store.state.user.username : ''
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.$store.dispatch('clearUser')
        this.$router.push('/login')
        this.$message.success('退出登录成功')
      }
    }
  }
}
</script>

<style scoped>

.sidebar-container {
  width: 210px;
  height: 100%;
  background-color: #304156;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  overflow-y: auto;
}

.logo {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 15px;
  color: #fff;
}

.logo img {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.main-container {
  min-height: 100%;
  margin-left: 210px;
  position: relative;
  background-color: #f0f2f5;
}

.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 20px;
  position: relative;
}

.right-menu {
  margin-left: auto;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}

.app-main {
  min-height: calc(100vh - 50px);
  padding: 20px;
  box-sizing: border-box;
}
</style> 