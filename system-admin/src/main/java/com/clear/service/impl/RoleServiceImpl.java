package com.clear.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clear.entity.Role;
import com.clear.mapper.RoleMapper;
import com.clear.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    @Override
    public void checkRoleCodeUnique(String code) {
        Long count = count(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, code)
                .eq(Role::getDeleted, 0));
        if (count > 0) {
            throw new RuntimeException("角色编码已存在");
        }
    }
} 