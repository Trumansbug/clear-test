package com.clear.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clear.entity.Question;
import com.clear.mapper.QuestionMapper;
import com.clear.service.PaperService;
import com.clear.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private PaperService paperService;

    @Override
    public List<Question> getQuestionsByPaperId(Long paperId) {
        return baseMapper.selectQuestionsByPaperId(paperId);
    }

    @Override
    @Transactional
    public void addQuestion(Question question) {
        // 检查试卷是否可编辑
        paperService.checkPaperCanEdit(question.getPaperId());
        
        // 检查答案格式
        checkAnswerFormat(question);
        
        // 保存题目
        save(question);
        
        // 更新试卷总分
        paperService.updateTotalScore(question.getPaperId());
    }

    @Override
    @Transactional
    public void updateQuestion(Question question) {
        // 检查试卷是否可编辑
        paperService.checkPaperCanEdit(question.getPaperId());
        
        // 检查答案格式
        checkAnswerFormat(question);
        
        // 更新题目
        updateById(question);
        
        // 更新试卷总分
        paperService.updateTotalScore(question.getPaperId());
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = getById(id);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        
        // 检查试卷是否可编辑
        paperService.checkPaperCanEdit(question.getPaperId());
        
        // 删除题目
        removeById(id);
        
        // 更新试卷总分
        paperService.updateTotalScore(question.getPaperId());
    }

    @Override
    public void checkAnswerFormat(Question question) {
        // 检查题目是否重复
        Long count = baseMapper.selectCount(
                new QueryWrapper<Question>()
                        .eq("paper_id", question.getPaperId())
                        .eq("content", question.getContent())
                        .ne(question.getId() != null, "id", question.getId())
        );
        if (count != null && count > 0) {
            throw new RuntimeException("题目内容【" + question.getContent() + "】重复");
        }
    }
} 