package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;


//拦截器
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    //在进入控制器之前执行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
        //获取token
        String token = request.getParameter("newToken");
        if (token != null){
            //将token存入cookie中
            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        }

        if (token == null){
            //将存在cookie中的数据取出来
            token =CookieUtil.getCookieValue(request, "token", false);
        }

        if (token != null){
            //解密token数据
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName",nickName);
        }

        //判断是否有注解
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(methodAnnotation!=null){
            //去认证
            String salt = request.getHeader("X-forwarded-for");
            //http://passport.atguigu.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ&salt=192.168.67.1
            String result  = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);

            if("success".equals(result)){
                //验证成功e
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                request.setAttribute("userId", userId);
                return true;
            }else {
                if (methodAnnotation.autoRedirect()){
                    String  requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    //http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F35.html
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;
                }
            }
        }

        return true;
    }

    private Map getUserMapByToken(String token) {
        //tokenn=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        //创建对象解密token
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;
    }

    //进入控制器之后，视图之前
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    //视图渲染之后执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
