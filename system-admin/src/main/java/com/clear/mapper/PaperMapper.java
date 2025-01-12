package com.clear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clear.entity.Paper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PaperMapper extends BaseMapper<Paper> {
    
    @Select("SELECT p.*, u.nickname as creatorName FROM paper p " +
            "LEFT JOIN sys_user u ON p.creator_id = u.id " +
            "WHERE p.deleted = 0 " +
            "ORDER BY p.create_time DESC")
    IPage<Paper> selectPaperList(Page<Paper> page);
    
    @Select("SELECT COUNT(*) FROM question WHERE paper_id = #{paperId} AND deleted = 0")
    int countQuestionsByPaperId(@Param("paperId") Long paperId);
} 