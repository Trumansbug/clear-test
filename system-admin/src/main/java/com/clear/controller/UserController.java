package com.clear.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clear.common.R;
import com.clear.entity.User;
import com.clear.model.request.PageRequest;
import com.clear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("list")
    public R<IPage<User>> list(PageRequest pageRequest,
                               @RequestParam(required = false) String username,
                               @RequestParam(required = false) String nickname) {
        Page<User> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        IPage<User> userPage = userService.page(page, new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .like(username != null, User::getUsername, username)
                .like(nickname != null, User::getNickname, nickname)
                .orderByDesc(User::getCreateTime));
        return R.success(userPage);
    }

    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        return R.success(userService.getUserById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        return R.success();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userService.updateById(user);
        return R.success();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.success();
    }

    @PutMapping("/{id}/roles")
    public R<Void> updateRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.updateUserRoles(id, roleIds);
        return R.success();
    }
} 