package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 李洋
 * @date 2019/11/5 09:20
 */
@Component
public class Init implements InitializingBean {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuService skuService;
    public void afterPropertiesSet() throws Exception {
        System.out.println("-----缓存预热----");
        categoryService.saveCategoryTreeToRedis();
        skuService.saveAllPriceToRedis();
        categoryService.saveAllCategoryToRedis();
    }
}
