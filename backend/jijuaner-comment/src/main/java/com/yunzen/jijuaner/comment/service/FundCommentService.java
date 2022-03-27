package com.yunzen.jijuaner.comment.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.yunzen.jijuaner.comment.entity.FundCommentEntity;
import com.yunzen.jijuaner.comment.entity.FundCommentReplyL1Entity;
import com.yunzen.jijuaner.comment.entity.FundCommentReplyL1Entity.FundCommentReplyL2;
import com.yunzen.jijuaner.common.utils.SignInUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class FundCommentService {
    private static final String FUND_COMMENT = "fund_comment";
    private static final String FUND_COMMENT_REPLY = "fund_comment_reply";
    private static final String _ID = "_id";
    private static final String FUND_CODE = "fundCode";
    private static final String TIME = "time";
    private static final String LIKES = "likes";
    private static final String TO_COMMENT_ID = "toCommentId";
    private static final String LIKE_USERS = "likeUsers";
    private static final String REPLY_NUM = "replyNum";

    @Autowired
    private MongoTemplate mongo;

    /**
     * 用户发表对某一基金的评论
     */
    public void addComment(FundCommentEntity entity) {
        entity.setUserId(SignInUtils.getUserId());
        entity.setTime(System.currentTimeMillis());
        entity.setLikes(0);
        entity.setReplyNum(0);
        entity.setLikeUsers(new HashMap<>());
        mongo.save(entity);
    }

    /**
     * 获得评论页
     */
    public List<FundCommentEntity> getCommentPage(String fundCode, Integer page, Integer size) {
        Query query = Query.query(Criteria.where(FUND_CODE).is(fundCode));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, TIME));
        query.with(pageRequest);
        query.fields().exclude(LIKE_USERS);
        return mongo.find(query, FundCommentEntity.class);
    }

    public FundCommentEntity getCommentById(String id) {
        Query query = Query.query(Criteria.where(_ID).is(id));
        // 排除 like_user 字段
        query.fields().exclude(LIKE_USERS);
        return mongo.findOne(query, FundCommentEntity.class);
    }

    /**
     * 返回 userId 对应的用户是否对 commentId 对应的评论点过赞
     *
     * @param commentId 0 表示评论，1 表示一级回复
     */
    public boolean hasLiked(String commentId, Integer userId, Byte commentLevel) {
        Query query = Query
                .query(Criteria.where(_ID).is(commentId).and(LIKE_USERS + "." + userId).exists(true));
        long count = mongo.count(query,
                commentLevel == 0 ? FundCommentEntity.class : FundCommentReplyL1Entity.class);
        return count != 0;
    }

    /**
     * 如果该用户已经点赞，则取消赞；如果没点赞，则加上赞
     *
     * @param commentLevel 如果是0，表示点赞评论，如果是1，表示点赞回复
     */
    public void toggleLikes(Integer userId, String commentId, Byte commentLevel) {
        Query query = Query.query(Criteria.where(_ID).is(commentId));
        var update = new Update();
        boolean hasLiked = hasLiked(commentId, userId, commentLevel);
        if (hasLiked) {
            update.inc(LIKES, -1);
            update.unset(LIKE_USERS + "." + userId);
        } else {
            update.inc(LIKES, 1);
            update.set(LIKE_USERS + "." + userId, true);
        }
        mongo.updateFirst(query, update, commentLevel == 0 ? FUND_COMMENT : FUND_COMMENT_REPLY);
    }

    /**
     * 回复评论
     */
    public void replyToComment(FundCommentReplyL1Entity entity) {
        entity.setUserId(SignInUtils.getUserId());
        entity.setTime(System.currentTimeMillis());
        entity.setLikes(0);
        entity.setReplyL2(new ArrayList<>());

        Query query = Query.query(Criteria.where(_ID).is(entity.getToCommentId()));
        var update = new Update();
        update.inc(REPLY_NUM, 1);
        mongo.updateFirst(query, update, FundCommentEntity.class);
        mongo.save(entity);
    }

    /**
     * 获得回复页
     */
    public List<FundCommentReplyL1Entity> getReplyL1Page(String commentId, Integer page, Integer size) {
        Query query = Query.query(Criteria.where(TO_COMMENT_ID).is(commentId));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, LIKES));
        query.with(pageRequest);
        return mongo.find(query, FundCommentReplyL1Entity.class);
    }

    public FundCommentReplyL1Entity getReplyById(String id) {
        Query query = Query.query(Criteria.where(_ID).is(id));
        // 排除 like_user 字段
        query.fields().exclude(LIKE_USERS);
        return mongo.findOne(query, FundCommentReplyL1Entity.class);
    }

    /**
     * 对回复进行回复
     */
    public void replyToReply(FundCommentReplyL2 entity) {
        entity.setUserId(SignInUtils.getUserId());
        entity.setTime(System.currentTimeMillis());

        Query query = Query.query(Criteria.where(_ID).is(entity.getToReplyId()));
        var update = new Update();
        update.push("replyL2", entity);
        mongo.updateFirst(query, update, FundCommentReplyL1Entity.class);
    }
}
