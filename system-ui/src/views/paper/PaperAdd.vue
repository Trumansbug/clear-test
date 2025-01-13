<template>
  <div class="paper-add-container">
    <el-card class="paper-info">
      <template #header>
        <div class="card-header">
          <span>{{ $route.query.id ? '编辑试卷' : '新增试卷' }}</span>
          <div>
            <el-button type="primary" @click="handleSave">保存</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-form ref="paperForm" :model="paperForm" :rules="rules" label-width="120px">
        <el-form-item label="试卷标题" prop="title">
          <el-input v-model="paperForm.title" placeholder="请输入试卷标题" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
              type="textarea"
              :rows="3"
              v-model="paperForm.remark"
              placeholder="请输入备注"
          />
        </el-form-item>

        <el-form-item label="试卷描述" prop="description">
          <vue-editor
            v-model="paperForm.description"
            :editor-toolbar="editorToolbar"
            placeholder="请输入试卷描述"
          />
        </el-form-item>

        <el-form-item label="试卷解析" prop="analysis">
          <vue-editor
            v-model="paperForm.analysis"
            :editor-toolbar="editorToolbar"
            placeholder="请输入试卷解析"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 试题列表 -->
    <el-card class="question-list" v-if="paperId">
      <template #header>
        <div class="card-header">
          <span>试题列表</span>
          <el-button type="primary" @click="handleAddQuestion">添加试题</el-button>
        </div>
      </template>

      <div v-if="!questionList.length" class="empty-text">
        暂无试题，请点击上方按钮添加
      </div>

      <div v-else class="questions-container">
        <div v-for="(question, index) in questionList" :key="question.id" class="question-item">
          <div class="question-header">
            <span class="question-index">第 {{ index + 1 }} 题</span>
            <el-tag :type="getQuestionTypeTag(question.type)" class="question-type">
              {{ getQuestionTypeName(question.type) }}
            </el-tag>
            <div class="question-operations">
              <el-button type="primary" link @click="handleEditQuestion(question)">编辑</el-button>
              <el-button type="danger" link @click="handleDeleteQuestion(question)">删除</el-button>
            </div>
          </div>
          <div class="question-content">{{ question.content }}</div>
          <div class="question-options">
            <div v-for="(option, key) in JSON.parse(question.options)" :key="key" class="option-item">
              {{ String.fromCharCode(65 + Number(key)) }}. {{ option.content }}
              <span class="option-score">({{ option.score }}分)</span>
            </div>
          </div>
          <div v-if="question.type === 2" class="question-mode">
            <span class="mode-label">判断模式：</span>
            <span>{{ question.judgeMode === 1 ? '多选得分（必须选择所有正确答案）' : '多选得分（选择部分正确答案也可得分）' }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑试题对话框 -->
    <el-dialog
      :title="questionDialogType === 'add' ? '添加试题' : '编辑试题'"
      :visible.sync="questionDialogVisible"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form ref="questionForm" :model="questionForm" :rules="questionRules" label-width="100px">
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="questionForm.type" placeholder="请选择题目类型">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
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
        <el-form-item label="选项">
          <div v-for="(option, index) in questionOptions" :key="index" class="option-item">
            <div class="option-row">
              <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
              <div class="option-input-container">
                <el-input 
                  v-model="questionOptions[index]" 
                  placeholder="请输入选项内容" 
                  @blur="validateOptionContent(index)"
                  :class="{ 'is-error': optionErrors[index] }"
                />
                <div v-if="optionErrors[index]" class="option-error">{{ optionErrors[index] }}</div>
              </div>
              <el-input-number 
                v-model="optionScores[index]" 
                :min="0" 
                :max="100" 
                size="small"
                placeholder="分值"
                style="width: 100px;"
              />
              <el-button type="danger" link @click="removeOption(index)" v-if="questionOptions.length > 2" style="margin-left: 10px">
                删除
              </el-button>
            </div>
          </div>
          <el-button type="primary" link @click="addOption" v-if="questionOptions.length < 6">
            添加选项
          </el-button>
          <div class="score-tip">
            <template v-if="questionForm.type === 1">
              题目分值：{{ questionForm.score = getMaxScore }}分（取最高分值选项）
            </template>
            <template v-else>
              题目分值：{{ questionForm.score = getTotalScore }}分（所有分值之和）
            </template>
          </div>
          <div v-if="scoreError" class="score-error">{{ scoreError }}</div>
        </el-form-item>
        <el-form-item v-if="questionForm.type === 2" label="判断模式" prop="judgeMode">
          <el-radio-group v-model="questionForm.judgeMode">
            <el-radio :label="1">全部对得分</el-radio>
            <el-radio :label="2">部分对得分</el-radio>
          </el-radio-group>
          <div class="mode-tip">
            <template v-if="questionForm.judgeMode === 1">
              只有选中所有正确答案才能得分（不能选中任何错误答案）
            </template>
            <template v-else>
              选中了部分正确答案可以得分（不能选中任何错误答案）
            </template>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleQuestionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Message, MessageBox } from 'element-ui'
