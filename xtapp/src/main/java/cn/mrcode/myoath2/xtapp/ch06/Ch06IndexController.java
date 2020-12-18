package cn.mrcode.myoath2.xtapp.ch06;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cn.mrcode.myoath2.xtapp.util.HttpURLClient;
import cn.mrcode.myoath2.xtapp.util.URLParamsUtil;

/**
 * <pre>
 * 第三方软件，小兔 APP; 授权码许可流程
 * </pre>
 *
 * @author mrcode
 * @date 2020/12/17 11:08
 */
@Controller
@RequestMapping("/ch06")
public class Ch06IndexController {
    final Logger logger = LoggerFactory.getLogger(Ch06IndexController.class);
    // 跳转地址：小兔 APP 后端服务
    String redirectUrl = "http://localhost:8080/ch06/callback";
    // 授权服务：京东
    String oauthUrl = "http://localhost:8081/ch06/authorize";
    // 受保护的资源-订单：京东
    String protectedURl = "http://localhost:8081/ch03/order";

    /**
     * 隐式许可流程
     *
     * @return
     */
    @GetMapping("/implicit")
    public String implicit() throws UnsupportedEncodingException {
        // 根据协议规范，必要参数就这些了
        Map<String, String> params = new HashMap<String, String>();
        params.put("response_type", "token");//告诉授权服务直接返回 access_token
        params.put("redirect_uri", redirectUrl);
        params.put("app_id", "APPIDTEST");

        String toOauthUrl = URLParamsUtil.appendParams(oauthUrl, params);//构造请求授权的URl
        logger.info("重定向到授权服务页面: " + toOauthUrl);

        return "redirect:" + toOauthUrl;
    }

    /**
     * 客户端凭证许可
     *
     * @return
     */
    @GetMapping("/client_credentials")
    @ResponseBody
    public String client_credentials() throws UnsupportedEncodingException {
        // 根据协议规范，必要参数就这些了
        Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type","client_credentials");
        params.put("app_id","APPIDTEST");
        params.put("app_secret","APPSECRETTEST");

        String accessToken = HttpURLClient.doPost(oauthUrl, HttpURLClient.mapToStr(params));
        logger.info(accessToken);
        return accessToken;
    }

    /**
     * 资源拥有者授权流程
     *
     * @return
     */
    @GetMapping("/password")
    @ResponseBody
    public String password() throws UnsupportedEncodingException {
        // 根据协议规范，必要参数就这些了
        Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "password");
        params.put("app_id", "APPIDTEST");
        params.put("app_secret", "APPSECRETTEST");
        params.put("name", "NAMETEST");
        params.put("password", "PASSWORDTEST");

        String accessToken = HttpURLClient.doPost(oauthUrl, HttpURLClient.mapToStr(params));
        logger.info(accessToken);
        return accessToken;
    }


    /**
     * 2. 当用户在 授权服务页面上 给我们授权之后，会携带 code 重定向到该服务
     * <pre>
     * 小兔 APP 后端服务：接受用户授权通过之后的 code 码;
     * 在此场景下：本来是先到前端，这里为了简化，直接重定向到后端服务，完成后面的 code 获取 访问 token 的逻辑
     * </pre>
     *
     * @return
     */
    @GetMapping("/callback")
    @ResponseBody
    public String callback(String code) {
        logger.info("拿到授权服务回调的 code：" + code);

        // 拿到授权码之后，就可以去 授权服务换取 accessToken
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("app_id", "APPID_RABBIT");
        params.put("app_secret", "APPSECRET_RABBIT");
        String accessToken = HttpURLClient.doPost(oauthUrl, HttpURLClient.mapToStr(params));
        logger.info("通过 code 换取到的 accessToken：" + accessToken);

        //使用 accessToken 请求受保护资源服务
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("app_id", "APPID_RABBIT");
        paramsMap.put("app_secret", "APPSECRET_RABBIT");
        // f5f1876f-206c-4d69-b9f0-da9658999200|d945e424-827e-4280-904d-0cc808628274
        paramsMap.put("token", accessToken.split("\\|")[0]);
        String result = HttpURLClient.doPost(protectedURl, HttpURLClient.mapToStr(paramsMap));
        logger.info("通过 accessToken 访问受保护的资源,获取到订单信息" + result);
        return result;
    }
}
