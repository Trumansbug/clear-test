<template>
  <div class="papers-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="试卷标题">
          <el-input v-model="searchForm.title" placeholder="请输入试卷标题" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd" v-if="hasRole('ROLE_ADMIN')">新增试卷</el-button>
      <el-button
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
          v-if="hasRole('ROLE_ADMIN')">批量删除</el-button>
    </div>

    <el-table
        ref="multipleTable"
        v-loading="loading"
        :data="paperList"
        @selection-change="handleSelectionChange"
        style="width: 100%"
    >
      <el-table-column type="selection" align="center" />
      <el-table-column prop="title" label="试卷标题" align="center" />
      <el-table-column prop="remark" label="备注" show-overflow-tooltip align="center" />
      <el-table-column prop="totalScore" label="总分" align="center" />
<!--      <el-table-column prop="status" label="状态" align="center">-->
<!--        <template slot-scope="scope">-->
<!--          <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">-->
<!--            {{ scope.row.status === 1 ? '已发布' : '未发布' }}-->
<!--          </el-tag>-->
<!--        </template>-->
<!--      </el-table-column>-->
      <el-table-column prop="creatorName" label="创建人" align="center" />
      <el-table-column prop="createTime" label="创建时间" align="center" :formatter="formatTime" />
      <el-table-column prop="updateTime" label="更新时间" align="center" :formatter="formatTime" />
      <el-table-column label="操作" align="center" min-width="180" fixed="right">
        <template slot-scope="scope">
          <el-button type="primary" link @click="handlePreview(scope.row)">预览</el-button>
          <template v-if="hasRole('ROLE_ADMIN') && scope.row.status === 0">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page.sync="queryParams.current"
      :page-size.sync="queryParams.size"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

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
import { mapGetters } from 'vuex'
import { Message } from 'element-ui'
import { getPaperList, deletePaper, publishPaper, getQuestionsByPaperId, batchDeletePaper } from '@/api/paper'

export default {
  name: 'Papers',
  data() {
    return {
      loading: false,
      paperList: [],
      total: 0,
      selectedIds: [],
      queryParams: {
        current: 1,
        size: 10
      },
      searchForm: {
        title: ''
      },
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
  computed: {
    ...mapGetters(['hasRole']),
    totalScore() {
      return this.questions.reduce((total, question) => {
        return total + question.score
      }, 0)
    },
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = {
          ...this.queryParams,
          ...this.searchForm
        }
        const res = await getPaperList(params)
        this.paperList = res.data.records
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    formatTime(row, column, cellValue) {
      return this.$formatTime(cellValue);
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    handleAdd() {
      this.$router.push('/paper/add')
    },
    handleBatchDelete() {
      this.$confirm('确认删除所选试卷吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await batchDeletePaper(this.selectedIds)
          Message.success('删除成功')
          await this.getList()
        } catch (error) {
          console.error('删除试卷失败:', error)
        }
      })
    },
    handleEdit(row) {
      this.$router.push({
        path: '/paper/add',
        query: { id: row.id }
      })
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
    handleDelete(row) {
      this.$confirm('确认删除该试卷吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deletePaper(row.id)
          this.$message.success('删除成功')
          this.getList()
        } catch (error) {
          console.error('删除试卷失败:', error)
        }
      })
    },
    handlePublish(row) {
      this.$confirm('确认发布该试卷吗？发布后将不能修改和删除', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await publishPaper(row.id)
          this.$message.success('发布成功')
          this.getList()
        } catch (error) {
          console.error('发布试卷失败:', error)
        }
      })
    },
    getQuestionTypeTag(type) {
      const types = {
        1: '',
        2: 'success'
      }
      return types[type] || 'info'
    },
    handleSearch() {
      this.queryParams.current = 1
      this.getList()
    },
    resetSearch() {
      this.searchForm.title = ''
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.queryParams.size = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.queryParams.current = val
      this.getList()
    }
  },
  created() {
    this.getList()
  }
}
</script>

<style scoped>
.papers-container {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.toolbar {
  margin-bottom: 20px;
}

.preview-content {
  padding: 0;
}

.preview-content h2 {
  text-align: center;
}

.description {
  color: #666;
  margin-bottom: 20px;
}

.total-score {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
}

.question-item {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.question-header {
  margin-bottom: 10px;
  font-weight: bold;
}

.question-type {
  margin-left: 10px;
}

.question-content {
  margin-bottom: 10px;
}

.question-options {
  color: #666;
  padding-left: 20px;
}

.question-mode {
  margin-top: 10px;
  color: #606266;
}

.option-score {
  margin-left: 8px;
  color: #F56C6C;
}

.mode-label {
  font-weight: bold;
}
</style> 