package com.clear.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clear.entity.User;
import com.clear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 查询用户角色
        return new UserDetailsImpl(user, userService.getUserRoleCodes(user.getId()));
    }
} 