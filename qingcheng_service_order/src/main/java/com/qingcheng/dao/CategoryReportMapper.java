package com.qingcheng.dao;

import com.qingcheng.pojo.order.CategoryReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/10/17 17:47
 */
public interface CategoryReportMapper extends Mapper<CategoryReport> {

    @Select("SELECT oi.category_id1 categoryId1, oi.category_id2 categoryId2, oi.category_id3 categoryId3, DATE_FORMAT(o.pay_time,'%Y-%m-%d') countDate , SUM(oi.num) num, SUM(oi.pay_money) money\n" +
            "FROM tb_order_item oi, tb_order o\n" +
            "WHERE oi.order_id = o.id AND o.pay_status = '1' AND o.is_delete = '0' AND DATE_FORMAT(o.pay_time,'%Y-%m-%d') = #{date}\n" +
            "GROUP BY oi.category_id1 , oi.category_id2 , oi.category_id3 , DATE_FORMAT(o.pay_time,'%Y-%m-%d')")
    public List<CategoryReport> categoryReport(@Param("date") LocalDate date);

    @Select("SELECT cr.category_id1 categoryId1, v.name categoryName, SUM(cr.num) num, SUM(cr.money) money\n" +
            "FROM tb_category_report cr, v_category1 v\n" +
            "WHERE cr.count_date >= #{date1} AND cr.count_date <= #{date2} AND cr.category_id1 = v.id\n" +
            "GROUP BY cr.category_id1, v.name")
    List<Map> category1Count(@Param("date1")String date1, @Param("date2")String date2);
}
