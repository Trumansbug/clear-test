package com.clear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clear.entity.Question;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {
    
    @Select("SELECT * FROM question WHERE paper_id = #{paperId} AND deleted = 0 ORDER BY order_num")
    List<Question> selectQuestionsByPaperId(@Param("paperId") Long paperId);
    
    @Select("SELECT SUM(score) FROM question WHERE paper_id = #{paperId} AND deleted = 0")
    Integer selectTotalScoreByPaperId(@Param("paperId") Long paperId);
} 