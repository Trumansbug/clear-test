<template>
  <div class="paper-add-container">
    <!-- 试卷基本信息 -->
    <el-card class="paper-info">
      <template #header>
        <div class="card-header">
          <span>试卷信息</span>
          <div>
            <el-button type="primary" @click="handleSave">保存</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-form ref="paperForm" :model="paperForm" :rules="rules" label-width="100px">
        <el-form-item label="试卷标题" prop="title">
          <el-input v-model="paperForm.title" placeholder="请输入试卷标题" />
        </el-form-item>
        <el-form-item label="试卷描述" prop="description">
          <el-input
            v-model="paperForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入试卷描述"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 题目列表 -->
    <el-card class="question-list" v-if="paperId">
      <template #header>
        <div class="card-header">
          <span>题目列表</span>
          <el-button type="primary" @click="handleAddQuestion">添加题目</el-button>
        </div>
      </template>

      <el-empty v-if="!questionList.length" description="暂无题目，请点击上方按钮添加题目" />
      
      <div v-else class="questions-container">
        <div v-for="(question, index) in questionList" :key="question.id" class="question-item">
          <div class="question-header">
            <span class="question-index">第 {{ index + 1 }} 题</span>
            <el-tag :type="getQuestionTypeTag(question.type)" class="question-type">
              {{ getQuestionTypeName(question.type) }}
            </el-tag>
            <span class="question-score">{{ question.score }} 分</span>
          </div>
          <div class="question-content">{{ question.content }}</div>
          <div v-if="question.type !== 3" class="question-options">
            <div v-for="(option, key) in JSON.parse(question.options)" :key="key" class="option-item">
              {{ String.fromCharCode(65 + Number(key)) }}. {{ option }}
            </div>
          </div>
          <div class="question-answer">
            <span class="answer-label">正确答案：</span>
            <span v-if="question.type === 2">{{ question.answer.split(',').join('、') }}</span>
            <span v-else>{{ question.answer }}</span>
          </div>
          <div class="question-actions">
            <el-button type="primary" link @click="handleEditQuestion(question)">编辑</el-button>
            <el-button type="danger" link @click="handleDeleteQuestion(question)">删除</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 题目编辑对话框 -->
    <el-dialog
      :visible.sync="questionDialogVisible"
      :title="questionDialogType === 'add' ? '添加题目' : '编辑题目'"
      width="600px"
    >
      <el-form ref="questionForm" :model="questionForm" :rules="questionRules" label-width="100px">
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="questionForm.type" placeholder="请选择题目类型">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="填空题" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入题目内容"
          />
        </el-form-item>
        <el-form-item label="选项" prop="options" v-if="questionForm.type !== 3">
          <div v-for="(option, index) in questionOptions" :key="index" class="option-item">
            <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
            <el-input v-model="questionOptions[index]" placeholder="请输入选项内容" />
            <el-button type="danger" link @click="removeOption(index)" v-if="questionOptions.length > 2">
              删除
            </el-button>
          </div>
          <el-button type="primary" link @click="addOption" v-if="questionOptions.length < 6">
            添加选项
          </el-button>
        </el-form-item>
        <el-form-item label="正确答案" prop="answer">
          <template v-if="questionForm.type === 1">
            <el-radio-group v-model="questionForm.answer">
              <el-radio
                v-for="(option, index) in questionOptions"
                :key="index"
                :label="String.fromCharCode(65 + index)"
              >
                {{ String.fromCharCode(65 + index) }}
              </el-radio>
            </el-radio-group>
          </template>
          <template v-else-if="questionForm.type === 2">
            <el-checkbox-group v-model="multiAnswer">
              <el-checkbox
                v-for="(option, index) in questionOptions"
                :key="index"
                :label="String.fromCharCode(65 + index)"
              >
                {{ String.fromCharCode(65 + index) }}
              </el-checkbox>
            </el-checkbox-group>
          </template>
          <template v-else>
            <el-input v-model="questionForm.answer" placeholder="请输入正确答案" />
          </template>
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="questionForm.score" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleQuestionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { createPaper, getQuestionsByPaperId, addQuestionToPaper, updatePaperQuestion, deleteQuestionFromPaper } from '../../api/paper'

