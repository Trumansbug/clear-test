package com.clear.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clear.entity.Role;
import java.util.List;

public interface RoleService extends IService<Role> {
    
    List<Role> getRolesByUserId(Long userId);
    
    void checkRoleCodeUnique(String code);
} 