package com.clear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clear.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("SELECT code FROM sys_role sr LEFT JOIN sys_user_role sur ON sur.role_id = sr.id WHERE sur.user_id = #{userId}")
    List<String> selectUserRoleByUserId(@Param("userId") Long userId);
} 