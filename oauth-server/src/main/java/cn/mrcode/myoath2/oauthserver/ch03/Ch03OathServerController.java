package cn.mrcode.myoath2.oauthserver.ch03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import cn.mrcode.myoath2.oauthserver.util.URLParamsUtil;


/**
 * <pre>
 * 授权服务：授权码许可流程
 * </pre>
 *
 * @author mrcode
 * @date 2020/12/17 11:08
 */
@Controller
@RequestMapping("/ch03")
public class Ch03OathServerController {
    final Logger logger = LoggerFactory.getLogger(Ch03OathServerController.class);
    //模拟授权码、令牌等数据存储

    /**
     * 存放生成的授权码
     * <pre>
     *  k= code,
     *  v= appid 等信息，方便校验
     * </pre>
     */
    static Map<String, String> codeMap = new HashMap<>();
    /**
     * code 与用户选择的授权范围进行绑定
     * <pre>
     *  k= code,
     *  v= 用户确认授权时选择的 scope
     * </pre>
     */
    static Map<String, String[]> codeScopeMap = new HashMap<>();


    /**
     * accessToken 与 app 的相关信息，便于后续验证
     * <pre>
     *  k= accessToken,
     *  v= apid 相关信息
     * </pre>
     */
    static Map<String, String> tokenMap = new HashMap<>();
    /**
     * accessToken 与用户选择的授权范围进行绑定
     * <pre>
     *  k= accessToken,
     *  v= 用户确认授权时选择的 scope
     * </pre>
     */
    static Map<String, String[]> tokenScopeMap = new HashMap<>();

    /**
     * refresh_token 与 appid 相关信息，便于后续验证
     * <pre>
     *  k= refresh_token,
     *  v= 用 appid 等信息
     * </pre>
     */
    static Map<String, String> refreshTokenMap = new HashMap<>();

    /**
     * 第三方软件在 授权服务注册的信息
     */
    static Map<String, String> appMap = new HashMap<>();

    /**
     * 页面请求的 ID 信息；用于过滤非法提交
     * <pre>
     *  k= reqid,
     *  v= reqid
     * </pre>
     */
    static Map<String, String> reqidMap = new HashMap<>();

    static {
        //模拟第三方软件注册之后的数据库存储
        appMap.put("app_id", "APPID_RABBIT");
        appMap.put("app_secret", "APPSECRET_RABBIT");
        appMap.put("redirect_uri", "http://localhost:8080/ch03/callback");
        appMap.put("scope", "today history");

    }


    /**
     * 1. 生成授权页面
     * <pre>
     *     访问本地址：http://localhost:8081/ch03/oath
     * </pre>
     *
     * @return
     */
    @GetMapping("/oath")
    public String index(Model model,
                        @RequestParam("response_type") String responseType,
                        @RequestParam("redirect_uri") String redirectUri,
                        @RequestParam("app_id") String appId,
                        @RequestParam("scope") String scope
    ) {
        logger.info("response_type", responseType);
        if (!appMap.get("app_id").equals(appId)) {
            throw new RuntimeException("没有该 app_id");
        }
        if (!appMap.get("redirect_uri").equals(redirectUri)) {
            throw new RuntimeException("没有该 redirect_uri");
        }
        //验证第三方软件请求的权限范围是否与当时注册的权限范围一致
        if (!checkScope(scope)) {
            //超出注册的权限范围
            throw new RuntimeException("没有该 redirect_uri");
        }
        //生成页面reqid
        String reqid = String.valueOf(System.currentTimeMillis());
        reqidMap.put(reqid, reqid);// 保存该 reqid 值

        model.addAttribute("reqid", reqid);
        model.addAttribute("response_type", responseType);
        model.addAttribute("redirect_uri", redirectUri);
        model.addAttribute("app_id", appId);
        return "approve.html";
    }