import { createPaper, getPaper, updatePaper } from '@/api/paper'
import { getQuestionsByPaperId, addQuestionToPaper, updatePaperQuestion, deleteQuestionFromPaper } from '@/api/paper'
import { VueEditor } from 'vue2-editor'

export default {
  name: 'PaperAdd',
  components: {
    VueEditor
  },
  data() {
    return {
      paperId: null,
      loading: false,
      paperForm: {
        title: '',
        remark: '',
        description: '',
        analysis: '',
        questionList: []
      },
      rules: {
        title: [
          { required: true, message: '请输入试卷标题', trigger: 'blur' },
          { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
        ]
      },
      editorToolbar: [
        ['bold', 'italic', 'underline', 'strike'],
        ['blockquote', 'code-block'],
        [{ 'header': 1 }, { 'header': 2 }],
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
        [{ 'script': 'sub'}, { 'script': 'super' }],
        [{ 'indent': '-1'}, { 'indent': '+1' }],
        [{ 'direction': 'rtl' }],
        [{ 'size': ['small', false, 'large', 'huge'] }],
        [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
        [{ 'font': [] }],
        [{ 'color': [] }, { 'background': [] }],
        [{ 'align': [] }],
        ['clean'],
        ['link', 'image']
      ],
      questionList: [],
      questionDialogVisible: false,
      questionDialogType: 'add',
      questionForm: {
        type: 1,
        judgeMode: 2,
        content: '',
        score: 0
      },
      questionOptions: ['', ''],
      optionScores: [0, 0],
      optionErrors: [],
      scoreError: '',
      scoreErrorIndexes: [],
      questionRules: {
        type: [
          { required: true, message: '请选择题目类型', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入题目内容', trigger: 'blur' }
        ],
        judgeMode: [
          { required: true, message: '请选择判断模式', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    getMaxScore() {
      return Math.max(...this.optionScores)
    },
    getTotalScore() {
      return this.optionScores.reduce((sum, score) => sum + score, 0)
    }
  },
  watch: {
    optionScores: {
      handler() {
        this.validateOptionScores()
      },
      deep: true
    },
    'questionForm.type'() {
      this.validateOptionScores()
    }
  },
  methods: {
    async getPaperInfo() {
      if (!this.paperId) return
      try {
        const res = await getPaper(this.paperId)
        if (res.data) {
          this.paperForm.title = res.data.title || ''
          this.paperForm.remark = res.data.remark || ''
          this.paperForm.description = res.data.description || ''
          this.paperForm.analysis = res.data.analysis || ''
          await this.getQuestionList()
        }
      } catch (error) {
        Message.error('获取试卷信息失败：' + error?.message)
      }
    },
    async handleSave() {
      try {
        await this.$refs.paperForm.validate()
        const params = {
          title: this.paperForm.title,
          remark: this.paperForm.remark,
          description: this.paperForm.description,
          analysis: this.paperForm.analysis
        }
        if (this.paperId) {
          await updatePaper(this.paperId, params)
          Message.success('更新成功')
        } else {
          const res = await createPaper(params)
          if (res.data) {
            this.paperId = res.data.id
            Message.success('创建成功')
          }
        }
      } catch (error) {
        if (error === false) return
        Message.error('保存失败：' + error?.message)
      }
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
        judgeMode: 2,
        content: '',
        score: 0
      }
      this.questionOptions = ['', '']
      this.optionScores = [0, 0]
      this.optionErrors = []
      this.scoreError = ''
      this.scoreErrorIndexes = []
      this.questionDialogVisible = true
    },
    handleEditQuestion(row) {
      this.questionDialogType = 'edit'
      this.questionForm = {
        id: row.id,
        type: row.type,
        content: row.content,
        judgeMode: row.judgeMode,
        score: row.score
      }
      
      // 解析选项和分值
      const options = JSON.parse(row.options)
      this.questionOptions = []
      this.optionScores = []
      
      for (const option of options) {
        this.questionOptions.push(option.content)
        this.optionScores.push(option.score)
      }
      
      this.questionDialogVisible = true
    },
    async handleQuestionSubmit() {
      // 验证表单
      if (!this.validateAllOptions() || !this.validateOptionScores()) {
        return
      }

      const options = this.questionOptions.map((content, index) => ({
        content,
        score: this.optionScores[index]
      }))

      const questionData = {
        ...this.questionForm,
        options: JSON.stringify(options),
        paperId: this.paperId
      }

      try {
        if (this.questionDialogType === 'add') {
          await addQuestionToPaper(questionData)
          Message.success('添加成功')
        } else {
          await updatePaperQuestion(questionData)
          Message.success('更新成功')
        }
        
        // 刷新题目列表
        await this.getQuestionList()
        this.handleDialogClose()
      } catch (error) {
        Message.error('操作失败：' + error?.message)
      }
    },
    async handleDeleteQuestion(row) {
      try {
        await MessageBox.confirm('确认删除该题目吗？', '提示', {
          type: 'warning'
        })
        
        await deleteQuestionFromPaper(row.id)
        Message.success('删除成功')
        await this.getQuestionList()
      } catch (error) {
        if (error !== 'cancel') {
          Message.error('删除失败：' + error?.message)
        }
      }
    },
    async getQuestionList() {
      try {
        const res = await getQuestionsByPaperId(this.paperId)
        this.questionList = res.data
      } catch (error) {
        Message.error('获取题目列表失败：' + error?.message)
      }
    },
    validateOptionContent(index) {
      const content = this.questionOptions[index]
      if (!content || !content.trim()) {
        this.$set(this.optionErrors, index, '选项内容不能为空')
        return false
      }

      // 检查选项内容是否重复
      const trimmedContent = content.trim()
      const duplicateIndex = this.questionOptions.findIndex((option, idx) => 
        idx !== index && option.trim() === trimmedContent
      )
      
      if (duplicateIndex !== -1) {
        this.$set(this.optionErrors, index, `选项内容与选项${String.fromCharCode(65 + duplicateIndex)}重复`)
        return false
      }

      this.$set(this.optionErrors, index, '')
      return true
    },
    validateAllOptions() {
      let isValid = true
      this.optionErrors = this.questionOptions.map(() => '')
      
      // 检查每个选项是否为空
      this.questionOptions.forEach((content, index) => {
        if (!content || !content.trim()) {
          this.$set(this.optionErrors, index, '选项内容不能为空')
          isValid = false
        }
      })

      if (!isValid) return false

      // 检查选项是否重复
      for (let i = 0; i < this.questionOptions.length; i++) {
        const content = this.questionOptions[i].trim()
        for (let j = i + 1; j < this.questionOptions.length; j++) {
          if (content === this.questionOptions[j].trim()) {
            this.$set(this.optionErrors, i, `选项内容与选项${String.fromCharCode(65 + j)}重复`)
            this.$set(this.optionErrors, j, `选项内容与选项${String.fromCharCode(65 + i)}重复`)
            isValid = false
          }
        }
      }

      return isValid
    },
    validateOptionScores() {
      this.scoreErrorIndexes = []
      
      if (this.questionForm.type === 1) {
        // 单选题：至少有一个选项分值非0
        if (!this.optionScores.some(score => score > 0)) {
          this.scoreError = '单选题至少需要一个选项分值大于0'
          this.scoreErrorIndexes = this.optionScores.map((score, index) => score === 0 ? index : -1).filter(index => index !== -1)
          return false
        }
      } else {
        // 多选题：至少有两个选项分值非0
        const nonZeroCount = this.optionScores.filter(score => score > 0).length
        if (nonZeroCount < 2) {
          this.scoreError = '多选题至少需要两个选项分值大于0'
          this.scoreErrorIndexes = this.optionScores.map((score, index) => score === 0 ? index : -1).filter(index => index !== -1)
          return false
        }
      }
      this.scoreError = ''
      return true
    },
    addOption() {
      if (this.questionOptions.length >= 6) {
        Message.warning('最多只能添加6个选项')
        return
      }
      this.questionOptions.push('')
      this.optionScores.push(0)
    },
    removeOption(index) {
      if (this.questionOptions.length <= 2) {
        Message.warning('至少需要2个选项')
        return
      }
      this.questionOptions.splice(index, 1)
      this.optionScores.splice(index, 1)
      this.validateOptionScores()
    },
    handleDialogClose() {
      this.questionDialogVisible = false
      this.questionForm = {
        type: 1,
        judgeMode: 2,
        content: ''
      }
      this.questionOptions = ['', '']
      this.optionScores = [0, 0]
      this.optionErrors = []
      this.scoreError = ''
      this.scoreErrorIndexes = []
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
    }
  },
  mounted() {
    // 从 URL 获取试卷 ID
    const id = this.$route.query.id
    if (id) {
      this.paperId = id
      this.getPaperInfo()
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

.question-content {
  margin-bottom: 15px;
  line-height: 1.5;
}

.question-options {
  margin-bottom: 15px;
}

.option-item {
  margin-bottom: 10px;
}

.option-score {
  margin-left: 8px;
  color: #F56C6C;
}

.question-mode {
  color: #606266;
  font-size: 13px;
}

.mode-label {
  font-weight: bold;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

/* 富文本编辑器样式 */
:deep(.ql-container) {
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
}

:deep(.ql-editor) {
  font-size: 14px;
  line-height: 1.5;
}

:deep(.ql-toolbar) {
  line-height: 1;
}

/* 选项相关样式 */
.option-row {
  display: flex;
  align-items: center;
  min-height: 32px;
  position: relative;
}

.option-label {
  margin-right: 8px;
  line-height: 32px;
}

.option-input-container {
  flex: 1;
  margin-right: 10px;
  position: relative;
}

.option-error {
  position: absolute;
  color: #F56C6C;
  font-size: 12px;
  line-height: 1;
  top: 100%;
  left: 0;
  margin-top: 4px;
  white-space: nowrap;
}

.score-tip {
  margin-top: 8px;
  color: #409EFF;
  font-size: 13px;
}

.score-error {
  color: #F56C6C;
  font-size: 12px;
  margin-top: 8px;
}

.mode-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
  line-height: 1.4;
}

.is-error :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #F56C6C inset !important;
}

.score-input-error :deep(.el-input-number__wrapper) {
  box-shadow: 0 0 0 1px #F56C6C inset !important;
}

.score-input-error :deep(.el-input-number__decrease),
.score-input-error :deep(.el-input-number__increase) {
  border-color: #F56C6C !important;
}

.score-input-error :deep(.el-input-number__decrease:hover),
.score-input-error :deep(.el-input-number__increase:hover) {
  color: #F56C6C;
}
</style> 