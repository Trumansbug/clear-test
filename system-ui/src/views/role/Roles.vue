<template>
  <div class="roles-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>

    <el-table v-loading="loading" :data="roleList" style="width: 100%">
      <el-table-column prop="name" label="角色名称" />
      <el-table-column prop="code" label="角色编码" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
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
import { ref, reactive, onMounted } from 'vue'
import { Message, MessageBox } from 'element-ui'
import { getRoleList, createRole, updateRole, deleteRole, getRolePermissions, updateRolePermissions } from '../../api/role'
import { getPermissionTree } from '../../api/permission'

export default {
  name: 'Roles',
  setup() {
    const loading = ref(false)
    const roleList = ref([])
    const total = ref(0)
    const queryParams = reactive({
      current: 1,
      size: 10
    })

    // 角色表单相关
    const dialogVisible = ref(false)
    const dialogType = ref('add')
    const roleForm = ref({})
    const roleFormRef = ref(null)
    const rules = {
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
    }

    // 权限相关
    const permissionDialogVisible = ref(false)
    const currentRole = ref(null)
    const permissionTree = ref([])
    const selectedPermissions = ref([])
    const permissionTreeRef = ref(null)
    const defaultProps = {
      children: 'children',
      label: 'name'
    }

    // 获取角色列表
    const getList = async () => {
      loading.value = true
      try {
        const res = await getRoleList(queryParams)
        roleList.value = res.data.records
        total.value = res.data.total
      } finally {
        loading.value = false
      }
    }

    // 获取权限树
    const getPermissionTreeData = async () => {
      try {
        const res = await getPermissionTree()
        permissionTree.value = res.data
      } catch (error) {
        console.error('获取权限树失败:', error)
      }
    }

    // 新增角色
    const handleAdd = () => {
      dialogType.value = 'add'
      roleForm.value = {
        status: 1
      }
      dialogVisible.value = true
    }

    // 编辑角色
    const handleEdit = (row) => {
      dialogType.value = 'edit'
      roleForm.value = { ...row }
      dialogVisible.value = true
    }

    // 删除角色
    const handleDelete = (row) => {
      MessageBox.confirm('确认删除该角色吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteRole(row.id)
          Message.success('删除成功')
          getList()
        } catch (error) {
          console.error('删除角色失败:', error)
        }
      })
    }

    // 权限设置
    const handlePermissions = async (row) => {
      currentRole.value = row
      try {
        const res = await getRolePermissions(row.id)
        selectedPermissions.value = res.data.map(p => p.id)
        permissionDialogVisible.value = true
      } catch (error) {
        console.error('获取角色权限失败:', error)
      }
    }

    // 提交角色表单
    const handleSubmit = async () => {
      await roleFormRef.value.validate()
      try {
        if (dialogType.value === 'add') {
          await createRole(roleForm.value)
          Message.success('创建成功')
        } else {
          await updateRole(roleForm.value.id, roleForm.value)
          Message.success('更新成功')
        }
        dialogVisible.value = false
        getList()
      } catch (error) {
        console.error('保存角色失败:', error)
      }
    }

    // 提交权限设置
    const handlePermissionSubmit = async () => {
      const checkedKeys = permissionTreeRef.value.getCheckedKeys()
      const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
      const permissionIds = [...checkedKeys, ...halfCheckedKeys]
      try {
        await updateRolePermissions(currentRole.value.id, permissionIds)
        Message.success('权限设置成功')
        permissionDialogVisible.value = false
      } catch (error) {
        console.error('设置权限失败:', error)
      }
    }

    // 分页相关
    const handleSizeChange = (val) => {
      queryParams.size = val
      getList()
    }

    const handleCurrentChange = (val) => {
      queryParams.current = val
      getList()
    }

    onMounted(() => {
      getList()
      getPermissionTreeData()
    })

    return {
      loading,
      roleList,
      total,
      queryParams,
      dialogVisible,
      dialogType,
      roleForm,
      roleFormRef,
      rules,
      permissionDialogVisible,
      currentRole,
      permissionTree,
      selectedPermissions,
      permissionTreeRef,
      defaultProps,
      handleAdd,
      handleEdit,
      handleDelete,
      handlePermissions,
      handleSubmit,
      handlePermissionSubmit,
      handleSizeChange,
      handleCurrentChange
    }
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