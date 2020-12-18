package cn.mrcode.myoath2.oauthserver.ch06;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;


/**
 * <pre>
 * 授权服务：授权码许可流程； 只模拟一个流程，不对各种协议逻辑做严谨的实现
 * </pre>
 *
 * @author mrcode
 * @date 2020/12/17 11:08
 */
@Controller
@RequestMapping("/ch06")
public class Ch06OathServerController {
    final Logger logger = LoggerFactory.getLogger(Ch06OathServerController.class);

    /**
     * 第三方软件在 授权服务注册的信息
     */
    static Map<String, String> appMap = new HashMap<>();

    static {
        //模拟第三方软件注册之后的数据库存储
        appMap.put("app_id", "APPID_RABBIT");
        appMap.put("app_secret", "APPSECRET_RABBIT");
        appMap.put("redirect_uri", "http://localhost:8080/ch03/callback");
        appMap.put("scope", "today history");
    }


    /**
     * 处理所有的令牌颁发：授权码除外
     *
     * @return
     */
    @RequestMapping("/authorize")
    @GetMapping("/authorize")
    @ResponseBody
    public String oath(
            HttpServletRequest request,
            @RequestParam(value = "app_id") String appId,
            @RequestParam(value = "app_secret", required = false) String appSecret,
            @RequestParam(value = "grant_type", required = false) String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "response_type", required = false) String responseType,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password

    ) {
        // implicit 隐式处授权许可
        if (responseType != null && "token".equals(responseType)) {
            return doImplicit(appId, redirectUri, scope);
        }

        switch (grantType) {
            case "client_credentials":
                return doClientCredentials(appId, appSecret, scope);
            case "password":
                return doPassword(appId, appSecret, username, password);
        }
        return "";
    }

    private String doPassword(String appId, String appSecret, String username, String password) {
        return generateAccessToken(appId, "1") + "|" + generateRefreshToken(appId, "1");
    }

    /**
     * 客户端凭证授权许可：https://tools.ietf.org/html/rfc6749#section-4.4
     *
     * @param appId
     * @param appSecret
     * @param scope
     * @return
     */
    private String doClientCredentials(String appId, String appSecret, String scope) {
        return generateAccessToken(appId, "1");
    }

    /**
     * 隐式许可流程: https://tools.ietf.org/html/rfc6749#section-4.2
     *
     * @param appId
     * @param redirectUri
     * @param scope
     * @return
     */
    private String doImplicit(String appId, String redirectUri, String scope) {
        return generateAccessToken(appId, "1");
    }


    /**
     * 生成access_token值
     *
     * @param appId
     * @param user
     * @return
     */
    private String generateAccessToken(String appId, String user) {
        String accessToken = UUID.randomUUID().toString();
        String expires_in = "1";//1天时间过期
        return accessToken;
    }

    /**
     * 生成 refresh_token 值
     *
     * @param appId
     * @param user
     * @return
     */
    private String generateRefreshToken(String appId, String user) {
        String refreshToken = UUID.randomUUID().toString();
        return refreshToken;
    }
}
