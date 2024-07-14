package com.github.ningasekiro.config;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DAGVO {
    private List<NodeVO> nodes;

    private List<EdgeVO> edges;

    private Map<String, String> parameters;
}
