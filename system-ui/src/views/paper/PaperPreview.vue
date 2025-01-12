<template>
  <div class="paper-preview-container">
    <div class="header">
      <h2>试卷预览</h2>
      <div class="actions">
        <el-button @click="handleBack">返回</el-button>
      </div>
    </div>

    <div class="paper-info">
      <h1 class="paper-title">{{ paper.title }}</h1>
      <p class="paper-description">{{ paper.description }}</p>
      <div class="paper-meta">
        <span>总分：{{ totalScore }} 分</span>
        <span>题目数量：{{ questions.length }} 题</span>
      </div>
    </div>

    <div class="questions-section">
      <template v-if="questions.length > 0">
        <div
          v-for="(question, index) in questions"
          :key="question.id"
          class="question-item"
        >
          <div class="question-header">
            <span class="question-index">第 {{ index + 1 }} 题</span>
            <div class="question-meta">
              <el-tag size="small">{{ questionTypes[question.type] }}</el-tag>
              <span class="question-score">{{ question.score }} 分</span>
            </div>
          </div>

          <div class="question-content">
            <div class="question-text">{{ question.content }}</div>
            <div v-if="question.type !== 3" class="question-options">
              <template v-if="question.type === 1">
                <el-radio-group v-model="answers[question.id]">
                  <div
                    v-for="(option, key) in JSON.parse(question.options)"
                    :key="key"
                    class="option-item"
                  >
                    <el-radio :label="key">{{ key }}. {{ option }}</el-radio>
                  </div>
                </el-radio-group>
              </template>
              <template v-else-if="question.type === 2">
                <el-checkbox-group v-model="answers[question.id]">
                  <div
                    v-for="(option, key) in JSON.parse(question.options)"
                    :key="key"
                    class="option-item"
                  >
                    <el-checkbox :label="key">{{ key }}. {{ option }}</el-checkbox>
                  </div>
                </el-checkbox-group>
              </template>
            </div>
            <div v-else class="fill-blank">
              <el-input
                v-model="answers[question.id]"
                placeholder="请输入答案"
              />
            </div>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无题目" />
    </div>

    <div v-if="questions.length > 0" class="submit-section">
      <el-button type="primary" @click="handleSubmit">提交答案</el-button>
    </div>

    <!-- 答题结果对话框 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="答题结果"
      width="600px"
    >
      <div class="r-content">
        <div class="r-header">
          <div class="total-score">
            总分：<span class="score">{{ totalScore }}</span> 分
          </div>
          <div class="correct-count">
            正确题数：{{ correctCount }} / {{ questions.length }}
          </div>
        </div>

        <div class="r-list">
          <div
            v-for="(question, index) in questions"
            :key="question.id"
            class="r-item"
          >
            <div class="r-item-header">
              <span>第 {{ index + 1 }} 题</span>
              <span :class="['status', isCorrect(question) ? 'correct' : 'wrong']">
                {{ isCorrect(question) ? '正确' : '错误' }}
              </span>
            </div>
            <div class="r-item-content">
              <div>你的答案：{{ formatAnswer(answers[question.id]) }}</div>
              <div>正确答案：{{ question.correctAnswer }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleBack">返回列表</el-button>
        <el-button type="primary" @click="handleRetry">重新作答</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { getPaper, getQuestionsByPaperId } from '../../api/paper'

export default {
  name: 'PaperPreview',
  data() {
    return {
      paper: {},
      questions: [],
      answers: {},
      resultDialogVisible: false,
      questionTypes: {
        1: '单选题',
        2: '多选题',
        3: '填空题'
      }
    }
  },
  computed: {
    totalScore() {
      return this.questions.reduce((total, question) => {
        return total + (this.isCorrect(question) ? question.score : 0)
      }, 0)
    },
    correctCount() {
      return this.questions.filter(question => this.isCorrect(question)).length
    }
  },
  methods: {
    async initData() {
      try {
        const [paperRes, questionsRes] = await Promise.all([
          getPaper(this.$route.params.id),
          getQuestionsByPaperId(this.$route.params.id)
        ])
        this.paper = paperRes.data
        this.questions = questionsRes.data
        // 初始化答案
        this.questions.forEach(question => {
          if (question.type === 2) {
            this.$set(this.answers, question.id, [])
          } else {
            this.$set(this.answers, question.id, '')
          }
        })
      } catch (error) {
        console.error('获取试卷数据失败:', error)
      }
    },
    handleBack() {
      this.$router.push('/paper/list')
    },
    handleSubmit() {
      // 检查是否所有题目都已作答
      const unanswered = this.questions.find(question => {
        const answer = this.answers[question.id]
        return !answer || (Array.isArray(answer) && answer.length === 0)
      })
      if (unanswered) {
        ElMessage.warning('请完成所有题目后再提交')
        return
      }
      this.resultDialogVisible = true
    },
    handleRetry() {
      this.questions.forEach(question => {
        if (question.type === 2) {
          this.$set(this.answers, question.id, [])
        } else {
          this.$set(this.answers, question.id, '')
        }
      })
      this.resultDialogVisible = false
    },
    isCorrect(question) {
      const userAnswer = this.answers[question.id]
      if (question.type === 2) {
        return (
          userAnswer.length === question.correctAnswer.length &&
          userAnswer.every(a => question.correctAnswer.includes(a))
        )
      }
      return userAnswer === question.correctAnswer
    },
    formatAnswer(answer) {
      if (Array.isArray(answer)) {
        return answer.join(', ')
      }
      return answer
    }
  },
  mounted() {
    this.initData()
  }
}
</script>

<style scoped>
.paper-preview-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.paper-info {
  text-align: center;
  margin-bottom: 30px;
}

.paper-title {
  font-size: 24px;
  margin-bottom: 10px;
}

.paper-description {
  color: #666;
  margin-bottom: 15px;
}

.paper-meta {
  color: #666;
}

.paper-meta span {
  margin: 0 10px;
}

.questions-section {
  margin-bottom: 30px;
}

.question-item {
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.question-index {
  font-weight: bold;
}

.question-meta {
  display: flex;
  align-items: center;
}

.question-score {
  margin-left: 10px;
  color: #666;
}

.question-text {
  margin-bottom: 15px;
}

.question-options {
  padding-left: 20px;
}

.option-item {
  margin-bottom: 10px;
}

.fill-blank {
  padding: 0 20px;
}

.submit-section {
  text-align: center;
  margin-top: 30px;
}

.r-content {
  padding: 20px;
}

.r-header {
  text-align: center;
  margin-bottom: 30px;
}

.total-score {
  font-size: 20px;
  margin-bottom: 10px;
}

.score {
  color: #f56c6c;
  font-weight: bold;
}

.r-list {
  max-height: 400px;
  overflow-y: auto;
}

.r-item {
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.r-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.status {
  font-weight: bold;
}

.status.correct {
  color: #67c23a;
}

.status.wrong {
  color: #f56c6c;
}

.r-item-content {
  color: #666;
  padding-left: 20px;
}
</style> 