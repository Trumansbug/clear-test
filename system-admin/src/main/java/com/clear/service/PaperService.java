package com.clear.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.clear.entity.Paper;

public interface PaperService extends IService<Paper> {
    
    IPage<Paper> getPaperList(Page<Paper> page);
    
    void publishPaper(Long id);
    
    void checkPaperCanEdit(Long id);
    
    void updateTotalScore(Long id);
} 