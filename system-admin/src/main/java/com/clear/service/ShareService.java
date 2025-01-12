package com.clear.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.clear.entity.Paper;
import com.clear.entity.Share;

public interface ShareService extends IService<Share> {
    
    IPage<Share> getShareList(Page<Share> page);

    void addShare(Share share);
}