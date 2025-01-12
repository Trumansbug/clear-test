<template>
  <div class="roles-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.name" placeholder="请输入用户名" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="searchForm.code" placeholder="请输入昵称" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>

    <el-table v-loading="loading" :data="roleList" style="width: 100%">
      <el-table-column prop="name" label="角色名称" />
      <el-table-column prop="code" label="角色编码" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="primary" link @click="handlePermissions(scope.row)">权限设置</el-button>
          <el-button
            type="danger"
            link
            @click="handleDelete(scope.row)"
            v-if="scope.row.code !== 'ROLE_ADMIN'"
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
          <el-input v-model="roleForm.code" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="权限设置"
      width="600px"
    >
      <el-form label-width="80px">
        <el-form-item label="角色名称">
          <span>{{ currentRole?.name }}</span>
        </el-form-item>
        <el-form-item label="权限">
          <el-tree
            ref="permissionTree"
            :data="permissionTree"
            :props="defaultProps"
            show-checkbox
            node-key="id"
            :default-checked-keys="selectedPermissions"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Message, MessageBox } from 'element-ui'
import { getRoleList, createRole, updateRole, deleteRole, getRolePermissions, updateRolePermissions } from '../../api/role'
import { getPermissionTree } from '../../api/permission'

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
          { pattern: /^ROLE_[A-Z_]+$/, message: '角色编码必须以ROLE_开头，且只能包含大写字母和下划线', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      },
      permissionDialogVisible: false,
      currentRole: null,
      permissionTree: [],
      selectedPermissions: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      },
      searchForm: {
        name: '',
        code: ''
      }
    }
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = {
          ...this.queryParams,
          ...this.searchForm
        }
        const res = await getRoleList(params)
        this.roleList = res.data.records
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    async getPermissionTreeData() {
      try {
        const res = await getPermissionTree()
        this.permissionTree = res.data
      } catch (error) {
        console.error('获取权限树失败:', error)
      }
    },
    resetSearch() {
      this.searchForm.name = ''
      this.searchForm.code = ''
      this.handleSearch()
    },
    handleSearch() {
      this.queryParams.current = 1
      this.getList()
    },
    handleAdd() {
      this.dialogType = 'add'
      this.roleForm = {
        status: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.roleForm = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      MessageBox.confirm('确认删除该角色吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteRole(row.id)
          Message.success('删除成功')
          this.getList()
        } catch (error) {
          console.error('删除角色失败:', error)
        }
      })
    },
    async handlePermissions(row) {
      this.currentRole = row
      try {
        const res = await getRolePermissions(row.id)
        this.selectedPermissions = res.data.map(p => p.id)
        this.permissionDialogVisible = true
      } catch (error) {
        console.error('获取角色权限失败:', error)
      }
    },
    async handleSubmit() {
      this.$refs.roleForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.dialogType === 'add') {
              await createRole(this.roleForm)
              Message.success('创建成功')
            } else {
              await updateRole(this.roleForm.id, this.roleForm)
              Message.success('更新成功')
            }
            this.dialogVisible = false
            this.getList()
          } catch (error) {
            console.error('保存角色失败:', error)
          }
        }
      })
    },
    async handlePermissionSubmit() {
      const checkedKeys = this.$refs.permissionTree.getCheckedKeys()
      const halfCheckedKeys = this.$refs.permissionTree.getHalfCheckedKeys()
      const permissionIds = [...checkedKeys, ...halfCheckedKeys]
      try {
        await updateRolePermissions(this.currentRole.id, permissionIds)
        Message.success('权限设置成功')
        this.permissionDialogVisible = false
      } catch (error) {
        console.error('设置权限失败:', error)
      }
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
    this.getPermissionTreeData()
  }
}
</script>

<style scoped>
.roles-container {
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

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style> 