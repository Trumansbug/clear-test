<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>试卷总数</span>
            </div>
          </template>
          <div class="card-content">
            <el-statistic :value="statistics.paperCount">
              <template #title>
                <div style="display: inline-flex; align-items: center">
                  已创建的试卷数量
                </div>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>已发布试卷</span>
            </div>
          </template>
          <div class="card-content">
            <el-statistic :value="statistics.publishedCount">
              <template #title>
                <div style="display: inline-flex; align-items: center">
                  已发布的试卷数量
                </div>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户数量</span>
            </div>
          </template>
          <div class="card-content">
            <el-statistic :value="statistics.userCount">
              <template #title>
                <div style="display: inline-flex; align-items: center">
                  注册用户数量
                </div>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近创建的试卷</span>
            </div>
          </template>
          <el-table :data="recentPapers" style="width: 100%">
            <el-table-column prop="title" label="试卷标题" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                  {{ scope.row.status === 1 ? '已发布' : '未发布' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">智力测试问卷系统</el-descriptions-item>
            <el-descriptions-item label="当前版本">1.0.0</el-descriptions-item>
            <el-descriptions-item label="开发框架">Vue 3 + Spring Boot</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getStatistics, getRecentPapers } from '../api/home'

export default {
  name: 'Home',
  data() {
    return {
      statistics: {
        paperCount: 0,
        publishedCount: 0,
        userCount: 0
      },
      recentPapers: []
    }
  },
  methods: {
    async loadData() {
      try {
        const [statsRes, papersRes] = await Promise.all([
          getStatistics(),
          getRecentPapers()
        ])
        this.statistics = statsRes.data
        this.recentPapers = papersRes.data
      } catch (error) {
        console.error('加载首页数据失败:', error)
      }
    }
  },
  mounted() {
    this.loadData()
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}
</style> 