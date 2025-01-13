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
              <div class="stats-value">{{ stats.shareCount }}</div>
              <div class="stats-label">分享总数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="recent-section">
      <h2>最近试卷</h2>
      <el-table :data="recentPapers" style="width: 100%">
        <el-table-column prop="title" label="试卷标题" align="center" />
        <el-table-column prop="remark" label="描述" show-overflow-tooltip align="center" />
        <el-table-column prop="totalScore" label="总分" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" :formatter="formatTime" />
        <el-table-column prop="updateTime" label="更新时间" width="180" align="center" :formatter="formatTime" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handlePreview(scope.row)">
              预览
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 试卷预览对话框 -->
    <el-dialog
        :visible.sync="previewVisible"
        title="试卷预览"
        width="1000px"
    >
      <div class="preview-content">
        <h2>{{ previewPaper.title }}</h2>
        <p class="description" v-html="previewPaper.description"></p>
        <div class="total-score">总分：{{ previewPaper.totalScore || 0 }} 分</div>

        <template v-if="questions.length > 0">
          <div v-for="(question, index) in questions" :key="question.id" class="question-item">
            <div class="question-header">
              第 {{ index + 1 }} 题（{{ question.score }}分）
              <el-tag :type="getQuestionTypeTag(question.type)" size="small" class="question-type">
                {{ questionTypes[question.type] }}
              </el-tag>
            </div>
            <div class="question-content">{{ question.content }}</div>
            <div v-if="question.type !== 3" class="question-options">
              <div v-for="(option, key) in JSON.parse(question.options)" :key="key">
                {{ String.fromCharCode(65 + Number(key)) }}. {{ option.content }}
                <span class="option-score">({{ option.score }}分)</span>
              </div>
            </div>
            <div v-if="question.type === 2" class="question-mode">
              <span class="mode-label">判断模式：</span>
              <span>{{ question.judgeMode === 1 ? '全部对得分' : '部分对得分' }}</span>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无题目" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPaperList, getQuestionsByPaperId } from '@/api/paper'
import { globalCount } from "@/api/home";

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
        shareCount: 0
      },
      recentPapers: [],
      previewVisible: false,
      previewPaper: {},
      questions: [],
      questionTypes: {
        1: '单选题',
        2: '多选题',
        3: '填空题'
      }
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
    async loadData() {
      try {
        const res = await globalCount()
        this.stats = res.data
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    },
    formatTime(row, column, cellValue) {
      return this.$formatTime(cellValue);
    },
    hasRole(role) {
      return this.$store.state.roles.includes(role)
    },
    async handlePreview(row) {
      this.previewPaper = row
      try {
        const res = await getQuestionsByPaperId(row.id)
        this.questions = res.data
      } catch (error) {
        console.error('获取题目失败:', error)
      }
      this.previewVisible = true
    },
    getQuestionTypeTag(type) {
      const types = {
        1: '',
        2: 'success'
      }
      return types[type] || 'info'
    },
  },
  mounted() {
    this.loadData()
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

.preview-content {
  padding: 0;
}

.preview-content h2 {
  text-align: center;
}

.total-score {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
}

.question-header {
  margin-bottom: 10px;
  font-weight: bold;
}

.question-content {
  margin-bottom: 10px;
}

.question-options {
  color: #666;
  padding-left: 20px;
}

.question-item {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.question-type {
  margin-left: 10px;
}

.question-mode {
  margin-top: 10px;
  color: #606266;
}

.option-score {
  margin-left: 8px;
  color: #F56C6C;
}
</style> 