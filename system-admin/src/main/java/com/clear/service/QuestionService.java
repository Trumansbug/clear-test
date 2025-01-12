package com.clear.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clear.entity.Question;
import java.util.List;

public interface QuestionService extends IService<Question> {
    
    List<Question> getQuestionsByPaperId(Long paperId);
    
    void addQuestion(Question question);
    
    void updateQuestion(Question question);
    
    void deleteQuestion(Long id);
    
    void checkAnswerFormat(Question question);
} 