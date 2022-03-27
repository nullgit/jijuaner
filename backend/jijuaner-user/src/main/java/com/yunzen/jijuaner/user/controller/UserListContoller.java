package com.yunzen.jijuaner.user.controller;

import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

import com.yunzen.jijuaner.common.to.UserInfoTo;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.common.utils.SignInUtils;
import com.yunzen.jijuaner.user.config.UserUtils;
import com.yunzen.jijuaner.user.entity.UserListEntity;
import com.yunzen.jijuaner.user.service.UserListService;
import com.yunzen.jijuaner.user.vo.UserInfoVo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 注册
     */
    @PostMapping("/login")
    public R login(@RequestBody UserListEntity userListEntity, @RequestParam("code") String code) {
        userListService.login(userListEntity, code);
        return R.ok().putMsg("账号注册成功");
    }

    /**
     * 登录
     */
    @PostMapping("/signIn")
    public R signIn(@RequestBody UserListEntity userListEntity, HttpSession session) {
        userListEntity = userListService.signIn(userListEntity.getEmail(), userListEntity.getPassword());
        UserInfoTo to = new UserInfoTo();
        BeanUtils.copyProperties(userListEntity, to);
        // 向 session 中保存登录信息
        session.setAttribute("userListTo", to);
        return R.ok().putMsg("登录成功");
    }

    /**
     * 退出登录
     */
    @GetMapping("/quit")
    public R quit(HttpSession session) {
        // 删除原有的登录信息
        session.removeAttribute("userListTo");
        return R.ok().putMsg("退出登录成功");
    }

    /**
     * 获取已登录的用户(自己)的信息(用户id, 用户名, 头像, email)
     *
     * @return R 中的 data 对象: {@link UserInfoVo}
     */
    @GetMapping("/getLoggedUserInfo")
    public R getLoggedUserInfo() {
        UserInfoVo vo = UserUtils.userListEntityToVo(userListService.getById(SignInUtils.getUserId()));
        return R.ok().putData(vo);
    }

    /**
     * 批量获取其他用户信息(用户id, 用户名, 头像, email)
     *
     * @return R 中的 data 对象: List<{@link UserInfoVo}>
     */
    @PostMapping("/getUserInfos")
    public R getUserInfos(@RequestBody List<Integer> ids) {
        List<UserInfoVo> data = ids.stream().parallel()
                .map(id -> UserUtils.userListEntityToVo(userListService.getUserInfo(id))).toList();
        return R.ok().putData(data);
    }

    /**
     * 向 email 发送验证码
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("email") String email) throws MessagingException {
        userListService.sendCode(email);
        return R.ok().putMsg("验证码已发送");
    }

    /**
     * 获取云存储服务的 policy
     */
    @GetMapping("/getOssPolicy")
    public R getOssPolicy() {
        Map<String, String> ossPolicy = userListService.getOssPolicy();
        return R.ok().putData(ossPolicy);
    }

    /**
     * 用户设置头像链接
     */
    @GetMapping("/setHeadImg")
    public R setHeadImg(@RequestParam("headImg") String headImg) {
        userListService.setHeadImg(SignInUtils.getUserId(), headImg);
        return R.ok().putMsg("设置头像链接成功");
    }

    /**
     * 用户重命名
     */
    @GetMapping("/rename")
    public R rename(@RequestParam("name") String name) {
        userListService.rename(SignInUtils.getUserId(), name);
        return R.ok().putMsg("重命名成功");
    }
}