    /**
     * 2. 处理用户的授权：发放授权码
     *
     * @return
     */
    @PostMapping("/oath-confirm")
    public String oath(
            HttpServletRequest request,
            @RequestParam(value = "app_id") String appId,
            @RequestParam(value = "redirect_uri") String redirectUri,
            @RequestParam(value = "response_type") String responseType,
            // 前面都是第三方客户端提交的信息，下面是用户操作的信息
            @RequestParam("reqType") String reqType, // 操作类型：approve：用户同意授权
            @RequestParam("rscope") String[] rscope, // 用户选择的 scope 项
            @RequestParam("reqid") String reqid // 页面 ID

    ) {
        if (!reqidMap.containsKey(reqid)) {
            throw new RuntimeException("reqid 不匹配");
        }
        if (!"approve".equals(reqType)) {
            throw new RuntimeException("用户拒绝授权");
        }

        if (!"code".equals(responseType)) {
            throw new RuntimeException("不支持此类型的授权类型");
        }

        if (!checkScope(rscope)) {//验证权限范围，对又验证一次
            throw new RuntimeException("超出注册的权限范围");
        }

        // 模拟登陆用户为 USERTEST，出现这一步的前提是，用户要在授权服务上是登录状态
        String code = generateCode(appId, "USERTEST");
        // 将 code 与用户选择的授权范围进行绑定
        codeScopeMap.put(code, rscope);
        Map<String, String> params = new HashMap<String, String>();
        params.put("code", code);
        //构造第三方软件的回调地址，并重定向到该地址
        String toAppUrl = URLParamsUtil.appendParams(redirectUri, params);
        return "redirect:" + toAppUrl;
    }

    /**
     * 3. 发放 accessToken： 用 code 换取 accessToken
     *
     * @return
     */
    @PostMapping("/oath")
    @ResponseBody
    public String oath(
            HttpServletRequest request,
            @RequestParam(value = "app_id") String appId,
            @RequestParam(value = "app_secret") String appSecret,
            @RequestParam(value = "grant_type") String grantType,
            @RequestParam(value = "code") String code

    ) {
        //处理授权码流程中的 颁发访问令牌 环节
        if (!"authorization_code".equals(grantType)) {
            throw new RuntimeException("不支持此授权类型");
        }
        if (!appMap.get("app_id").equals(appId)) {
            throw new RuntimeException("app_id 是无效的");
        }
        if (!appMap.get("app_secret").equals(appSecret)) {
            throw new RuntimeException("app_secret 是无效的");
        }
        if (!isExistCode(code)) {//验证 code 值
            throw new RuntimeException("code 是无效的");
        }

        codeMap.remove(code);// 授权码一旦被使用，须要立即作废
        String accessToken = generateAccessToken(appId, "USERTEST");// 生成访问令牌access_token的值
        tokenScopeMap.put(accessToken, codeScopeMap.get(code));// 授权范围与访问令牌绑定
        String refreshToken = generateRefreshToken(appId, "USERTEST");// 生成刷新令牌refresh_token的值

        // TODO: 2020/2/28 将 accessToken 和 refreshToken 做绑定 ，将 refreshToken 和 codeScopeMap 做绑定
        // 可以看到这里的实现是比较简陋的，在使用 refreshToken 时，是不是还需要获取到原始授权的相关 scope 等信息
        return accessToken + "|" + refreshToken;
    }


    /**
     * 验证权限
     *
     * @param scope
     * @return
     */
    private boolean checkScope(String scope) {
        logger.info("appMap size: " + appMap.size());
        logger.info("appMap scope: " + appMap.get("scope"));
        logger.info("scope: " + scope);
        return appMap.get("scope").contains(scope);// 简单模拟权限验证
    }

    /**
     * 检查 scope
     *
     * @param rscope
     * @return
     */
    private boolean checkScope(String[] rscope) {
        String scope = "";

        for (int i = 0; i < rscope.length; i++) {
            scope = scope + rscope[i];
        }

        return appMap.get("scope").replace(" ", "").contains(scope);// 简单模拟权限验证
    }


    /**
     * 生成code值
     *
     * @return
     */
    private String generateCode(String appId, String user) {
        Random r = new Random();
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            strb.append(r.nextInt(10));
        }

        String code = strb.toString();

        // 在这一篇章我们仅作为演示用，实际这应该是一个全局内存数据库，有效期官方建议是10分钟
        codeMap.put(code, appId + "|" + user + "|" + System.currentTimeMillis());

        return code;
    }

    /**
     * 是否存在code值
     *
     * @param code
     * @return
     */
    private boolean isExistCode(String code) {
        return codeMap.containsKey(code);
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
        //在这一篇章我们仅作为演示用，实际这应该是一个全局数据库,并且有有效期
        tokenMap.put(accessToken, appId + "|" + user + "|" + System.currentTimeMillis() + "|" + expires_in);
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
        //在这一篇章我们仅作为演示用，实际这应该是一个全局数据库,并且有有效期
        refreshTokenMap.put(refreshToken, appId + "|" + user + "|" + System.currentTimeMillis());
        return refreshToken;
    }
}
