package com.qingcheng.service.goods;

import java.util.Map;

/**
 * @author 李洋
 * @date 2019/11/10 15:06
 */
public interface SkuSearchService {

    /**
     * 查询
     * @param searchMap
     * @return
     */
    Map<String, Object> search(Map<String, String> searchMap);
}
