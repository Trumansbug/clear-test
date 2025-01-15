<template>
  <div class="papers-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="试卷标题">
          <el-input v-model="searchForm.code" placeholder="请输入分享码" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd" v-if="hasRole('ROLE_ADMIN')">新增分享</el-button>
      <el-button
          type="danger"
          @click="handleBatchDelete"
          v-if="hasRole('ROLE_ADMIN')"
          :disabled="selectedIds.length === 0">批量删除</el-button>
    </div>

    <el-table
        ref="multipleTable"
        v-loading="loading"
        :data="shareList"
        @selection-change="handleSelectionChange"
        style="width: 100%">
      <el-table-column type="selection" align="center" />
      <el-table-column prop="code" label="分享码" align="center" />
      <el-table-column prop="paperTitle" label="关联试卷" align="center" />
      <el-table-column prop="remark" label="备注" show-overflow-tooltip align="center" />
      <el-table-column prop="status" label="状态" align="center">
        <template slot-scope="scope">
          <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="updateShareStatus(scope.row)"
              active-color="#13ce66"
              inactive-color="#ff4949">
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="creatorName" label="创建人" align="center" />
      <el-table-column prop="createTime" label="创建时间" align="center" :formatter="formatTime" />
      <el-table-column prop="expireTime" label="过期时间" align="center" :formatter="formatTime" />
      <el-table-column label="操作" align="center" min-width="180" fixed="right">
        <template slot-scope="scope">
          <el-button id="copyUrlBtn" type="primary" v-if="scope.row.status === 1" :data-clipboard-text="testUrl" link @click="copyTestUrl(scope.row)">获取链接</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :visible.sync="addDialogVisible" title="新增分享">
      <el-form ref="addShareForm" :model="addShareForm" label-width="100px" :rules="addShareFormRules">
        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker
            v-model="addShareForm.expireTime"
            type="datetime"
            placeholder="选择日期时间"
            prop="expireTime"
            :picker-options="{ disabledDate(time) { return time.getTime() < Date.now(); } }"
          />
        </el-form-item>
        <el-form-item label="选择试卷" prop="paperId" :inline="true">
          <el-button @click="openPaperSelection">选择试卷</el-button>
          <span> {{ selectedPaperTitle }} </span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
              type="textarea"
              :rows="3"
              v-model="addShareForm.remark"
              placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddShare">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="paperDialogVisible" title="选择试卷">
      <div class="search-bar-paper-dialog">
        <el-form :inline="true" :model="searchPaperForm">
          <el-form-item label="试卷标题">
            <el-input v-model="searchPaperForm.title" placeholder="请输入试卷标题" clearable @clear="handlePaperSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handlePaperSearch">搜索</el-button>
            <el-button @click="resetPaperSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
          :data="paperList"
          highlight-current-row
          @row-click="selectPaper"
          v-loading="loadingPaper"
      >
        <el-table-column prop="title" label="试卷标题" align="center" />
        <el-table-column prop="remark" label="描述" show-overflow-tooltip align="center" />
        <el-table-column prop="totalScore" label="总分" align="center" />
        <el-table-column prop="creatorName" label="创建人" align="center" />
        <el-table-column prop="createTime" label="创建时间" align="center" :formatter="formatTime" />
      </el-table>
      <el-pagination
          :current-page.sync="queryPaperParams.current"
          :page-size.sync="queryPaperParams.size"
          :total="totalPaper"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handlePaperSizeChange"
          @current-change="handlePaperCurrentChange"
      />
    </el-dialog>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import {Message} from 'element-ui'
import {batchDeleteShare, changeShareStatus, createShare, deleteShare, getShareList} from '@/api/share'
import {getPaperList} from '@/api/paper'
import Clipboard from 'clipboard'


