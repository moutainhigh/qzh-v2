package com.huidong.qzh.sso.controller;

import com.huidong.qzh.sso.entity.EipAccounts;
import com.huidong.qzh.sso.service.EipAccountService;
import com.huidong.qzh.util.common.util.QzhResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EipAccountController {

    @Autowired
    private EipAccountService accountService;


    /**
     * 用户注册
     *
     * @return
     */
    @RequestMapping("/register")
    public QzhResult register(String phone, String authCode, String password, String companyName, String nickName, HttpServletRequest request) {
        return accountService.register(phone, authCode, password, companyName, nickName, request);
    }


    /**
     * 登录
     *
     * @param username
     * @param password
     * @param request
     * @param response
     * @param returnUrl
     * @return
     */
    @PostMapping("/login")
    public QzhResult userLogin(String username, String password, String authCode,
                               HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) String returnUrl) {
        try {
            QzhResult qzhResult = accountService.userLogin(username, password, authCode, request, response);
            if (qzhResult.getStatus() == 200) {
                Map<String, Object> result = new HashMap<>();
                result.put("returnUrl", returnUrl);
                result.put("token", qzhResult.getData());
                qzhResult.setData(result);
            }
            return qzhResult;
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.build(500, e.getMessage());
        }
    }

    /**
     * 校验token
     *
     * @param token
     * @param callback
     * @return
     */
    @RequestMapping("/token/{token}")
    public QzhResult getUserByToken(@PathVariable String token, @RequestParam(required = false) String callback) {
        try {
            QzhResult userByToken = accountService.getUserByToken(token);
            System.out.println(userByToken);
            return accountService.getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.error(e.getMessage());
        }
//        //判断是否为jsonp调用
//        if(StringUtils.isBlank(callback)){
//            return result;
//        }else{
//            MappingJacksonValue mappingJacksonValue=new MappingJacksonValue(result);
//            mappingJacksonValue.setJsonpFunction(callback);
//            return mappingJacksonValue;
//        }
    }

    /**
     * 根据token 获取 user 对象
     * @param token token字符串
     * @return
     */
    @RequestMapping("/getUserObject/{token}")
    public EipAccounts getUserObjectByToken(@PathVariable String token) {
        try {
            return accountService.getUserObjectByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 校验token
     *
     * @param token
     * @return
     */
    @PostMapping("/getToken")
    public QzhResult getUserToken(@RequestParam(required = true) String token) {
        try {
            return accountService.getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return QzhResult.error(e.getMessage());
        }
    }

    /**
     * 注销登录
     *
     * @param token
     * @return
     */
    @PostMapping("/logout/{token}")
    public QzhResult logout(@PathVariable String token) {
        return accountService.logout(token);
    }

    /**
     * 修改密码,验证码验证
     *
     * @param phone
     * @param authCode
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/verifyMobileCode")
    public QzhResult verifyMobileCode(String phone, String authCode, HttpServletRequest request) throws Exception {
        return accountService.verifyMobileCode(phone, authCode, request);
    }

    /**
     * 修改密码
     *
     * @param phone
     * @param password
     * @param request
     * @return
     */
    @RequestMapping("/editUserPass")
    public QzhResult editUserPass(String phone, String password, HttpServletRequest request) {
        return accountService.editUserPass(phone, password, request);
    }

    /**
     * 强制删除用户 ,测试用,禁止正式使用
     *
     * @param phone
     * @return
     */
    @RequestMapping("/delUser/{phone}")
    public QzhResult delUser(String phone) {
        Boolean isDel = accountService.delUser(phone);
        if (isDel) {
            return QzhResult.ok("删除成功!");
        }
        return QzhResult.error("删除失败!");
    }
}
