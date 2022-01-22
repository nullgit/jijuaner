package com.yunzen.jijuaner.user.controller;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.user.entity.UserListEntity;
import com.yunzen.jijuaner.user.exception.LoginException;
import com.yunzen.jijuaner.user.exception.SignInException;
import com.yunzen.jijuaner.user.interceptor.UserInterceptor;
import com.yunzen.jijuaner.user.service.UserListService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/userList")
public class UserListContoller {
    @Autowired
    private UserListService userListService;

    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund user!";
    }

    @RequestMapping("/login")
    public R login(@RequestBody UserListEntity userListEntity, @RequestParam("code") String code) {
        try {
            if (userListEntity.getEmail() == null) {
                throw new LoginException("邮箱账号不能为空！");
            } else if (!testEmail(userListEntity.getEmail())) {
                throw new LoginException("邮箱格式不正确！");
            } else if (userListEntity.getPassword() == null) {
                throw new LoginException("密码不能为空！");
            } else if (userListEntity.getPassword().length() < 6) {
                throw new LoginException("密码需要至少6位数！");
            } else if (code == null) {
                throw new LoginException("验证码不能为空！");
            }
            // TODO 防止验证码在60s内反复提交
            userListService.login(userListEntity, code);
        } catch (LoginException e) {
            return R.error().putCode(JiJuanerException.LOGIN_EXCEPTION.getCode()).putMsg(e.getMessage());
        }
        return R.ok().putMsg("账号注册成功！");
    }

    @RequestMapping("/signIn")
    public R signIn(@RequestBody UserListEntity userListEntity, HttpSession session) {
        try {
            if (userListEntity == null) {
                throw new SignInException("用户账号密码不能为空！");
            } else if (userListEntity.getEmail() == null) {
                throw new SignInException("邮箱账号不能为空！");
            } else if (userListEntity.getPassword() == null) {
                throw new SignInException("密码不能为空！");
            }
            userListEntity = userListService.signIn(userListEntity.getEmail(), userListEntity.getPassword());
            userListEntity.setPassword("");
            session.setAttribute("userListEntity", userListEntity);
        } catch (SignInException e) {
            return R.error().putCode(JiJuanerException.SIGN_IN_EXCEPTION.getCode()).putMsg(e.getMessage());
        }
        return R.ok().putMsg("登录成功");
    }

    @RequestMapping("/quit")
    public R quit(HttpSession session) {
        // 删除原有的登录信息
        session.removeAttribute("userListEntity");
        return R.ok().putMsg("退出登录成功");
    }

    @RequestMapping("/getLoggedUserInfo")
    public R getLoggedUserInfo() {
        try {
            if (UserInterceptor.toThreadLocal.get() == null) {
                throw new SignInException("该用户没有登录信息");
            }
        } catch (SignInException e) {
            return R.error().putCode(JiJuanerException.SIGN_IN_EXCEPTION.getCode()).putMsg(e.getMessage());
        }
        return R.ok().putData(UserInterceptor.toThreadLocal.get());
    }

    @RequestMapping("/sendCode")
    public R sendCode(@RequestParam("email") String email) throws MessagingException {
        try {
            if (!testEmail(email)) {
                throw new LoginException("邮箱格式不正确");
            }
            userListService.sendCode(email);
        } catch (LoginException e) {
            return R.error().putCode(JiJuanerException.LOGIN_EXCEPTION.getCode()).putMsg(e.getMessage());
        }
        return R.ok().putMsg("验证码已发送");
    }

    private String emailRegex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";

    private boolean testEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.matches(emailRegex);
    }
}
