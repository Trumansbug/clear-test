package com.clear.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clear.entity.Share;
import com.clear.mapper.ShareMapper;
import com.clear.service.ShareService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements ShareService {

    @Override
    public IPage<Share> getShareList(Page<Share> page) {
        return baseMapper.selectShareList(page);
    }

    @Override
    @Transactional
    public void addShare(Share share) {
        checkAddShare(share);

        String code = generateShareCode();
        share.setCode(code);

        share.setCreateTime(new Date());

        this.save(share);
    }

    private void checkAddShare(Share share) {
        if (share.getExpireTime().before(new Date())) {
            throw new RuntimeException("过期时间必须大于当前时间");
        }


        Share s = baseMapper.selectOne(
                new QueryWrapper<Share>()
                        .eq("expire_time", share.getExpireTime())
                        .eq("paper_id", share.getPaperId())
        );

        if (s != null) {
            throw new RuntimeException("已存在相同过期时间的分享码【" + share.getCode() + "】");
        }
    }

    private String generateShareCode() {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();

        // 生成一个随机数
        Random random = new Random();
        int randomNumber = random.nextInt(10000); // 生成0到9999之间的随机数

        // 组合时间戳和随机数
        String combinedString = timestamp + String.format("%04d", randomNumber);

        // 使用MD5哈希函数生成固定长度的字符串
        String hash = md5(combinedString);

        // 截取前八位作为分享码

        return hash.substring(0, 8).toUpperCase();
    }

    // MD5哈希函数
    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}