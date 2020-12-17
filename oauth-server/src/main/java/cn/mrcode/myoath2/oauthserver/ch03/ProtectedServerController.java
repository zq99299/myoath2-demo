package cn.mrcode.myoath2.oauthserver.ch03;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <pre>
 *  受保护的资源
 * </pre>
 *
 * @author mrcode
 * @date 2020/12/17 15:06
 */
@Controller
@RequestMapping("/ch03")
public class ProtectedServerController {
    /**
     * 受保护的资源：订单信息
     *
     * @return
     */
    @PostMapping("/order")
    @ResponseBody
    public String order(String token) {
        //根据当时授权的 token 对应的权限范围，做相应的处理动作
        //不同权限对应不同的操作
        // 比如：当前这个接口是获取订单信息，那么校验授权的 scope 里面是否包含这个权限, 如果包含就运行访问
        final String[] scope = Ch03OathServerController.tokenScopeMap.get(token);
        StringBuffer sbuf = new StringBuffer();
        for (int i = 0; i < scope.length; i++) {
            sbuf.append(scope[i]).append("|");
        }

        if (sbuf.toString().indexOf("query") > 0) {
            queryGoods("");
        }

        if (sbuf.toString().indexOf("add") > 0) {
            addGoods("");
        }

        if (sbuf.toString().indexOf("del") > 0) {
            delGoods("");
        }
        return queryGoods("");
    }

    private String queryGoods(String id) {
        return "{'id':1,'name':'订单1'}";
    }

    private String addGoods(String goods) {
        return "添加订单成功";
    }

    private String delGoods(String id) {
        return "删除订单成功";
    }
}
