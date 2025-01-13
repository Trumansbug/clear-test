package com.clear.aspect;

import cn.hutool.core.util.StrUtil;
import com.clear.annotation.LogRecord;
import com.clear.entity.ApiLog;
import com.clear.mapper.ApiLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Aspect
@Component
public class LogAspect {

    private final ApiLogMapper apiLogMapper;
    private final ObjectMapper objectMapper;

    public LogAspect(ApiLogMapper apiLogMapper, ObjectMapper objectMapper) {
        this.apiLogMapper = apiLogMapper;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(logRecord)")
    public Object around(ProceedingJoinPoint point, LogRecord logRecord) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        ApiLog apiLog = new ApiLog();
        
        try {
            // 记录请求参数
            MethodSignature signature = (MethodSignature) point.getSignature();
            String methodName = signature.getMethod().getName();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = point.getArgs();
            
            // 获取请求信息
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails = 
                        (org.springframework.security.core.userdetails.UserDetails) principal;
                    apiLog.setOperator(userDetails.getUsername());
                }
            }
            
            apiLog.setApiPath(request.getRequestURI());
            apiLog.setMethodName(methodName);
            apiLog.setMethodDesc(logRecord.value());
            String requestParamsJson = objectMapper.writeValueAsString(args);
            apiLog.setRequestParams(
                    StrUtil.isNotEmpty(requestParamsJson) &&  requestParamsJson.length() > 2000 ? requestParamsJson.substring(0, 2000) : requestParamsJson
            );
            apiLog.setIpAddress(getIpAddress(request));
            
            // 执行原方法
            result = point.proceed();
            
            // 记录响应结果
            String responseDataJson = objectMapper.writeValueAsString(result);
            apiLog.setResponseData(
                    StrUtil.isNotEmpty(responseDataJson) && responseDataJson.length() > 2000 ? responseDataJson.substring(0, 2000) : responseDataJson
            );
            // 设置请求成功
            apiLog.setStatus(1);
            
        } catch (Exception e) {
            // 设置请求失败
            apiLog.setStatus(0);
            apiLog.setErrorMessage(e.getMessage());
            apiLog.setResponseData("Error: " + e.getMessage());
            throw e;
        } finally {
            // 计算执行时间
            apiLog.setExecutionTime(System.currentTimeMillis() - startTime);
            apiLog.setCreateTime(new Date());
            
            // 使用MyBatis-Plus保存日志
            apiLogMapper.insert(apiLog);
        }
        
        return result;
    }
    
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 