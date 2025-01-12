package com.clear.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clear.common.R;
import com.clear.entity.Paper;
import com.clear.entity.Question;
import com.clear.entity.Share;
import com.clear.entity.User;
import com.clear.model.request.PageRequest;
import com.clear.service.PaperService;
import com.clear.service.QuestionService;
import com.clear.service.ShareService;
import com.clear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public R<IPage<Share>> list(PageRequest pageRequest) {
        Page<Share> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        return R.success(shareService.getShareList(page));
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody Share share) {
        User currentUser = userService.getCurrentUser();
        share.setCreatorId(currentUser.getId());
        shareService.addShare(share);
        return R.success("添加成功");
    }

    @GetMapping("/changeStatus")
    public R<String> changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        Share share = new Share();
        share.setId(id);
        share.setStatus(status);
        shareService.updateById(share);
        return R.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        shareService.removeById(id);
        return R.success();
    }

    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                shareService.removeById(id);
            }
        }

        return R.success();
    }
} 