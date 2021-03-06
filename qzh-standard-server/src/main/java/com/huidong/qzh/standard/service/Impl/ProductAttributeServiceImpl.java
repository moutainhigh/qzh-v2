package com.huidong.qzh.standard.service.Impl;

import com.huidong.qzh.standard.entity.ProductAttribute;
import com.huidong.qzh.standard.entity.ProductAttributeOption;
import com.huidong.qzh.standard.entity.ProductAttributeOptionRelation;
import com.huidong.qzh.standard.entity.ProductCategoryAttribute;
import com.huidong.qzh.standard.mapper.ProductAttributeMapper;
import com.huidong.qzh.standard.mapper.ProductAttributeOptionMapper;
import com.huidong.qzh.standard.mapper.ProductAttributeOptionRelationMapper;
import com.huidong.qzh.standard.mapper.ProductCategoryAttributeMapper;
import com.huidong.qzh.standard.service.ProductAttributeService;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductAttributeServiceImpl implements ProductAttributeService{

    @Autowired
    private ProductAttributeOptionRelationMapper productAttributeOptionRelationMapper;

    @Autowired
    private ProductAttributeMapper productAttributeMapper;
    @Autowired
    private ProductCategoryAttributeMapper productCategoryAttributeMapper;
    @Autowired
    private ProductAttributeOptionMapper productAttributeOptionMapper;



    /**
     * 通过产品ID获取产品属性
     * @param goodsId
     * @return
     */
    @Override
    public List listProductAttribute(Integer goodsId) {
        Example example = new Example(ProductAttributeOptionRelation.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("goodsId",goodsId);
        return productAttributeOptionRelationMapper.selectByExample(example);
    }

    /**
     * 通过分类ID 获取品牌的属性
     * @param categoryId
     * @return
     */
    @Override
    public List listBrandAttribute(Integer categoryId) {
        ProductAttribute attribute = new ProductAttribute();
        attribute.setAttributeName("品牌");
        List<ProductAttribute> attributes = productAttributeMapper.select(attribute);

        ProductCategoryAttribute categoryAttribute = new ProductCategoryAttribute();
        categoryAttribute.setCategoryId(categoryId);

        ProductCategoryAttribute categoryAttribute1 = null;
        for(ProductAttribute proAttribute : attributes){
            categoryAttribute.setAttributeId(proAttribute.getId());
            categoryAttribute1 = productCategoryAttributeMapper.selectOne(categoryAttribute);
            if(null!=categoryAttribute1){
                break;
            }
        }
        ProductAttributeOption attributeOption = new ProductAttributeOption();
        List<ProductAttributeOption> attributeOptions =null;
        if(null!=categoryAttribute1){
            attributeOption.setAttributeId(categoryAttribute1.getAttributeId());
            attributeOptions = productAttributeOptionMapper.select(attributeOption);
        }



        return attributeOptions;
    }

    /**
     * 获取这个分类下的所有属性 及 属性选项
     * @param categoryId
     * @return
     */
    @Override
    public List listAttributeOption(Integer categoryId) {
        ProductCategoryAttribute categoryAttribute = new ProductCategoryAttribute();
        categoryAttribute.setCategoryId(categoryId);
        List<ProductCategoryAttribute> categoryAttributeList = productCategoryAttributeMapper.select(categoryAttribute);

        List<Map<Object,Object>> listMap = new ArrayList<Map<Object,Object>>();
        categoryAttributeList.forEach(productCategoryAttribute -> {
            ProductAttribute attribute = productAttributeMapper.selectByPrimaryKey(productCategoryAttribute.getAttributeId());
            BeanMap beanMap = new BeanMap(attribute);
            Map<Object,Object> map = new HashMap<Object,Object>();
            map.putAll(beanMap);
            map.remove("class");

            ProductAttributeOption attributeOption = new ProductAttributeOption();
            attributeOption.setAttributeId(attribute.getId());
            List<ProductAttributeOption> attributeOptionList = productAttributeOptionMapper.select(attributeOption);
            map.put("options",attributeOptionList);

            listMap.add(map);
        });

        return listMap;
    }

}
