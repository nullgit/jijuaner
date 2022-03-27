package com.yunzen.jijuaner.user.config;

import java.util.List;
import java.util.Random;

import com.yunzen.jijuaner.user.entity.UserListEntity;
import com.yunzen.jijuaner.user.vo.UserInfoVo;

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * 在 user 微服务中要用到的 bean 或工具
 */
@Configuration
public class UserUtils {
    /**
     * 对用户密码加密时使用的加密器
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 验证邮箱格式是否正确的工具方法
     */
    public static boolean testEmail(String email) {
        return email != null && email.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    }

    public static <T> void swapListElement(List<T> list, int idx1, int idx2) {
        T tmp = list.get(idx1);
        list.set(idx1, list.get(idx2));
        list.set(idx2, tmp);
    }

    public static UserInfoVo userListEntityToVo(UserListEntity entity) {
        var vo = new UserInfoVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 检验自选分组的名称是否合理
     */
    public static String groupNameValidator(String groupName) {
        if (!StringUtils.hasText(groupName)) {
            return "分组名称不能为空";
        } else if (groupName.length() > 6) {
            return "分组名称字符不能超过6";
        }
        return null;
    }

    private static String[] letters = new String[] { "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d",
            "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m", "Q", "W", "E", "R", "T", "Y", "U", "I",
            "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M", "0", "1", "2",
            "3", "4", "5", "6", "7", "8", "9" };

    private static Random random = new Random();

    /**
     * 生成 6 位数字验证码
     */
    public static String getCode() {
        var sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb = sb.append(letters[random.nextInt(0, letters.length)]);
        }
        return sb.toString();
    }
}
