<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item
      v-for="(item, index) in breadcrumbs"
      :key="item.path"
      :to="index === breadcrumbs.length - 1 ? null : { path: item.path }"
    >
      {{ item.meta.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script>
export default {
  name: 'Breadcrumb',
  data() {
    return {
      breadcrumbs: []
    }
  },
  methods: {
    getBreadcrumbs() {
      const matched = this.$route.matched.filter(item => item.meta && item.meta.title)
      this.breadcrumbs = matched
    }
  },
  watch: {
    '$route.path': {
      handler() {
        this.getBreadcrumbs()
      },
      immediate: true
    }
  }
}
</script>

<style scoped>
.el-breadcrumb {
  line-height: 50px;
  font-size: 14px;
}
</style> 