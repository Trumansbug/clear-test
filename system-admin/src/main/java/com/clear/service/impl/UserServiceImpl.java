package com.clear.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clear.entity.User;
import com.clear.entity.UserRole;
import com.clear.mapper.UserMapper;
import com.clear.mapper.UserRoleMapper;
import com.clear.model.request.LoginRequest;
import com.clear.service.UserService;
import com.clear.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(userDetails);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        List<String> roles = userRoleMapper.selectUserRoleByUserId(user.getId());
        user.setRoles(roles);
        return user;
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色关联
        userRoleMapper.deleteByUserId(userId);
        
        // 添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> userRoles = roleIds.stream().map(roleId -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                return userRole;
            }).collect(Collectors.toList());
            
            userRoles.forEach(userRole -> userRoleMapper.insert(userRole));
        }
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        return userRoleMapper.selectUserRoleByUserId(userId);
    }

    @Override
    public User getUserById(Long userId) {
        User user = baseMapper.selectById(userId);
        List<String> roles = userRoleMapper.selectUserRoleByUserId(userId);
        List<String> rolesWithPrefix = roles.stream()
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
            .collect(Collectors.toList());
        user.setRoles(rolesWithPrefix);
        return user;
    }
}