package com.yunzen.jijuaner.comment.vo;

import java.io.Serializable;
import java.util.List;

import com.yunzen.jijuaner.comment.entity.FundCommentReplyL1Entity;

import lombok.Data;

@Data
public class FundCommentVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private Integer userId;
    private Long time;
    private String content;
    private Integer likes;
    private Integer replyNum;

    // private Map<String, Boolean> likeUsers;  // 点赞的用户
    private boolean hasLiked;
    private List<FundCommentReplyL1Entity> someReply;  // 一些回复
}
