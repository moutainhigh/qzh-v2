package com.qzh.personalCenter.controller;

import com.huidong.qzh.util.common.util.QzhResult;
import com.qzh.personalCenter.service.CollectProductService;
import com.qzh.personalCenter.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collectProduct")
public class CollectProductController {

    @Autowired
    private CollectProductService collectProductService;
    @Autowired
    private CommonService commonService;

    /**
     * 收藏产品
     * @param goodsId
     * @return
     */
    @RequestMapping("/insert")
    public QzhResult insertCollectProduct(HttpServletRequest request,
                                          @RequestParam(required = true) Integer goodsId){
        try {
            QzhResult land = commonService.isLand(request);
            if(land.getStatus()!=200){
                return  land;
            }
            Map<String,Object> land_map = (Map<String,Object>)land.getData();
            Integer accountId = Integer.parseInt(land_map.get("id").toString());

            collectProductService.insertCollectProduct(accountId,goodsId);
            return QzhResult.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.error(e.getMessage());
        }
    }

    /**
     * 分页获取收藏产品数据
     * @param pageNo    第几页
     * @param pageSize  一页多少条数据
     * @return
     */
    @RequestMapping("/select")
    public QzhResult listCollectProduct(HttpServletRequest request,
                                        @RequestParam(required = false,defaultValue = "1") Integer pageNo,
                                        @RequestParam(required = false,defaultValue = "1") Integer pageSize){

        try {
            QzhResult land = commonService.isLand(request);
            if(land.getStatus()!=200){
                return  land;
            }
            Map<String,Object> land_map = (Map<String,Object>)land.getData();
            Integer accountId = Integer.parseInt(land_map.get("id").toString());

            return collectProductService.listCollectProduct(accountId, pageNo, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.error(e);
        }
    }

    /**
     * ids:是收藏商品表的id，删除多个id时，以逗号隔开
     * @param ids  例如（1，2，3）
     */
    @RequestMapping("/delet")
    public QzhResult deletCollectProduct(@RequestParam(required = true) String ids){
        try {
            collectProductService.deletCollectProduct(ids);
            return QzhResult.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.error(e);
        }
    }

    /**
     * 判断是否收藏商品
     * @param goodsId
     * @return
     */
    @RequestMapping("/isCollectProduct")
    public QzhResult isCollectProduct(HttpServletRequest request,
                                    @RequestParam(required = true) Integer goodsId) {
        Integer collectProduct = 0;
        try {
            QzhResult land = commonService.isLand(request);
            if(land.getStatus()!=200){
                return  land;
            }
            Map<String,Object> land_map = (Map<String,Object>)land.getData();
            Integer accountId = Integer.parseInt(land_map.get("id").toString());

            collectProduct = collectProductService.isCollectProduct(accountId, goodsId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(collectProduct==0){
            return QzhResult.error("");
        }else{
            return QzhResult.ok(collectProduct);
        }
    }


}
