package com.qzh.store.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huidong.qzh.util.common.util.QzhResult;
import com.mongodb.util.JSON;
import com.qzh.store.entity.StoreCategory;
import com.qzh.store.mapper.StoreCategoryMapper;
import com.qzh.store.service.StoreProductService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreProductServiceImpl implements StoreProductService{
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private StoreCategoryMapper storeCategoryMapper;

    /**
     * 商品分类管理  通过 名称  价格  发布与否   分类与否 搜索某商家商品
     * @param info
     * @return
     */
    @Override
    public QzhResult listGoodsCategory(String info, Integer pageNo, Integer pageSize,Integer memberId) {
        try {
            Map<String,Object> map = new HashMap<>();
            map.put("info",info);
            map.put("pageNo",pageNo);
            map.put("pageSize",pageSize);
            map.put("memberId",memberId);
            String url = "http://QZH-STANDARD/productGoods/listGoodsByParam?info={info}&pageNo={pageNo}&pageSize={pageSize}&memberId={memberId}";

            ResponseEntity<QzhResult> responseEntity = restTemplate.getForEntity(url, QzhResult.class, map);
            QzhResult body = responseEntity.getBody();
            if(body.getStatus()!=200){
                return body;
            }
            if(body.getData()!=null){
                Map<Object,Object> dataMap = (Map<Object,Object>)body.getData();
                if(dataMap.get("list")!=null){
                    List<Map<Object,Object>> mapList = (List<Map<Object,Object>>)dataMap.get("list");
                    for(int a=0;a<mapList.size();a++){
                        Map<Object, Object> a_map = mapList.get(a);
                        //分类
                        Object customCategoryId = a_map.get("customCategoryId");
                        if(customCategoryId!=null){
                            String categoryName_new = "";
                            String[] split_customCategoryId = customCategoryId.toString().split(",");
                            for(int b=0;b<split_customCategoryId.length;b++){
                                if(StringUtils.isNotBlank(split_customCategoryId[b])){
                                    StoreCategory storeCategory = new StoreCategory();
                                    storeCategory.setId(Integer.parseInt(split_customCategoryId[b]));
                                    storeCategory.setLevel(2);
                                    storeCategory = storeCategoryMapper.selectOne(storeCategory);
                                    if(storeCategory!=null){
                                        String categoryName = storeCategory.getCategoryName();
                                        categoryName_new += categoryName+",";
                                    }
                                }
                            }
                            if(StringUtils.isNotBlank(categoryName_new)){
                                categoryName_new = categoryName_new.substring(0,categoryName_new.length()-1);
                            }
                            a_map.put("customCategoryId",categoryName_new);
                        }
                    }
                }
            }
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.error(e.getMessage());
        }
    }


    /**
     * 店铺商品分类管理  修改/设置分类
     * @param goodsIds
     * @param categoryIds
     * @return
     */
    @Override
    public QzhResult updateGoodsCategory(String goodsIds, String categoryIds) {
        try {
            Map<String,Object> map = new HashMap<>();
            map.put("goodsIds",goodsIds);
            map.put("categoryIds",categoryIds);
            String url = "http://QZH-STANDARD/productGoods/updateGoodsCategory?goodsIds={goodsIds}&categoryIds={categoryIds}";

            ResponseEntity<QzhResult> responseEntity = restTemplate.getForEntity(url, QzhResult.class, map);
            QzhResult body = responseEntity.getBody();
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.error(e.getMessage());
        }
    }
}
