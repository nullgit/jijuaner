package com.yunzen.jijuaner.comment.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 评论回复实体类, 保存在 mongo 中
 */
@Document(collection = "fund_comment_reply")
@Data
public class FundCommentReplyL1Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private Integer userId;
    private String toCommentId;
    private Long time;
    private String content;
    private Integer likes;
    private Integer replyNum;
    private Map<String, Boolean> likeUsers; // 点赞的用户
    private List<FundCommentReplyL2> replyL2; // 二级评论

    @Data
    public static class FundCommentReplyL2 implements Serializable {
        private static final long serialVersionUID = 1L;

        @Transient // 不加到数据库中
        private String toReplyId;
        private Integer toUserId;
        private Integer userId;
        private Long time;
        private String content;
    }
}
