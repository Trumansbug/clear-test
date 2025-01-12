package com.clear.controller;

import com.clear.common.R;
import com.clear.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/global")
public class GlobalController {

    @Autowired
    private PaperService paperService;

    @GetMapping("/count")
    public R<Map<String, Object>> count() {

        return R.success();
    }
}
