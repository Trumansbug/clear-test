package com.clear.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clear.entity.User;
import com.clear.model.request.LoginRequest;
import java.util.List;

public interface UserService extends IService<User> {
    
    String login(LoginRequest request);
    
    User getCurrentUser();
    
    void updateUserRoles(Long userId, List<Long> roleIds);
    
    List<String> getUserRoleCodes(Long userId);

    User getUserById(Long userId);
} 