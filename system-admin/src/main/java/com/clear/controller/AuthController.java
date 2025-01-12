package com.clear.controller;

import com.clear.common.R;
import com.clear.entity.User;
import com.clear.model.request.LoginRequest;
import com.clear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        User user = userService.getCurrentUser();
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return R.success(data);
    }

    @GetMapping("/info")
    public R<User> getUserInfo() {
        return R.success(userService.getCurrentUser());
    }
} 