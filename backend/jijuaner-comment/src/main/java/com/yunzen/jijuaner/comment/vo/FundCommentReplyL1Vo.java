package com.yunzen.jijuaner.comment.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 向用户返回的评论回复, 与实体类不同的地方在于: 删去了点赞的用户, 新增是否点赞
 */
@Data
public class FundCommentReplyL1Vo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private Integer userId;
    private Long time;
    private String content;
    private Integer likes;
    private Integer replyNum;
    // private Map<String, Boolean> likeUsers; // 点赞的用户
    private boolean hasLiked;
    private List<FundCommentReplyL2> someReply; // 所有回复

    @Data
    public static class FundCommentReplyL2 implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer toUserId;
        private Integer userId;
        private Long time;
        private String content;
    }
}
