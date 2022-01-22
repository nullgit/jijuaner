package com.yunzen.jijuaner.user.service;

import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.mail.util.MailSSLSocketFactory;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.user.dao.UserListDao;
import com.yunzen.jijuaner.user.entity.UserListEntity;
import com.yunzen.jijuaner.user.exception.LoginException;
import com.yunzen.jijuaner.user.exception.SignInException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service("userListService")
@Slf4j
public class UserListService extends ServiceImpl<UserListDao, UserListEntity> {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Transactional
    public void login(UserListEntity userListEntity, String code) throws LoginException {
        String key = JiJuanerConstantString.VERIFICATION_CODE.getConstant() + userListEntity.getEmail();
        String realCode = stringRedisTemplate.opsForValue().get(key);
        // 验证验证码
        if (realCode != null && realCode.equals(code)) {
            stringRedisTemplate.delete(key);
        } else {
            throw new LoginException("验证码不正确或已过期！");
        }

        Integer emailCount = baseMapper
                .selectCount(new QueryWrapper<UserListEntity>().eq("email", userListEntity.getEmail()));
        if (emailCount != 0) {
            throw new LoginException("该邮箱已注册过账号！");
        }

        String encode = bCryptPasswordEncoder.encode(userListEntity.getPassword());
        userListEntity.setPassword(encode);
        baseMapper.insert(userListEntity);
    }

    public UserListEntity signIn(String email, String password) throws SignInException {
        UserListEntity record = baseMapper
                .selectOne(new QueryWrapper<UserListEntity>().eq("email", email));
        if (record == null) {
            throw new SignInException("邮箱账号不存在，请注册！");
        }

        if (!bCryptPasswordEncoder.matches(password, record.getPassword())) {
            throw new SignInException("密码错误！");
        }
        return record;
    }

    private Properties emailProperties;

    public UserListService() throws GeneralSecurityException {
        emailProperties = new Properties();
        // TODO 开启debug调试，以便在控制台查看
        emailProperties.setProperty("mail.debug", "true");
        // 设置邮件服务器主机名
        emailProperties.setProperty("mail.host", "smtp.qq.com");
        // 发送服务器需要身份验证
        emailProperties.setProperty("mail.smtp.auth", "true");
        // 发送邮件协议名称
        emailProperties.setProperty("mail.transport.protocol", "smtp");
        // 开启SSL加密，否则会失败
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        emailProperties.put("mail.smtp.ssl.enable", "true");
        emailProperties.put("mail.smtp.ssl.socketFactory", sf);
    }

    public void sendCode(String email) throws MessagingException {
        // 创建session
        Session session = Session.getInstance(emailProperties);
        // 通过session得到transport对象
        Transport ts = session.getTransport();
        // 连接邮件服务器：邮箱类型，帐号，POP3/SMTP协议授权码 163使用：smtp.163.com
        // ts.connect("smtp.qq.com", "发送方的QQ号", "POP3/SMTP协议授权码");
        // xeyxxxxxxxxx
        ts.connect("smtp.qq.com", "2450264253", "");

        // 创建邮件对象
        MimeMessage message = new MimeMessage(session);
        // 指明邮件的发件人
        message.setFrom(new InternetAddress("2450264253@qq.com"));
        // 指明邮件的收件人，发件人和收件人如果是一样的，那就是自己给自己发
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        // 邮件的标题
        message.setSubject("鸡圈儿验证码");
        // 邮件的文本内容
        String code = getCode();
        message.setContent("【鸡圈儿】账号注册验证码为（5分钟有效）：" + code
                + "，请勿回复此邮箱", "text/html;charset=UTF-8");
        // 发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();

        // Redis保存邮件+验证码
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        opsForValue.set(JiJuanerConstantString.VERIFICATION_CODE.getConstant() + email, code, Duration.ofMinutes(5));
    }

    private String[] letters = new String[] { "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g",
            "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m", "A", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
            "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M", "0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9" };

    private Random random = new Random();

    private String getCode() {
        var sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb = sb.append(letters[random.nextInt(0, letters.length)]);
        }
        return sb.toString();
    }
}
