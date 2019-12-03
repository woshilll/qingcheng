package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author 李洋
 * @date 2019/11/10 15:30
 */
@Controller
public class SearchController {

    @Reference
    private SkuSearchService skuSearchService;

    @GetMapping("/search")
    public String search(@RequestParam Map<String, String> searchMap, Model model) throws Exception {
        //字符集处理，解决中文乱码
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);
        //如果没传分页 就设置起始页
        searchMap.putIfAbsent("pageNo", "1");


        //设置排序误差值 sort:排序类型 sortOrder:排序规则
        searchMap.putIfAbsent("sort", "");
        searchMap.putIfAbsent("sortOrder", "DESC");




        Map<String, Object> search = skuSearchService.search(searchMap);

        //拼接URL
        StringBuffer stringBuffer = new StringBuffer("/search.do?");
        for (String key : searchMap.keySet()) {
            stringBuffer.append("&" + key + "=" + searchMap.get(key));
        }



        model.addAttribute("result", search);
        model.addAttribute("url", stringBuffer);
        model.addAttribute("searchMap", searchMap);

        //设置起始页
        int pageNo = Integer.parseInt(searchMap.get("pageNo"));
        model.addAttribute("pageNo", pageNo);

        Long totalPages = (Long) search.get("totalPages");
        //起始页
        int startPage = 1;
        //结束页
        int endPage = totalPages.intValue();
        if (totalPages > 5) {
            startPage = pageNo - 2;
            if (startPage < 1) {
                startPage = 1;
            }
            endPage = startPage + 4;
        }
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "search";
    }
}
