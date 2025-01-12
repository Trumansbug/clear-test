package com.clear.model.request;

import lombok.Data;

@Data
public class PageRequest {
    private Integer current = 1;
    private Integer size = 10;
} 