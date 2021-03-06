package com.qzh.store.service.Impl;

import com.huidong.qzh.util.common.util.QzhResult;
import com.huidong.qzh.util.common.util.StringUtils;
import com.mongodb.util.JSON;
import com.qzh.store.entity.StoreCategory;
import com.qzh.store.entity.StoreInformation;
import com.qzh.store.feign.FILEFeignClient;
import com.qzh.store.mapper.StoreCategoryMapper;
import com.qzh.store.mapper.StoreInformationMapper;
import com.qzh.store.service.DecorationRankingService;
import com.qzh.store.service.StoreCategoryService;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreCategoryServiceImpl implements StoreCategoryService{
    @Autowired
    private StoreInformationMapper informationMapper;
    @Autowired
    private StoreCategoryMapper categoryMapper;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private FILEFeignClient fileFeignClient;
    @Autowired
    private DecorationRankingService decorationRankingService;

    /**
     * 获取店铺所有的店铺商品自定义分类
     * @return
     */
    @Override
    public QzhResult listStoreCategory(Integer memberId) {
        StoreInformation information = new StoreInformation();
        information.setMemberId(memberId);
        information = informationMapper.selectOne(information);
        if(information!=null){
            Integer storeId = information.getId();

            StoreCategory category = new StoreCategory();
            category.setStoreId(storeId);
            category.setParentId(0);
            category.setLevel(1);
            List<StoreCategory> categoryList1 = categoryMapper.select(category);
            List<Map<Object,Object>> mapList = new ArrayList<Map<Object,Object>>();
            categoryList1.forEach(storeCategory -> {
                StoreCategory category2 = new StoreCategory();
                category2.setParentId(storeCategory.getId());
                category2.setLevel(2);
                List<StoreCategory> categoryList2 = categoryMapper.select(category2);

                BeanMap beanMap1 = new BeanMap(storeCategory);
                Map<Object,Object> map1 = new HashMap<Object,Object>();
                map1.putAll(beanMap1);
                map1.remove("class");
                map1.put("level2",categoryList2);
                mapList.add(map1);
            });
            return QzhResult.ok(mapList);

        }else{
            return QzhResult.error("没有店铺信息！");
        }
    }

    /**
     * 店铺自定义分类的新增
     * @param info
     * @return
     */
    @Override
    public QzhResult insertStoreCategory(String info,Integer storeId) {
        List<Map<String,Object>> mapList = (List<Map<String,Object>>) JSON.parse(info);
        List<StoreCategory> storeCategoryList = new ArrayList<StoreCategory>();//一级
        List<StoreCategory> storeCategoryList2 = new ArrayList<StoreCategory>();//二级
        try {
            for(int m=0;m<mapList.size();m++){
                Map<String,Object> map = mapList.get(m);
                //分类名称
                Object categoryName = map.get("categoryName");
                if(categoryName!=null){
                    StoreCategory storeCategory = new StoreCategory();
                    storeCategory.setCategoryName(categoryName.toString());
                    //图片
                    Object categoryPic = map.get("categoryPic");
                    if(categoryPic!=null){
                        storeCategory.setCategoryPic(categoryPic.toString());
                    }
                    //排序
                    Object sort = map.get("sort");
                    if(sort!=null){
                        storeCategory.setSort(Integer.parseInt(sort.toString()));
                    }
                    //保存
                    if(storeCategory!=null){
                        //等级
                        storeCategory.setLevel(1);
                        storeCategory.setParentId(0);
                        storeCategory.setStoreId(storeId);
                        //主键
                        String id = map.get("id").toString();
                        if(!StringUtils.equalsNull(id)){
                            storeCategory.setId(Integer.parseInt(id));
                            categoryMapper.updateByPrimaryKey(storeCategory);
                        }else{
                            categoryMapper.insert(storeCategory);
                        }

                        storeCategoryList.add(storeCategory);
                        //二级
                        List<Map<String,Object>> maplist2 = (List<Map<String,Object>>)map.get("level2");
                        for(int n=0;n<maplist2.size();n++){
                            Map<String,Object> map2 = maplist2.get(n);
                            StoreCategory storeCategory2 = new StoreCategory();
                            //名称
                            Object categoryName2 = map2.get("categoryName");
                            if(categoryName2!=null){
                                storeCategory2.setCategoryName(categoryName2.toString());
                                //图片
                                Object categoryPic2 = map2.get("categoryPic");
                                if(categoryPic2!=null){
                                    storeCategory2.setCategoryPic(categoryPic2.toString());
                                }
                                //排序
                                Object sort2 = map2.get("sort");
                                if(sort2!=null){
                                    storeCategory2.setSort(Integer.parseInt(sort2.toString()));
                                }
                                //保存
                                if(storeCategory2!=null) {
                                    //等级
                                    storeCategory2.setLevel(2);
                                    storeCategory2.setParentId(storeCategory.getId());
                                    storeCategory2.setStoreId(storeId);
                                    //主键
                                    String id2 = map2.get("id").toString();
                                    if(!StringUtils.equalsNull(id2)){
                                        storeCategory2.setId(Integer.parseInt(id2));
                                        categoryMapper.updateByPrimaryKey(storeCategory2);
                                    }else{
                                        categoryMapper.insert(storeCategory2);
                                    }

                                    storeCategoryList2.add(storeCategory2);
                                }
                            }
                        }
                    }
                }
            }
            return QzhResult.ok("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            if(storeCategoryList!=null&&storeCategoryList.size()!=0){
                storeCategoryList.forEach(category1 -> {
                    categoryMapper.delete(category1);
                });
            }
            if(storeCategoryList2!=null&&storeCategoryList2.size()!=0){
                storeCategoryList2.forEach(category2 -> {
                    categoryMapper.delete(category2);
                });
            }
            return QzhResult.error("insert is error");
        }

    }

    /**
     * 删除  批量删除
     * @param ids
     * @return
     */
    @Override
    public QzhResult deleteStoreCategory(String ids) {
        try {
            String replace_ids = ids.replace(",", "");
            if(StringUtils.equalsNull(replace_ids)){
                return QzhResult.error("parameter is error !");
            }
            String[] split_ids = ids.split(",");
            for(int a=0;a<split_ids.length;a++){
                if(!StringUtils.equalsNull(split_ids[a])){
                    //判断是否有子类，并一起删除
                    StoreCategory storeCategory = new StoreCategory();
                    storeCategory.setParentId(Integer.parseInt(split_ids[a]));
                    List<StoreCategory> categoryList = categoryMapper.select(storeCategory);
                    if(categoryList!=null&&categoryList.size()>0){
                        categoryList.forEach(category0 -> {
                            categoryMapper.delete(category0);
                        });
                    }
                    categoryMapper.deleteByPrimaryKey(Integer.parseInt(split_ids[a]));
                }
            }
            return QzhResult.ok("SUCCESS");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return QzhResult.error(e.getMessage());
        }

    }


    /**
     * 上传分类的图片
     * @param file
     * @param memberId
     * @return
     */
    @Override
    public QzhResult insertStoreCategoryPic(MultipartFile file, Integer memberId) {
        try {
            String path = "/store/store_"+memberId+"/categoryPic";
            return fileFeignClient.handFileUpload(file, path);

        } catch (RestClientException e) {
            e.printStackTrace();
            return QzhResult.error(e.getMessage());
        }
    }

    @Override
    public List<StoreCategory> getNavigationCategoryInfo(Integer memberId) {
        //根据memberId得到storeId
        Integer storeId=decorationRankingService.getStoreId(memberId);
        Example example=new Example(StoreCategory.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("storeId",storeId);
        criteria.andEqualTo("level",1);
        example.orderBy("sort").desc();
        List<StoreCategory> storeCategoryList=categoryMapper.selectByExample(example);
        return storeCategoryList;
    }

    @Override
    public Integer addNavigationCategoryInfo(String stringIds) {
        Integer num=0;
        if(org.apache.commons.lang.StringUtils.isNotBlank(stringIds)){
            if(stringIds.contains(",")){
                String[] strs=stringIds.split(",");
                for(String s:strs){
                    Integer id=Integer.parseInt(s);
                    StoreCategory storeCategory=new StoreCategory();
                    storeCategory.setId(id);
                    storeCategory= categoryMapper.selectByPrimaryKey(storeCategory);
                    if(storeCategory.getIsStoreNavigation()!=1){
                        storeCategory.setIsStoreNavigation(1);
                    }
                    categoryMapper.updateByPrimaryKey(storeCategory);
                    num++;
                }
            }else{
                Integer id=Integer.parseInt(stringIds);
                StoreCategory storeCategory=new StoreCategory();
                storeCategory.setId(id);
                storeCategory= categoryMapper.selectByPrimaryKey(storeCategory);
                if(storeCategory.getIsStoreNavigation()!=1){
                    storeCategory.setIsStoreNavigation(1);
                }
                categoryMapper.updateByPrimaryKey(storeCategory);
                num++;
            }
        }
        return num;
    }

    @Override
    public QzhResult reSortNavigationInfo(String stringIds) {
        if(org.apache.commons.lang.StringUtils.isNotBlank(stringIds)){
            if(stringIds.contains(",")){
                String[] strs=stringIds.split(",");
                int nums=strs.length;
                    for(String s:strs){
                        Integer id=Integer.parseInt(s);
                        StoreCategory storeCategory=new StoreCategory();
                        storeCategory.setId(id);
                        storeCategory=categoryMapper.selectByPrimaryKey(storeCategory);
                        storeCategory.setSort(nums);
                        categoryMapper.updateByPrimaryKey(storeCategory);
                        nums=nums-1;
                    }
            }
        }
        return QzhResult.ok("success");
    }
}
