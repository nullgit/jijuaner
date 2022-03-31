package com.yunzen.jijuaner.user.service;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

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
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.user.config.UserUtils;
import com.yunzen.jijuaner.user.dao.UserListDao;
import com.yunzen.jijuaner.user.entity.UserListEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("userListService")
public class UserListService extends ServiceImpl<UserListDao, UserListEntity> {
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String HEAD_IMG = "head_img";
    private static final String EMAIL = "email";

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private StringRedisTemplate redis;

    /**
     * 注册
     */
    @Transactional
    public void login(UserListEntity userListEntity, String code) {
        String message = null;
        if (userListEntity.getEmail() == null) {
            message = "邮箱账号不能为空";
        } else if (!UserUtils.testEmail(userListEntity.getEmail())) {
            message = "邮箱格式不正确";
        } else if (userListEntity.getPassword() == null) {
            message = "密码不能为空";
        } else if (userListEntity.getPassword().length() < 6) {
            message = "密码需要至少6位数";
        } else if (code == null) {
            message = "验证码不能为空";
        }
        if (message != null) {
            throw JiJuanerException.VALID_EXCEPTION.putMessage(message);
        }

        // TODO 防止验证码在60s内反复提交
        String key = JiJuanerConstantString.VERIFICATION_CODE.getConstant() + userListEntity.getEmail();
        String realCode = redis.opsForValue().get(key);
        // 验证验证码
        if (realCode != null && realCode.equals(code)) {
            redis.delete(key);
        } else {
            throw JiJuanerException.LOGIN_EXCEPTION.putMessage("验证码不正确或已过期");
        }

        Integer emailCount = baseMapper
                .selectCount(new QueryWrapper<UserListEntity>().eq(EMAIL, userListEntity.getEmail()));
        if (emailCount != 0) {
            throw JiJuanerException.LOGIN_EXCEPTION.putMessage("该邮箱已注册过账号");
        }

        String encode = bCryptPasswordEncoder.encode(userListEntity.getPassword());
        userListEntity.setPassword(encode);
        baseMapper.insert(userListEntity);
    }

    /**
     * 登录
     */
    public UserListEntity signIn(String email, String password) {
        String message = null;
        if (email == null) {
            message = "邮箱账号不能为空";
        } else if (password == null) {
            message = "密码不能为空";
        }
        if (message != null) {
            throw JiJuanerException.VALID_EXCEPTION.putMessage(message);
        }

        UserListEntity entity = baseMapper
                .selectOne(new QueryWrapper<UserListEntity>().eq(EMAIL, email));
        if (entity == null) {
            throw JiJuanerException.SIGN_IN_EXCEPTION.putMessage("邮箱账号不存在, 请注册");
        }

        if (!bCryptPasswordEncoder.matches(password, entity.getPassword())) {
            throw JiJuanerException.SIGN_IN_EXCEPTION.putMessage("密码错误");
        }
        return entity;
    }

    /**
     * 获取用户的信息(用户id, 用户名, 头像)
     */
    @Cacheable(cacheNames = "jijuaner:userList")
    public UserListEntity getUserInfo(Integer id) {
        return baseMapper
                .selectOne(new QueryWrapper<UserListEntity>().eq(USER_ID, id).select(USER_ID, USER_NAME, HEAD_IMG));
    }

    private static Properties emailProperties;
    @Value("${config.email.server}")
    public String emailServer;
    @Value("${config.email.account}")
    public String emailAccount;
    @Value("${config.email.authcode}")
    public String emailAuthcode;
    @Value("${config.email.from}")
    public String emailFrom;

    public UserListService() throws GeneralSecurityException {
        emailProperties = new Properties();
        // 开启debug调试，以便在控制台查看
        // emailProperties.setProperty("mail.debug", "true");
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

    /**
     * 向 email 发送验证码
     */
    public void sendCode(String email) throws MessagingException {
        if (!UserUtils.testEmail(email)) {
            throw JiJuanerException.VALID_EXCEPTION.putMessage("邮箱格式不正确");
        }

        // 创建session
        Session session = Session.getInstance(emailProperties);
        // 通过session得到transport对象
        Transport ts = session.getTransport();
        // ts.connect("smtp.qq.com", "发送方的QQ号", "POP3/SMTP协议授权码");
        ts.connect(emailServer, emailAccount, emailAuthcode);

        // 创建邮件对象
        MimeMessage message = new MimeMessage(session);
        // 指明邮件的发件人
        message.setFrom(new InternetAddress(emailFrom));
        // 指明邮件的收件人，发件人和收件人如果是一样的，那就是自己给自己发
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        // 邮件的标题
        message.setSubject("鸡圈儿验证码");
        // 邮件的文本内容
        String code = UserUtils.getCode();
        message.setContent("[鸡圈儿] 账号注册验证码为(5分钟有效): " + code
                + ", 请勿回复此邮箱.", "text/html;charset=UTF-8");
        // 发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();

        // Redis 保存邮件 + 验证码
        ValueOperations<String, String> opsForValue = redis.opsForValue();
        opsForValue.set(JiJuanerConstantString.VERIFICATION_CODE.getConstant() + email, code, Duration.ofMinutes(5));
    }

    @Resource
    private OSSClient ossClient;
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;
    @Value("${spring.cloud.alicloud.oss.bucket-name}")
    private String bucket;
    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;

    /**
     * 获取云存储服务的 policy
     */
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
        if (oldImg != null) { // 删除原来的头像
            ossClient.deleteObject(bucket, oldImg.replace("https://" + bucket + "." + endpoint + "/", ""));
        }
    }

    /**
     * 用户重命名
     */
    public void rename(Integer userId, String name) {
        name = name.trim();
        String message = null;
        if (!StringUtils.hasText(name)) {
            message = "名字中没有有效字符";
        } else if (name.length() > 32) {
            message = "名字长度不能超过32个字符";
        }
        if (message != null) {
            throw JiJuanerException.VALID_EXCEPTION.putMessage(message);
        }

        UserListEntity entity = new UserListEntity();
        entity.setUserId(userId);
        entity.setUserName(name);
        baseMapper.updateById(entity);
    }
}