export default {
  name: 'Shares',
  data() {
    return {
      loading: false,
      loadingPaper: false,
      shareList: [],
      total: 0,
      totalPaper: 0,
      selectedIds: [],
      queryParams: {
        current: 1,
        size: 10
      },
      queryPaperParams: {
        current: 1,
        size: 10
      },
      searchForm: {
        code: ''
      },
      addDialogVisible: false,
      paperDialogVisible: false,
      addShareForm: {
        remark: '',
        expireTime: '',
        paperId: null
      },
      selectedPaperTitle: '',
      paperList: [],
      searchPaperForm: {
        title: ''
      },
      testUrl: '',
      addShareFormRules: {
        expireTime: [
          { required: true, message: '请选择过期时间', trigger: 'blur' }
        ],
        paperId: [
          { required: true, message: '请选择试卷', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['hasRole'])
  },
  watch: {
    'addShareForm.paperId': function (newVal) {
      this.$refs.addShareForm.validateField('paperId');
    }
  },
  methods: {
    copyTestUrl(row){
      // 获取当前程序运行的 URL
      const baseUrl = window.location.origin
      // 拼接完整的测试链接
      this.testUrl = `${baseUrl}/paper/doTest/${row.code}`;

      let clipBoard = new Clipboard('#copyUrlBtn');
      clipBoard.on('success', function() {
        clipBoard.destroy() // 销毁上一次的复制内容
        clipBoard = new Clipboard('#copyUrlBtn')
        Message.success('获取完成，已复制到剪贴板')
      })
      clipBoard.on('error', function() {
        Message.info('复制失败，请手动复制')
      })
    },
    formatTime(row, column, cellValue) {
      return this.$formatTime(cellValue);
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          ...this.queryParams,
          ...this.searchForm
        }
        const res = await getShareList(params)
        this.shareList = res.data.records
        this.total = res.data.total
      } catch (error) {
        console.error('获取试卷列表失败:', error?.message)
      } finally {
        this.loading = false
      }
    },
    updateShareStatus(row) {
      const oldStatus = row.status === 1 ? 0 : 1;
      try {
        const params = {
          id: row.id,
          status: row.status
        }
        const res = changeShareStatus(params)
      } catch (error) {
        console.error('更新分享状态失败:', error);
        Message.error('更新分享状态失败:' + error?.message)
        row.status = oldStatus;
      }
    },
    handleAdd() {
      this.addDialogVisible = true;
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    handleBatchDelete() {
      this.$confirm('确认删除所选分享码吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await batchDeleteShare(this.selectedIds)
          Message.success('删除成功')
          await this.getList()
        } catch (error) {
          console.error('删除试卷失败:', error)
        }
      })
    },
    handleEdit(row) {
      this.$router.push(`/paper/edit/${row.id}`)
    },
    handleDelete(row) {
      this.$confirm('确认删除该分享吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteShare(row.id)
          Message.success('删除成功')
          await this.getList()
        } catch (error) {
          console.error('删除试卷失败: ', error)
          Message.error('删除分享失败: ' + error?.message)
        }
      })
    },
    handleSearch() {
      this.queryParams.current = 1
      this.getList()
    },
    resetSearch() {
      this.searchForm.title = ''
      this.handleSearch()
    },
    handlePaperSearch() {
      this.queryParams.current = 1
      this.getPapers()
    },
    resetPaperSearch() {
      this.searchForm.title = ''
      this.handlePaperSearch()
    },
    handlePaperSizeChange(val) {
      this.queryPaperParams.size = val
      this.getList()
    },
    handlePaperCurrentChange(val) {
      this.queryPaperParams.current = val
      this.getList()
    },
    handleSizeChange(val) {
      this.queryParams.size = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.queryParams.current = val
      this.getList()
    },
    openPaperSelection() {
      this.paperDialogVisible = true;
      this.loadPaperList();
    },
    async getPapers() {
      this.loadingPaper = true
      try {
        const params = {
          ...this.queryPaperParams,
          ...this.searchPaperForm
        }
        const res = await getPaperList(params)
        this.paperList = res.data.records
        this.totalPaper = res.data.total
      } finally {
        this.loadingPaper = false
      }
    },
    loadPaperList() {
      this.getPapers()
    },
    selectPaper(row) {
      this.addShareForm.paperId = row.id;
      this.selectedPaperTitle = row.title;
      this.paperDialogVisible = false;
    },
    handleAddShare() {
      this.$refs.addShareForm.validate(async valid => {
        if (valid) {
          try {
            const res = await createShare(this.addShareForm)
            Message.success('创建成功')
            this.resetSearch();
            this.addDialogVisible = false;
            this.resetAddDialog();
          } catch (error) {
            Message.error('保存试卷失败：' + error?.message)
          }
        }
      })
    },
    resetAddDialog() {
      this.addShareForm = {
        remark: '',
        expireTime: '',
        paperId: null
      };
      this.selectedPaperTitle = null
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

.search-bar-paper-dialog {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
}

.toolbar {
  margin-bottom: 20px;
}

.preview-content h2 {
  text-align: center;
  margin-bottom: 20px;
}
</style> 