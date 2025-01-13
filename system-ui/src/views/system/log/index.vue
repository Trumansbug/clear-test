<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-wrapper">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.operator"
            placeholder="请输入操作人"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作描述">
          <el-input
            v-model="queryParams.methodDesc"
            placeholder="请输入操作描述"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <el-card class="table-wrapper">
      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="logList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="50" align="center" />
        <el-table-column label="用户名" prop="operator" min-width="100" show-overflow-tooltip />
        <el-table-column label="请求方法" prop="methodName" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作描述" prop="methodDesc" min-width="150" show-overflow-tooltip />
        <el-table-column label="请求URL" prop="apiPath" min-width="150" show-overflow-tooltip />
        <el-table-column label="请求参数" prop="requestParams" min-width="150" show-overflow-tooltip />
        <el-table-column label="请求URL" prop="responseData" min-width="150" show-overflow-tooltip />
        <el-table-column label="IP地址" prop="ipAddress" min-width="120" show-overflow-tooltip />
        <el-table-column label="执行时长" min-width="100" align="center">
          <template #default="{ row }">
            {{ row.executionTime }}ms
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作时间" prop="createTime" min-width="160" show-overflow-tooltip :formatter="formatTime"/>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { getLogList, deleteLog, batchDeleteLog, clearLog } from '@/api/system/log'
import { Message, MessageBox } from 'element-ui'

export default {
  name: 'SystemLog',
  data() {
    return {
      // 加载状态
      loading: false,
      // 日志列表数据
      logList: [],
      // 总记录数
      total: 0,
      // 选中的日志ID
      selectedIds: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        operator: null,
        methodDesc: null,
        status: null,
        startTime: undefined,
        endTime: undefined
      },
      // 日期范围
      dateRange: []
    }
  },
  created() {
    this.getList()
  },
  methods: {
    // 获取日志列表
    async getList() {
      this.loading = true
      try {
        // 处理日期范围
        if (this.dateRange && this.dateRange.length === 2) {
          this.queryParams.startTime = this.dateRange[0]
          this.queryParams.endTime = this.dateRange[1]
        } else {
          this.queryParams.startTime = undefined
          this.queryParams.endTime = undefined
        }

        const res = await getLogList(this.queryParams)
        this.logList = res.data.records
        this.total = res.data.total
      } catch (error) {
        console.error('获取日志列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    formatTime(row, column, cellValue) {
      return this.$formatTime(cellValue);
    },
    // 搜索按钮操作
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    // 重置按钮操作
    resetQuery() {
      this.dateRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        operator: null,
        methodDesc: null,
        status: null,
        startTime: undefined,
        endTime: undefined
      }
      this.getList()
    },
    // 选择条数
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },
    // 选择页码
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    // 删除单条日志
    handleDelete(row) {
      MessageBox.confirm('确认要删除该条日志吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteLog(row.id)
          Message.success('删除成功')
          await this.getList()
        } catch (error) {
          console.error('删除日志失败:', error)
        }
      })
    },
    // 批量删除日志
    handleBatchDelete() {
      if (this.selectedIds.length === 0) {
        Message.warning('请选择要删除的日志')
        return
      }
      MessageBox.confirm(`确认要删除选中的 ${this.selectedIds.length} 条日志吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await batchDeleteLog(this.selectedIds)
          Message.success('删除成功')
          this.getList()
        } catch (error) {
          console.error('批量删除日志失败:', error)
        }
      })
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-wrapper {
  margin-bottom: 20px;
}

.table-wrapper {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 