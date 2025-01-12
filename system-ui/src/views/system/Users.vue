<template>
  <div class="users-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="userList" style="width: 100%">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="primary" link @click="handleRoles(scope.row)">角色</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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
          <el-input v-model="userForm.password" type="password" />
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
          <el-switch
            v-model="userForm.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 角色分配对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="500px"
    >
      <el-checkbox-group v-model="selectedRoles">
        <el-checkbox
          v-for="role in roleList"
          :key="role.id"
          :label="role.id"
        >
          {{ role.name }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, updateUserRoles } from '../../api/user'
import { getRoleList } from '../../api/role'

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
      dialogVisible: false,
      dialogType: 'add',
      userForm: {},
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ],
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' }
        ]
      },
      roleDialogVisible: false,
      roleList: [],
      selectedRoles: [],
      currentUserId: null
    }
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const res = await getUserList(this.queryParams)
        this.userList = res.data.records
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    async getRoles() {
      try {
        const res = await getRoleList()
        this.roleList = res.data
      } catch (error) {
        console.error('获取角色列表失败:', error)
      }
    },
    async handleSubmit() {
      await this.$refs.userForm.validate()
      try {
        if (this.dialogType === 'add') {
          await createUser(this.userForm)
          ElMessage.success('创建成功')
        } else {
          await updateUser(this.userForm.id, this.userForm)
          ElMessage.success('更新成功')
        }
        this.dialogVisible = false
        this.getList()
      } catch (error) {
        console.error('保存用户失败:', error)
      }
    },
    async handleRoleSubmit() {
      try {
        await updateUserRoles(this.currentUserId, this.selectedRoles)
        ElMessage.success('角色分配成功')
        this.roleDialogVisible = false
      } catch (error) {
        console.error('角色分配失败:', error)
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
    async handleRoles(row) {
      this.currentUserId = row.id
      this.selectedRoles = row.roleIds || []
      this.roleDialogVisible = true
    },
    handleDelete(row) {
      ElMessageBox.confirm('确认删除该用户吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteUser(row.id)
          ElMessage.success('删除成功')
          this.getList()
        } catch (error) {
          console.error('删除用户失败:', error)
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
    this.getRoles()
  }
}
</script>

<style scoped>
.users-container {
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