export default {
  name: 'PaperAdd',
  data() {
    return {
      paperId: null,
      loading: false,
      paperForm: {
        title: '',
        description: ''
      },
      rules: {
        title: [
          { required: true, message: '请输入试卷标题', trigger: 'blur' },
          { min: 2, message: '标题长度至少为2个字符', trigger: 'blur' },
          { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
        ]
      },
      questionList: [],
      questionDialogVisible: false,
      questionDialogType: 'add',
      questionForm: {},
      questionOptions: ['', ''],
      multiAnswer: [],
      questionRules: {
        type: [
          { required: true, message: '请选择题目类型', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入题目内容', trigger: 'blur' }
        ],
        answer: [
          { required: true, message: '请设置正确答案', trigger: 'blur' }
        ],
        score: [
          { required: true, message: '请设置分值', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    async getQuestionList() {
      if (!this.paperId) return
      try {
        const res = await getQuestionsByPaperId(this.paperId)
        this.questionList = res.data
      } catch (error) {
        console.error('获取题目列表失败:', error)
      }
    },
    handleSave() {
      this.$refs.paperForm.validate(async valid => {
        if (valid) {
          try {
            if (!this.paperId) {
              const res = await createPaper(this.paperForm)
              this.paperId = res.data.id
              this.$message.success('创建成功')
            }
          } catch (error) {
            this.$message.error('保存试卷失败：' + error?.message)
          }
        }
      })
    },
    handleBack() {
      this.$router.push('/paper/list')
    },
    handleAddQuestion() {
      if (!this.paperId) {
        this.$message.warning('请先保存试卷基本信息')
        return
      }
      this.questionDialogType = 'add'
      this.questionForm = {
        type: 1,
        score: 5
      }
      this.questionOptions = ['', '']
      this.multiAnswer = []
      this.questionDialogVisible = true
    },
    handleEditQuestion(row) {
      this.questionDialogType = 'edit'
      this.questionForm = { ...row }
      if (row.type !== 3) {
        this.questionOptions = JSON.parse(row.options)
        if (row.type === 2) {
          this.multiAnswer = row.answer.split(',')
        }
      }
      this.questionDialogVisible = true
    },
    handleDeleteQuestion(row) {
      this.$confirm('确认删除该题目吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteQuestionFromPaper(this.paperId, row.id)
          this.$message.success('删除成功')
          await this.getQuestionList()
        } catch (error) {
          console.error('删除题目失败:', error)
        }
      })
    },
    addOption() {
      this.questionOptions.push('')
    },
    removeOption(index) {
      this.questionOptions.splice(index, 1)
    },
    async handleQuestionSubmit() {
      this.$refs.questionForm.validate(async valid => {
        if (valid) {
          try {
            const questionData = { ...this.questionForm }
            if (questionData.type !== 3) {
              questionData.options = JSON.stringify(this.questionOptions)
              if (questionData.type === 2) {
                questionData.answer = this.multiAnswer.sort().join(',')
              }
            }
            if (this.questionDialogType === 'add') {
              await addQuestionToPaper(this.paperId, questionData)
            } else {
              await updatePaperQuestion(this.paperId, questionData.id, questionData)
            }
            this.$message.success(this.questionDialogType === 'add' ? '添加成功' : '更新成功')
            this.questionDialogVisible = false
            await this.getQuestionList()
          } catch (error) {
            console.error('保存题目失败:', error)
          }
        }
      })
    },
    getQuestionTypeName(type) {
      const types = {
        1: '单选题',
        2: '多选题',
        3: '填空题'
      }
      return types[type] || '未知'
    },
    getQuestionTypeTag(type) {
      const types = {
        1: '',
        2: 'success',
        3: 'warning'
      }
      return types[type] || 'info'
    }
  }
}
</script>

<style scoped>
.paper-add-container {
  padding: 20px;
}

.paper-info {
  margin-bottom: 20px;
}

.question-list {
  margin-bottom: 20px;
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
  position: relative;
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

.question-score {
  color: #f56c6c;
  font-weight: bold;
}

.question-content {
  margin-bottom: 15px;
  line-height: 1.5;
}

.question-options {
  margin-bottom: 15px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.option-label {
  width: 30px;
  margin-right: 10px;
}

.question-answer {
  color: #67c23a;
  margin-bottom: 15px;
}

.answer-label {
  font-weight: bold;
  margin-right: 10px;
}

.question-actions {
  position: absolute;
  top: 20px;
  right: 20px;
}

.el-input {
  margin-right: 10px;
}
</style> 