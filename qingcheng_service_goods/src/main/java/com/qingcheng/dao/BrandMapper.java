package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface BrandMapper extends Mapper<Brand> {

    /**
     * 通过商品分类名查找品牌
     * @param categoryName
     * @return
     */
    @Select("select name, image from tb_brand WHERE id in (\n" +
            "select brand_id from tb_category_brand WHERE category_id in (\n" +
            "select id from tb_category WHERE name = #{name}\n" +
            ")\n" +
            ")")
    List<Map> findListByCategoryName(@Param("name") String categoryName);
}
