package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/10/30 14:44
 */
@RestController
@RequestMapping("/item")
public class ItemController {

    @Reference
    private SpuService spuService;

    @Value("${pagePath}")
    private String pagePath;

    @Reference
    private CategoryService categoryService;

    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping("/createPage")
    public void createPage(String spuId) {

        //查询商品信息

        Goods goods = spuService.findGoodsById(spuId);
        //获取spu信息
        Spu spu = goods.getSpu();
        //获取sku列表
        List<Sku> skuList = goods.getSkuList();
        //批量生成sku页面

        List<String> categoryName = new ArrayList<>();
        categoryName.add(categoryService.findById(spu.getCategory1Id()).getName());
        categoryName.add(categoryService.findById(spu.getCategory2Id()).getName());
        categoryName.add(categoryService.findById(spu.getCategory3Id()).getName());

        //生成SKU地址列表
        Map urlMap=new HashMap();
        for(Sku sku:skuList){
            //对规格json字符串进行排序
            if ("1".equals(sku.getStatus())) {
                String specJson= JSON.toJSONString(JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField );
                urlMap.put(specJson,sku.getId()+".html");
            }

        }

        for (Sku sku : skuList) {
            //创建上下文和数据模型
            Context context = new Context();
            Map<String, Object> map = new HashMap<>();
            map.put("spu", spu);
            map.put("sku", sku);
            map.put("categoryList", categoryName);
            map.put("spuImages", spu.getImages().split(","));
            map.put("skuImages", sku.getImages().split(","));
            Map paraItems = JSON.parseObject(spu.getParaItems());
            Map spec = JSON.parseObject(sku.getSpec());

            map.put("paraItems", paraItems);
            map.put("specs", spec);

            Map<String, List> specItems = (Map) JSON.parseObject(spu.getSpecItems());
            //循环规格
            for (String key : specItems.keySet()) {
                List<String> list = specItems.get(key);
                List<Map> mapList = new ArrayList<>();
                for (String value : list) {
                    Map map1 = new HashMap();
                    map1.put("option", value);
                    if (spec.get(key).equals(value)) {
                        map1.put("checked", true);
                    }else {
                        map1.put("checked", false);
                    }
                    Map<String,String> spec1 = (Map) JSON.parseObject(sku.getSpec());
                    spec1.put(key,value);
                    String specJson = JSON.toJSONString(spec1,SerializerFeature.MapSortField);
                    map1.put("url", urlMap.get(specJson));



                    mapList.add(map1);
                    specItems.put(key, mapList);
                }
            }
            map.put("specItems", specItems);
            context.setVariables(map);
            //准备文件

            File file = new File(pagePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File dest = new File(file, sku.getId() + ".html");
            //生成页面
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(dest, "UTF-8");
                templateEngine.process("item", context, printWriter);
                System.out.println(pagePath+sku.getId() + ".html");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }



        }
    }
}
