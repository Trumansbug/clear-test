<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1>欢迎使用</h1>
      <p class="description">
        等到春暖艳阳天
      </p>
    </div>

    <div class="stats-section" v-if="hasRole('ROLE_ADMIN')">
      <h2>系统统计</h2>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stats-card">
            <div class="stats-icon user">
              <i class="el-icon-user"></i>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.userCount }}</div>
              <div class="stats-label">用户总数</div>
            </div>
          </div>
        </el-col>

        <el-col :span="6">
          <div class="stats-card">
            <div class="stats-icon paper">
              <i class="el-icon-document"></i>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.paperCount }}</div>
              <div class="stats-label">试卷总数</div>
            </div>
          </div>
        </el-col>

        <el-col :span="6">
          <div class="stats-card">
            <div class="stats-icon question">
              <i class="el-icon-question"></i>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.questionCount }}</div>
              <div class="stats-label">题目总数</div>
            </div>
          </div>
        </el-col>

        <el-col :span="6">
          <div class="stats-card">
            <div class="stats-icon submission">
              <i class="el-icon-finished"></i>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.submissionCount }}</div>
              <div class="stats-label">答卷总数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="recent-section">
      <h2>最近试卷</h2>
      <el-table :data="recentPapers" style="width: 100%">
        <el-table-column prop="title" label="试卷标题" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="总分" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handlePreview(scope.row)">
              预览
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { getPaperList } from '../../api/paper'

export default {
  name: 'Home',
  components: {
    
  },
  data() {
    return {
      stats: {
        userCount: 0,
        paperCount: 0,
        questionCount: 0,
        submissionCount: 0
      },
      recentPapers: []
    }
  },
  methods: {
    async getRecentPapers() {
      try {
        const res = await getPaperList({
          current: 1,
          size: 5
        })
        this.recentPapers = res.data.records
      } catch (error) {
        console.error('获取最近试卷失败:', error)
      }
    },
    hasRole(role) {
      return this.$store.state.roles.includes(role)
    },
    handleToPaper() {
      this.$router.push('/paper/list')
    },
    handleToUser() {
      this.$router.push('/user/list')
    },
    handleToRole() {
      this.$router.push('/role/list')
    },
    handlePreview(paper) {
      this.$router.push(`/paper/preview/${paper.id}`)
    }
  },
  mounted() {
    // 模拟统计数据
    this.stats = {
      userCount: 100,
      paperCount: 50,
      questionCount: 200,
      submissionCount: 1000
    }
    this.getRecentPapers()
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
}

.welcome-section h1 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 10px;
}

.description {
  font-size: 16px;
  color: #606266;
}

.feature-card {
  margin-bottom: 20px;
  height: 200px;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-header .el-icon {
  margin-right: 10px;
  font-size: 20px;
}

.card-content {
  text-align: center;
}

.card-content p {
  margin: 20px 0;
  color: #606266;
}

.stats-section {
  margin: 40px 0;
}

.stats-section h2 {
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
}

.stats-card {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
}

.stats-icon .el-icon {
  font-size: 24px;
  color: #fff;
}

.stats-icon.user {
  background: #409eff;
}

.stats-icon.paper {
  background: #67c23a;
}

.stats-icon.question {
  background: #e6a23c;
}

.stats-icon.submission {
  background: #f56c6c;
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.recent-section {
  margin-top: 40px;
}

.recent-section h2 {
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
}
</style> 