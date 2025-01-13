package com.clear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clear.entity.Share;
import org.apache.ibatis.annotations.Select;

public interface ShareMapper extends BaseMapper<Share> {
    
    @Select("SELECT s.*, p.title AS paperTitle, su.username AS username FROM share s " +
            "LEFT JOIN paper p ON p.id = s.paper_id " +
            "LEFT JOIN sys_user su ON su.id = s.creator_id " +
            "WHERE s.deleted = 0 " +
            "ORDER BY s.create_time DESC ")
    IPage<Share> selectShareList(Page<Share> page);


    @Select("SELECT s.*, p.title AS paperTitle FROM share s " +
            "LEFT JOIN paper p ON p.id = s.paper_id " +
            "WHERE s.deleted = 0 AND s.code = #{shareCode}")
    Share selectByShareCode(String shareCode);
}