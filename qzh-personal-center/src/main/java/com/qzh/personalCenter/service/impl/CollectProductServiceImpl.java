package com.qzh.personalCenter.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huidong.qzh.util.common.util.QzhResult;
import com.qzh.personalCenter.entity.CollectProduct;
import com.qzh.personalCenter.feign.StandardFeignClient;
import com.qzh.personalCenter.mapper.CollectProductMapper;
import com.qzh.personalCenter.pojo.CollectionProdutId;
import com.qzh.personalCenter.service.CollectProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class CollectProductServiceImpl implements CollectProductService{
    @Autowired
    private CollectProductMapper collectProductMapper;

    @Autowired
    private StandardFeignClient standardFeignClient;

    /**
     * 收藏产品
     * @param accountId  用户id
     * @param goodsId    货品id
     */
    public void insertCollectProduct(Integer accountId,Integer goodsId){

        CollectProduct collectProduct = new CollectProduct();
        //java8 获取当前时间
        LocalDate now = LocalDate.now();
        //将LocalDate转换成Date
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = now.atStartOfDay(zoneId);
        Date date = Date.from(zdt.toInstant());

        collectProduct.setAccountId(accountId);
        collectProduct.setGoodsId(goodsId);
        collectProduct.setCreateDate(date);
        collectProductMapper.insert(collectProduct);
        //在货品表中添加收藏产品数量
        standardFeignClient.addDelCollectionGoods(goodsId, "add");


    }

    /**
     * 根据accountId 、pageNo、pageSize 分页获取数据
     * @param accountId  用户id
     * @param pageNo      第几页
     * @param pageSize    一页多少条数据
     * @return
     */
    public QzhResult listCollectProduct(Integer accountId, Integer pageNo, Integer pageSize){
        Map<Object,Object> maplist = null;
        try {
            Example example = new Example(CollectProduct.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("accountId",accountId);
            example.setOrderByClause(" id DESC ");
            PageHelper.startPage(pageNo,pageSize);
            Page<CollectProduct> collectProductPage = (Page<CollectProduct>) collectProductMapper.selectByExample(example);
            maplist = new HashMap<>();
            List<Map<Object,Object>> list = new ArrayList();
            collectProductPage.forEach(collectProduct-> {
                QzhResult productGoodsById = standardFeignClient.getProductGoodsById(collectProduct.getGoodsId());
                Object data = productGoodsById.getData();
                Integer status = productGoodsById.getStatus();
                if(status==200&&data!=null){
                    //拿到货品详情
                    Map<Object,Object> map = (Map<Object,Object>)data;
                    map.put("collectProId",collectProduct.getId());
                    list.add(map);
                }
            });
            maplist.put("productGoodsDetail",list);
            maplist.put("pages",collectProductPage.getPages());
            maplist.put("totalCount",collectProductPage.getTotal());
            return QzhResult.ok(maplist);
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.error(e.getMessage());
        }

    }

    /**
     * ids:是收藏商品表的id，删除多个id时，以逗号隔开
     * @param ids  例如（1，2，3）
     */
    public void deletCollectProduct(String ids){
        if(StringUtil.isNotEmpty(ids)){
            String[] idArr = ids.split(",");
            for (int i = 0; i < idArr.length; i++) {
                if(StringUtil.isNotEmpty(idArr[i])){

                    CollectProduct collectProduct = collectProductMapper.selectByPrimaryKey(Integer.parseInt(idArr[i]));
                    standardFeignClient.addDelCollectionGoods(collectProduct.getGoodsId(), "del");

                    collectProductMapper.deleteByPrimaryKey(Integer.parseInt(idArr[i]));

                }
            }
        }
    }

    /**
     * 判断是否收藏商品
     * @param accountId
     * @param goodsId
     * @return
     */
    @Override
    public Integer isCollectProduct(Integer accountId, Integer goodsId) {
        Integer exist = 0;
        try {
            CollectProduct collectProduct = new CollectProduct();
            collectProduct.setAccountId(accountId);
            collectProduct.setGoodsId(goodsId);
            collectProduct = collectProductMapper.selectOne(collectProduct);
            if(collectProduct!=null){
                exist = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exist;
    }

}
