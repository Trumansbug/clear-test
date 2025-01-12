<template>
  <div class="paper-edit-container">
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

    <el-card class="question-list" v-if="paperId">
      <template #header>
        <div class="card-header">
          <span>题目列表</span>
          <el-button type="primary" @click="handleAddQuestion">添加题目</el-button>
        </div>
      </template>

      <el-table :data="questionList" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip align="center"/>
        <el-table-column prop="type" label="题目类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getQuestionTypeTag(scope.row.type)">
              {{ getQuestionTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEditQuestion(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDeleteQuestion(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { MessageBox, Message } from 'element-ui'
import { getPaper, createPaper, updatePaper } from '../../api/paper'
import { getQuestionsByPaperId, addQuestionToPaper, updatePaperQuestion, deleteQuestionFromPaper } from '../../api/paper'

export default {
  name: 'PaperEdit',
  data() {
    return {
      paperForm: {
        title: '',
        description: ''
      },
      rules: {
        title: [
          { required: true, message: '请输入试卷标题', trigger: 'blur' }
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
        score: [
          { required: true, message: '请设置分值', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    paperId() {
      return this.$route.params.id
    }
  },
  methods: {
    async getPaperInfo() {
      if (!this.paperId) return
      try {
        const res = await getPaper(this.paperId)
        this.paperForm = res.data
        this.getQuestionList()
      } catch (error) {
        console.error('获取试卷信息失败:', error)
      }
    },
    async getQuestionList() {
      try {
        const res = await getQuestionsByPaperId(this.paperId)
        this.questionList = res.data
      } catch (error) {
        console.error('获取题目列表失败:', error)
      }
    },
    async handleSave() {
      await this.$refs.paperForm.validate()
      try {
        if (this.paperId) {
          await updatePaper(this.paperId, this.paperForm)
        } else {
          const res = await createPaper(this.paperForm)
          this.$router.replace(`/paper/edit/${res.data.id}`)
        }
        Message.success('保存成功')
      } catch (error) {
        console.error('保存试卷失败:', error)
      }
    },
    handleBack() {
      this.$router.push('/paper/list')
    },
    handleAddQuestion() {
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
      MessageBox.confirm('确认删除该题目吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteQuestionFromPaper(this.paperId, row.id)
          Message.success('删除成功')
          this.getQuestionList()
        } catch (error) {
          console.error('删除题目失败:', error)
        }
      }).catch(() => {
        
      })
    },
    addOption() {
      this.questionOptions.push('')
    },
    removeOption(index) {
      this.questionOptions.splice(index, 1)
    },
    async handleQuestionSubmit() {
      await this.$refs.questionForm.validate()
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
        Message.success(this.questionDialogType === 'add' ? '添加成功' : '更新成功')
        this.questionDialogVisible = false
        this.getQuestionList()
      } catch (error) {
        console.error('保存题目失败:', error)
      }
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
  },
  mounted() {
    this.getPaperInfo()
  }
}
</script>

<style scoped>
.paper-edit-container {
  padding: 20px;
}

.paper-info {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.el-input {
  margin-right: 10px;
}
</style> 