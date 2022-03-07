package com.yunzen.jijuaner.user.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.interceptor.UserInterceptor;
import com.yunzen.jijuaner.common.to.UserInfoTo;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.user.entity.UserListEntity;
import com.yunzen.jijuaner.user.exception.LoginException;
import com.yunzen.jijuaner.user.exception.SignInException;
import com.yunzen.jijuaner.user.service.UserListService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
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
            UserInfoTo to = new UserInfoTo();
            userListEntity = userListService.signIn(userListEntity.getEmail(), userListEntity.getPassword());
            BeanUtils.copyProperties(userListEntity, to);
            session.setAttribute("userListTo", to);
        } catch (SignInException e) {
            return R.error().putCode(JiJuanerException.SIGN_IN_EXCEPTION.getCode()).putMsg(e.getMessage());
        }
        return R.ok().putMsg("登录成功");
    }

    @RequestMapping("/quit")
    public R quit(HttpSession session) {
        // 删除原有的登录信息
        session.removeAttribute("userListTo");
        return R.ok().putMsg("退出登录成功");
    }

    @RequestMapping("/getLoggedUserInfo")
    public R getLoggedUserInfo() {
        try {
            if (UserInterceptor.toThreadLocal.get() == null) {
                throw new SignInException("该用户没有登录信息");
            }
            // TODO session 里面不应该存放 userName 等会变更的信息
            UserListEntity user = userListService.getById(UserInterceptor.toThreadLocal.get().getUserId());
            UserInfoTo to = new UserInfoTo();
            BeanUtils.copyProperties(user, to);
            return R.ok().putData(to);
        } catch (SignInException e) {
            return R.error().putCode(JiJuanerException.SIGN_IN_EXCEPTION.getCode()).putMsg(e.getMessage());
        }
    }

    @PostMapping("/getUserInfos")
    public R getUserInfos(@RequestBody List<Integer> ids) {
        if (ids.isEmpty()) {
            return R.ok().putData(new ArrayList<String>());
        }
        List<UserListEntity> data = ids.stream().parallel().map(id -> userListService.getUserInfo(id)).toList();
        return R.ok().putData(data);
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

    private static final String EMAILREGEX = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";

    private boolean testEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.matches(EMAILREGEX);
    }

    @RequestMapping("/getOssPolicy")
    public R getOssPolicy() {
        Map<String, String> ossPolicy = userListService.getOssPolicy();
        return R.ok().putData(ossPolicy);
    }

    @RequestMapping("/setHeadImg")
    public R setHeadImg(@RequestParam("headImg") String headImg) {
        userListService.setHeadImg(UserInterceptor.toThreadLocal.get().getUserId(), headImg);
        return R.ok().putData(headImg);
    }

    @RequestMapping("/rename")
    public R rename(@RequestParam("name") String name) {
        name = name.trim();
        if (!StringUtils.hasText(name)) {
            return R.error().putMsg("名字中没有有效字符");
        } else if (name.length() > 32) {
            return R.error().putMsg("名字长度不能超过32个字符");
        }
        userListService.rename(UserInterceptor.toThreadLocal.get().getUserId(), name);
        return R.ok().putMsg("重命名成功");
    }
}
