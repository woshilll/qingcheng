package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/10/17 17:58
 */
public interface CategoryReportService {
    public List<CategoryReport> categoryReport(LocalDate date);

    /**
     * 创建数据
     */
    void createData();

    /**
     * 统计类目1
     * @param date1
     * @param date2
     * @return
     */
    List<Map> category1Count(String date1, String date2);
}
