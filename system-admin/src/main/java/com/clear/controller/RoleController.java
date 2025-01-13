package com.clear.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clear.annotation.LogRecord;
import com.clear.common.R;
import com.clear.entity.Role;
import com.clear.model.request.PageRequest;
import com.clear.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("list")
    @LogRecord("获取角色列表")
    public R<IPage<Role>> list(PageRequest pageRequest,
                               @RequestParam String name,
                               @RequestParam String code) {
        Page<Role> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        IPage<Role> rolePage = roleService.page(page, new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .like(name != null, Role::getName, name)
                .like(code != null, Role::getCode, code)
                .orderByDesc(Role::getCreateTime));
        return R.success(rolePage);
    }

    @GetMapping("/all")
    @LogRecord("获取所有角色列表")
    public R<List<Role>> listAll() {
        List<Role> roles = roleService.list(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .orderByDesc(Role::getCreateTime));
        return R.success(roles);
    }

    @GetMapping("/{id}")
    @LogRecord("获取角色详情")
    public R<Role> getById(@PathVariable Long id) {
        return R.success(roleService.getById(id));
    }

    @PostMapping("add")
    @LogRecord("添加角色")
    public R<Void> add(@RequestBody Role role) {
        roleService.checkRoleCodeUnique(role.getCode());
        roleService.save(role);
        return R.success();
    }

    @PutMapping("/{id}")
    @LogRecord("修改角色")
    public R<Void> update(@PathVariable Long id, @RequestBody Role role) {
        Role oldRole = roleService.getById(id);
        if (!oldRole.getCode().equals(role.getCode())) {
            roleService.checkRoleCodeUnique(role.getCode());
        }
        role.setId(id);
        roleService.updateById(role);
        return R.success();
    }

    @DeleteMapping("/{id}")
    @LogRecord("删除角色")
    public R<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return R.success();
    }

    @GetMapping("/user/{userId}")
    @LogRecord("获取用户角色列表")
    public R<List<Role>> getRolesByUserId(@PathVariable Long userId) {
        return R.success(roleService.getRolesByUserId(userId));
    }
} 