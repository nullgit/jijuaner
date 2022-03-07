package com.yunzen.jijuaner.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yunzen.jijuaner.common.to.UserInfoTo;

import org.springframework.web.servlet.HandlerInterceptor;

public class UserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> toThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        UserInfoTo userInfoSession = (UserInfoTo) session.getAttribute("userListTo");
        toThreadLocal.set(userInfoSession);
        return true;
    }

}
