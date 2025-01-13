package com.clear.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalCount {
    private Long userCount;

    private Long paperCount;

    private Long questionCount;

    private Long shareCount;
}
