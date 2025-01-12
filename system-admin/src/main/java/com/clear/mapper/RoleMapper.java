package com.clear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clear.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {
    
    @Select("SELECT r.* FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<Role> selectRolesByUserId(@Param("userId") Long userId);
} 