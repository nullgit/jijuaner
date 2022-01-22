package com.yunzen.jijuaner.user.interceptor;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yunzen.jijuaner.user.entity.UserListEntity;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class UserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserListEntity> toThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        UserListEntity userInfoSession = (UserListEntity) session.getAttribute("userListEntity");
        toThreadLocal.set(userInfoSession);
        return true;
    }

    // @Override
    // public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
    //         ModelAndView modelAndView) throws Exception {
    //     // TODO Auto-generated method stub
    //     response.addCookie(new Cookie("key", "value"));
    //     HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    // }

}
