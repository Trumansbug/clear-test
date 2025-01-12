<template>
  <div class="roles-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>

    <el-table v-loading="loading" :data="roleList" style="width: 100%">
      <el-table-column prop="name" label="角色名称" />
      <el-table-column prop="code" label="角色编码" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button 
            type="danger" 
            link 
            @click="handleDelete(scope.row)"
            :disabled="['ROLE_ADMIN', 'ROLE_USER'].includes(scope.row.code)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.current"
      v-model:page-size="queryParams.size"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
      width="500px"
    >
      <el-form
        ref="roleForm"
        :model="roleForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input 
            v-model="roleForm.code" 
            :disabled="dialogType === 'edit' && ['ROLE_ADMIN', 'ROLE_USER'].includes(roleForm.code)"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole } from '../../api/role'

export default {
  name: 'Roles',
  data() {
    return {
      loading: false,
      roleList: [],
      total: 0,
      queryParams: {
        current: 1,
        size: 10
      },
      dialogVisible: false,
      dialogType: 'add',
      roleForm: {},
      rules: {
        name: [
          { required: true, message: '请输入角色名称', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入角色编码', trigger: 'blur' },
          { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const res = await getRoleList(this.queryParams)
        this.roleList = res.data.records
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    async handleSubmit() {
      await this.$refs.roleForm.validate()
      try {
        if (this.dialogType === 'add') {
          await createRole(this.roleForm)
          ElMessage.success('创建成功')
        } else {
          await updateRole(this.roleForm.id, this.roleForm)
          ElMessage.success('更新成功')
        }
        this.dialogVisible = false
        this.getList()
      } catch (error) {
        console.error('保存角色失败:', error)
      }
    },
    handleAdd() {
      this.dialogType = 'add'
      this.roleForm = {}
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.roleForm = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      if (['ROLE_ADMIN', 'ROLE_USER'].includes(row.code)) {
        ElMessage.warning('系统内置角色不能删除')
        return
      }

      ElMessageBox.confirm('确认删除该角色吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteRole(row.id)
          ElMessage.success('删除成功')
          this.getList()
        } catch (error) {
          console.error('删除角色失败:', error)
        }
      })
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
  mounted() {
    this.getList()
  }
}
</script>

<style scoped>
.roles-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style> 