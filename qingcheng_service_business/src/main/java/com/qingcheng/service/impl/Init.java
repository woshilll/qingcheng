package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 李洋
 * @date 2019/11/5 09:47
 */
@Component
public class Init implements InitializingBean {

    @Autowired
    private AdService adService;
    public void afterPropertiesSet() throws Exception {
        adService.saveAllAdsToRedis();
    }
}
