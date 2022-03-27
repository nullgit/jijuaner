package com.yunzen.jijuaner.comment.entity;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 基金评论实体类, 保存在 mongo 中
 */
@Document(collection = "fund_comment")
@Data
public class FundCommentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String fundCode;
    private Integer userId;
    private Long time;
    private String content;
    private Integer likes;
    private Integer replyNum;
    private Map<String, Boolean> likeUsers; // 点赞的用户
}
