package com.clear.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clear.entity.Paper;
import com.clear.entity.Share;
import com.clear.mapper.PaperMapper;
import com.clear.mapper.QuestionMapper;
import com.clear.service.PaperService;
import com.clear.service.ShareService;
import com.clear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperService paperService;

    @Autowired
    private ShareService shareService;

    @Autowired
    private UserService userService;

    @Override
    public IPage<Paper> getPaperList(Page<Paper> page) {
        return baseMapper.selectPaperList(page);
    }

    @Override
    public void publishPaper(Long id) {
        Paper paper = getById(id);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }
        
        // 检查是否有题目
        int questionCount = baseMapper.countQuestionsByPaperId(id);
        if (questionCount == 0) {
            throw new RuntimeException("试卷中没有题目，不能发布");
        }
        
        // 更新试卷状态为已发布
        paper.setStatus(1);
        updateById(paper);
    }

    @Override
    public void checkPaperCanEdit(Long id) {
        Paper paper = getById(id);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }
        if (paper.getStatus() == 1) {
            throw new RuntimeException("试卷已发布，不能编辑");
        }
    }

    @Override
    public void updateTotalScore(Long id) {
        Integer totalScore = questionMapper.selectTotalScoreByPaperId(id);
        Paper paper = new Paper();
        paper.setId(id);
        paper.setTotalScore(totalScore != null ? totalScore : 0);
        updateById(paper);
    }

    @Override
    @Transactional
    public void savePaper(Paper paper) {
        // 检查是否可以保存
        checkPaperCanSave(paper);

        // 设置基础信息
        paper.setCreatorId(userService.getCurrentUser().getId());
        paper.setStatus(0);
        Date currentDate = new Date();
        paper.setCreateTime(currentDate);
        paper.setUpdateTime(currentDate);

        super.save(paper);
    }

    @Override
    public Paper getPaperByShareCode(String shareCode) {
        Share share = shareService.getByShareCode(shareCode);
        if (share == null || share.getStatus() != 1) {
            throw new RuntimeException("系统异常，请联系管理员");
        }

        if (share.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new RuntimeException("分享码已过期");
        }

        Paper paper = baseMapper.selectById(share.getPaperId());
        if (paper == null) {
            throw new RuntimeException("系统异常，请联系管理员");
        }

        paper.setQuestions(questionMapper.selectQuestionsByPaperId(share.getPaperId()));

        return paper;
    }

    private void checkPaperCanSave(Paper paper) {
        Long count = baseMapper.selectCount(new QueryWrapper<Paper>().eq(paper.getTitle() != null, "title", paper.getTitle()));
        if (count != null && count > 0) {
            throw new RuntimeException("试卷标题【" + paper.getTitle() + "】重复");
        }
    }
}