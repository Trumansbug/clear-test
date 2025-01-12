<template>
  <div class="users-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="userList" style="width: 100%">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="primary" link @click="handleAssignRoles(scope.row)">分配角色</el-button>
          <el-button
            type="danger"
            link
            @click="handleDelete(scope.row)"
            v-if="scope.row.username !== 'admin'"
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

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        ref="userForm"
        :model="userForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
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

    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <span>{{ currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoles">
            <el-checkbox
              v-for="role in roleList"
              :key="role.id"
              :label="role.id"
            >
              {{ role.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, createUser, updateUser, deleteUser, getUserRoles, assignRoles } from '../../api/user'
import { getAllRoles } from '../../api/role'

export default {
  name: 'Users',
  data() {
    return {
      loading: false,
      userList: [],
      total: 0,
      queryParams: {
        current: 1,
        size: 10
      },
      // 用户表单相关
      dialogVisible: false,
      dialogType: 'add',
      userForm: {},
      // 角色相关
      roleDialogVisible: false,
      currentUser: null,
      roleList: [],
      selectedRoles: [],
      // 搜索表单
      searchForm: {
        username: '',
        nickname: ''
      }
    }
  },
  methods: {
    validateEmail(rule, value, callback) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (value && !emailRegex.test(value)) {
        callback(new Error('请输入正确的邮箱地址'))
      } else {
        callback()
      }
    },
    validatePhone(rule, value, callback) {
      const phoneRegex = /^1[3-9]\d{9}$/
      if (value && !phoneRegex.test(value)) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    },
    resetSearch() {
      this.searchForm.username = ''
      this.searchForm.nickname = ''
      this.handleSearch()
    },
    handleSearch() {
      this.queryParams.current = 1
      this.getList()
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          ...this.queryParams,
          ...this.searchForm
        }
        const res = await getUserList(params)
        this.userList = res.data.records
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    async getRoleList() {
      try {
        const res = await getAllRoles()
        this.roleList = res.data
      } catch (error) {
        console.error('获取角色列表失败:', error)
      }
    },
    handleAdd() {
      this.dialogType = 'add'
      this.userForm = {
        status: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.userForm = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确认删除该用户吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteUser(row.id)
          this.$message.success('删除成功')
          this.getList()
        } catch (error) {
          console.error('删除用户失败:', error)
        }
      })
    },
    async handleAssignRoles(row) {
      this.currentUser = row
      try {
        const res = await getUserRoles(row.id)
        this.selectedRoles = res.data.map(role => role.id)
        this.roleDialogVisible = true
      } catch (error) {
        console.error('获取用户角色失败:', error)
      }
    },
    async handleSubmit() {
      await this.$refs.userForm.validate()
      try {
        if (this.dialogType === 'add') {
          await createUser(this.userForm)
          this.$message.success('创建成功')
        } else {
          await updateUser(this.userForm.id, this.userForm)
          this.$message.success('更新成功')
        }
        this.dialogVisible = false
        this.getList()
      } catch (error) {
        console.error('保存用户失败:', error)
      }
    },
    async handleRoleSubmit() {
      try {
        await assignRoles(this.currentUser.id, this.selectedRoles)
        this.$message.success('分配角色成功')
        this.roleDialogVisible = false
      } catch (error) {
        console.error('分配角色失败:', error)
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
  computed: {
    rules() {
      return {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, message: '用户名长度不能小于3位', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { validator: this.validateEmail, trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { validator: this.validatePhone, trigger: 'blur' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      }
    }
  },
  mounted() {
    this.getList()
    this.getRoleList()
  }
}
</script>

<style scoped>
.users-container {
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