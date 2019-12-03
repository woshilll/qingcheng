package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/10/30 11:10
 */
@Controller
public class IndexController {

    @Reference
    private AdService adService;

    @Reference
    private CategoryService categoryService;

    @GetMapping("/index")
    public String index(Model model) {
        List<Ad> index_lb = adService.findByPosition("web_index_lb");
        model.addAttribute("lbt", index_lb);

        List<Map> categoryList = categoryService.findCategoryTree();

        model.addAttribute("categoryList", categoryList);
        return "index";
    }
}
