package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.BrandMapper;
import com.qingcheng.dao.SpecMapper;
import com.qingcheng.service.goods.SkuSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

/**
 * @author 李洋
 * @date 2019/11/10 15:07
 */
@Service
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpecMapper specMapper;

    public Map<String, Object> search(Map<String, String> searchMap) {

        //封装查询请求
        SearchRequest searchRequest = new SearchRequest("sku");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //查询条件
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keywords"));
        boolQueryBuilder.must(matchQueryBuilder);

        //商品分类过滤查询
        if (searchMap.get("category") != null) {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryName", searchMap.get("category"));
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //商品厂商过滤查询
        if (searchMap.get("brand") != null) {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", searchMap.get("brand"));
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //规格过滤
        for (String key : searchMap.keySet()) {
            if (key.startsWith("spec.")) {
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key + ".keyword", searchMap.get(key));
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //价格过滤
        if (searchMap.get("price") != null) {
            String[] prices = searchMap.get("price").split("-");
            if (!prices[0].equals("0")) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(prices[0] + "00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
            if (!prices[1].equals("*")) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(prices[1] + "00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);

        //分页
        Integer index = Integer.parseInt(searchMap.get("pageNo"));
        //每页大小
        Integer pageSize = 30;
        searchSourceBuilder.from((index - 1) * pageSize);
        searchSourceBuilder.size(pageSize);



        //商品排序
        String sort = searchMap.get("sort");
        String sortOrder = searchMap.get("sortOrder");
        if (!"".equals(sort)) {
            searchSourceBuilder.sort(sort, SortOrder.valueOf(sortOrder));
        }



        //高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags("<font style='color:red'>").postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);


        searchRequest.source(searchSourceBuilder);
        //聚合查询（商品）
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("sku_category").field("categoryName");
        searchSourceBuilder.aggregation(termsAggregationBuilder);


        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        //获取查询结果
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] hits1 = hits.getHits();
            for (SearchHit documentFields : hits1) {
                Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();

                //高亮设置
                Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                HighlightField name = highlightFields.get("name");
                Text[] fragments = name.fragments();
                //覆盖
                sourceAsMap.put("name", fragments[0].toString());
                resultList.add(sourceAsMap);
            }
            result.put("rows", resultList);


            //商品列表分类
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
            Terms terms = (Terms) aggregationMap.get("sku_category");
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            List<String> categoryList = new ArrayList<String>();
            for (Terms.Bucket bucket : buckets) {
                categoryList.add(bucket.getKeyAsString());
            }
            result.put("categoryList", categoryList);


            String categoryName = "";
            //如果查询条件中没有品牌分类，就从得到的品牌分类列表中查出第一个
            if (searchMap.get("category") == null) {
                if (categoryList.size() > 0) {
                    categoryName = categoryList.get(0);
                }
            } else {
                //通过查询条件查找
                categoryName = searchMap.get("category");
            }


            //查询品牌
            if (searchMap.get("brand") == null) {

                List<Map> brandList = brandMapper.findListByCategoryName(categoryName);

                result.put("brandList", brandList);
            }
            //查询规格
            List<Map> specList = specMapper.findListByCategoryName(categoryName);
            for (Map map : specList) {
                String option = (String)map.get("options");
                String[] options = option.split(",");
                map.put("options", options);
            }

            result.put("specList", specList);

            //页码
            long totalHits = hits.getTotalHits();
            long pageCount = (totalHits % pageSize == 0) ? totalHits/pageSize : (totalHits/pageSize + 1);
            result.put("totalPages", pageCount);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
