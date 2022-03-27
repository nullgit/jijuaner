package com.yunzen.jijuaner.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yunzen.jijuaner.common.to.UserInfoTo;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用来拦截并获取用户的 session, 使用前需要在拦截器组中注册
 */
public class UserInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<UserInfoTo> toThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        UserInfoTo userInfoSession = (UserInfoTo) session.getAttribute("userListTo");
        toThreadLocal.set(userInfoSession);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        toThreadLocal.remove();
    }
}
