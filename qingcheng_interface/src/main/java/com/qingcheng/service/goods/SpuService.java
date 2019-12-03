package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Spu;

import java.util.*;

/**
 * spu业务逻辑层
 */
public interface SpuService {


    public List<Spu> findAll();


    public PageResult<Spu> findPage(int page, int size);


    public List<Spu> findList(Map<String,Object> searchMap);


    public PageResult<Spu> findPage(Map<String,Object> searchMap,int page, int size);


    public Spu findById(String id);

    public void add(Spu spu);


    public void update(Spu spu);


    public void delete(String id);

    /**
     * 保存
     * @param goods
     */
    void save(Goods goods);

    /**
     * 查找商品
     * @param id
     * @return
     */
    Goods findGoodsById(String id);

    /**
     * 修改审核状态
     * @param id
     * @param status
     * @param message
     */
    void audit(String id, String status, String message);

    /**
     * 商品下架
     * @param id
     */
    void pull(String id);

    /**
     * 商品上架
     * @param id
     */
    void put(String id);

    /**
     * 批量上架
     * @param ids
     * @return
     */
    int putMany(String[] ids);

    /**
     * 批量下架
     * @param ids
     * @return
     */
    int pullMany(String[] ids);

    /**
     * 假删除 将isDelete置为1
     * @param id
     */
    void deleteFalse(String id);

    /**
     * 真实删除，前提判断isDelete是否为1
     * @param id
     */
    void deleteTrue(String id);
}
