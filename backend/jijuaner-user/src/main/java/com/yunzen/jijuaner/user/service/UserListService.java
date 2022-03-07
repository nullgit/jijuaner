package com.yunzen.jijuaner.user.service;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.mail.util.MailSSLSocketFactory;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.user.dao.UserListDao;
import com.yunzen.jijuaner.user.entity.UserListEntity;
import com.yunzen.jijuaner.user.exception.LoginException;
import com.yunzen.jijuaner.user.exception.SignInException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userListService")
@RefreshScope
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

    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String HEAD_IMG = "head_img";

    @Cacheable(cacheNames = "jijuaner:userList")
    public UserListEntity getUserInfo(Integer id) {
        return baseMapper
                .selectOne(new QueryWrapper<UserListEntity>().eq(USER_ID, id).select(USER_ID, USER_NAME, HEAD_IMG));
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

    @Resource
    private OSSClient ossClient;
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;
    @Value("${spring.cloud.alicloud.oss.bucket-name}")
    private String bucket;
    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;

    public Map<String, String> getOssPolicy() {
        String host = "https://" + bucket + "." + endpoint; // host 的格式为 bucketname.endpoint
        // callbackUrl 为上传回调服务器的 URL，请将下面的 IP 和 Port 配置为您自己的真实信息。
        // String callbackUrl = "http://88.88.88.88:8888";

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = "jijuaner/headImg/" + date + "/"; // 用户上传文件时指定的前缀。

        // 创建OSSClient实例。
        var respMap = new LinkedHashMap<String, String>();
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject 请求最大可支持的文件大小为 5 GB，即 CONTENT_LENGTH_RANGE 为 5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // JSONObject jasonCallback = new JSONObject();
            // jasonCallback.put("callbackUrl", callbackUrl);
            // jasonCallback.put("callbackBody",
            // "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
            // jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
            // String base64CallbackBody =
            // BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
            // respMap.put("callback", base64CallbackBody);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return respMap;
    }

    public void setHeadImg(Integer userId, String url) {
        UserListEntity entity = baseMapper
                .selectOne(new QueryWrapper<UserListEntity>().select(USER_ID, HEAD_IMG).eq(USER_ID, userId));
        String oldImg = entity.getHeadImg();
        entity.setHeadImg(url);
        baseMapper.updateById(entity);
        if (oldImg != null) {
            ossClient.deleteObject(bucket, oldImg.replace("https://" + bucket + "." + endpoint + "/", ""));
        }
    }

    public void rename(Integer userId, String name) {
        UserListEntity entity = new UserListEntity();
        entity.setUserId(userId);
        entity.setUserName(name);
        baseMapper.updateById(entity);
    }
}
