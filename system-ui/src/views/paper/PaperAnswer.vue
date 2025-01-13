<template>
  <div class="paper-answer-container">
    <el-card class="paper-info">
      <template #header>
        <div class="card-header">
          <span style="font-size: 25px; font-weight: bold"> {{ paper.title }} </span>
          <el-button type="primary" @click="handleSubmit">完成</el-button>
        </div>
      </template>

      <div class="paper-description" v-html="paper.description"></div>

      <div v-if="!paper.questionList.length">
        <el-empty description="加载中..." />
      </div>

      <div v-else class="questions-container">
        <div v-for="(question, index) in paper.questionList" :key="question.id" class="question-item">
          <div class="question-header">
            <span class="question-index">第 {{ index + 1 }} 题</span>
            <el-tag :type="getQuestionTypeTag(question.type)" class="question-type">
              {{ getQuestionTypeName(question.type) }}
            </el-tag>
          </div>
          <div class="question-content">{{ question.content }}</div>
          <div class="question-options">
            <el-radio-group v-if="question.type === 1" v-model="userAnswers[question.id]">
              <el-radio 
                v-for="(option, key) in JSON.parse(question.options)" 
                :key="key" 
                :label="String.fromCharCode(65 + Number(key))"
              >
                {{ String.fromCharCode(65 + Number(key)) }}. {{ option.content }}
              </el-radio>
            </el-radio-group>
            <el-checkbox-group v-if="question.type === 2" v-model="userAnswers[question.id]">
              <el-checkbox 
                v-for="(option, key) in JSON.parse(question.options)" 
                :key="key" 
                :label="String.fromCharCode(65 + Number(key))"
              >
                {{ String.fromCharCode(65 + Number(key)) }}. {{ option.content }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
      </div>

      <el-dialog
        title="测试结果"
        :visible.sync="dialogVisible"
        width="50%"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="true">
        <div class="score-result">
          <h2 class="total-score">总分：{{ score }} 分</h2>
          <div class="answer-details" v-html="paper.analysis"></div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="handleClose">确定</el-button>
        </span>
      </el-dialog>

    </el-card>
  </div>
</template>

<script>
import { getPaperByShareCode } from '@/api/paper'
import { Message } from 'element-ui'

export default {
  name: 'PaperAnswer',
  data() {
    return {
      paperId: null,
      paper: {
        questionList: [],
        title: null,
        description: null,
        analysis: null
      },
      userAnswers: {},
      score: null,
      dialogVisible: false
    }
  },
  computed: {
    shareCode() {
      return this.$route.params.shareCode
    }
  },
  methods: {
    async fetchQuestions() {
      if (!this.shareCode) return
      try {
        const res = await getPaperByShareCode(this.shareCode)
        if (res.data) {
          this.paper.questionList = res.data.questions || []
          this.paper.description = res.data.description || ''
          this.paper.title = res.data.title || ''
          this.paper.analysis = res.data.analysis || ''

          this.paper.questionList.forEach(question => {
            if (question.type === 2) {
              this.$set(this.userAnswers, question.id, [])
            } else {
              this.$set(this.userAnswers, question.id, '')
            }
          })
        }
      } catch (error) {
        Message.error('获取题目列表失败：' + error?.message)
        console.error('获取题目列表失败:', error)
      }
    },
    handleSubmit() {
      // 检查是否所有题目都已回答
      const unansweredQuestions = this.paper.questionList.filter(question => {
        const answer = this.userAnswers[question.id]
        return !answer || (Array.isArray(answer) && answer.length === 0)
      })

      if (unansweredQuestions.length > 0) {
        // 获取未答题的题号（从1开始）
        const unansweredNumbers = unansweredQuestions.map(question => {
          return this.paper.questionList.findIndex(q => q.id === question.id) + 1
        })
        Message({
          message: `第 ${unansweredNumbers.join('、')} 题未回答，请检查后再提交`,
          type: 'warning',
          duration: 3000
        })
        return
      }

      let totalScore = 0
      this.paper.questionList.forEach(question => {
        totalScore += this.checkAnswer(question) || 0
      })
      this.score = totalScore
      this.dialogVisible = true
    },
    checkAnswer(question) {
      if (!this.userAnswers[question.id]) {
        return 0
      }

      const options = JSON.parse(question.options)

      if (question.type === 1) {
        const selectedAnswer = this.userAnswers[question.id]
        const selectedOption = options[selectedAnswer.charCodeAt(0) - 65]
        return selectedOption ? selectedOption.score : 0
      } else if (question.type === 2) {
        const selectedAnswers = this.userAnswers[question.id]

        if (!selectedAnswers.length) {
          return 0
        }

        const selectedScores = selectedAnswers.map(answer => {
          const option = options[answer.charCodeAt(0) - 65]
          return option ? option.score : 0
        })

        if (selectedScores.includes(0)) {
          return 0
        }

        if (question.judgeMode === 1) {
          const correctAnswers = options
              .map((option, index) => option.score > 0 ? String.fromCharCode(65 + index) : null)
              .filter(answer => answer !== null)

          const hasAllCorrect = correctAnswers.every(answer => selectedAnswers.includes(answer))
          if (!hasAllCorrect) {
            return 0
          }

          return selectedScores.reduce((sum, score) => sum + score, 0)
        }

        return selectedScores.reduce((sum, score) => sum + score, 0)
      }

      return 0
    },
    getQuestionTypeName(type) {
      const types = {
        1: '单选题',
        2: '多选题'
      }
      return types[type] || '未知'
    },
    getQuestionTypeTag(type) {
      const types = {
        1: '',
        2: 'success'
      }
      return types[type] || 'info'
    },
    handleClose() {
      this.dialogVisible = false
    }
  },
  mounted() {
    this.fetchQuestions()
  }
}
</script>

<style scoped>
.paper-answer-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.questions-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 10px;
}

.question-index {
  font-weight: bold;
}

.question-type {
  margin-right: auto;
}

.question-content {
  margin-bottom: 15px;
  line-height: 1.5;
}

.question-options {
  margin-bottom: 15px;
}

.score-result {
  padding: 20px;
}

.total-score {
  text-align: center;
  color: #409EFF;
  margin-bottom: 30px;
}

.answer-details {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-item {
  border-bottom: 1px solid #EBEEF5;
  padding: 15px 0;
}

.detail-item:last-child {
  border-bottom: none;
}

.question-text {
  color: #606266;
  margin: 10px 0;
}

.correct {
  color: #67C23A;
}

.score {
  font-weight: bold;
  color: #F56C6C;
}

.detail-item h4 {
  margin: 0;
  color: #303133;
}

.option-score {
  color: #F56C6C;
  margin-left: 8px;
  font-size: 13px;
}
</style> 