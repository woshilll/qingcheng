package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {

    /**
     * 通过categoryName查找规格
     * @param categoryName
     * @return
     */
    @Select("select `name`,`options` from tb_spec where template_id in (select template_id from tb_category where name = #{categoryName})")
    List<Map> findListByCategoryName(@Param("categoryName") String categoryName);

}
