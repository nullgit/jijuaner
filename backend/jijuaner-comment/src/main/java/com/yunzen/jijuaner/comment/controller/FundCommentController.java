package com.yunzen.jijuaner.comment.controller;

import java.util.ArrayList;
import java.util.List;

import com.yunzen.jijuaner.comment.entity.FundCommentEntity;
import com.yunzen.jijuaner.comment.entity.FundCommentReplyL1Entity;
import com.yunzen.jijuaner.comment.entity.FundCommentReplyL1Entity.FundCommentReplyL2;
import com.yunzen.jijuaner.comment.service.FundCommentService;
import com.yunzen.jijuaner.comment.vo.FundCommentReplyL1Vo;
import com.yunzen.jijuaner.comment.vo.FundCommentVo;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.common.utils.SignInUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment/fundComment")
public class FundCommentController {
    @Autowired
    private FundCommentService fundCommentService;

    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund comment!";
    }

    /**
     * 用户发表对某一基金的评论
     *
     * @see FundCommentService#addComment(FundCommentEntity)
     */
    @PostMapping("/addComment")
    public R addComment(@RequestBody FundCommentEntity to) {
        fundCommentService.addComment(to);
        return R.ok().putMsg("评论发表成功");
    }

    /**
     * 将评论数据转化为 vo, 可以加上该用户是否对某条评论点赞
     */
    private FundCommentVo fundCommentEntityToVo(FundCommentEntity entity, boolean needSomeReply) {
        var vo = new FundCommentVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setHasLiked(
                fundCommentService.hasLiked(entity.getId(), SignInUtils.getUserId(), (byte) 0));
        vo.setSomeReply(needSomeReply ? fundCommentService.getReplyL1Page(entity.getId(), 0, 2) : new ArrayList<>());
        return vo;
    }

    /**
     * 获得某一条评论
     *
     * @see FundCommentService#getCommentById(String)
     */
    @GetMapping("/getComment/{id}")
    public R getCommentById(@PathVariable("id") String id) {
        FundCommentEntity entity = fundCommentService.getCommentById(id);
        return R.ok().putData(fundCommentEntityToVo(entity, false));
    }

    /**
     * 获得评论页
     *
     * @see FundCommentService#getCommentPage(String, Integer, Integer)
     */
    @GetMapping("/getCommentPage")
    public R getCommentPage(@RequestParam("id") String fundCode, @RequestParam("page") Integer page,
            @RequestParam("size") Integer size) {
        List<FundCommentEntity> data = fundCommentService.getCommentPage(fundCode, page, size);
        return R.ok().putData(data.stream().map(entity -> fundCommentEntityToVo(entity, true)).toList());
    }

    /**
     * 回复评论
     *
     * @see FundCommentService#replyToComment(FundCommentReplyL1Entity)
     */
    @PostMapping("/replyComment")
    public R replyToComment(@RequestBody FundCommentReplyL1Entity to) {
        fundCommentService.replyToComment(to);
        return R.ok().putMsg("回复评论成功");
    }

    /**
     * 将回复数据转化为 vo, 可以加上该用户是否对某条回复点赞
     */
    private FundCommentReplyL1Vo fundCommentReplyEntityToVo(FundCommentReplyL1Entity entity) {
        var vo = new FundCommentReplyL1Vo();
        BeanUtils.copyProperties(entity, vo);
        vo.setHasLiked(
                fundCommentService.hasLiked(entity.getId(), SignInUtils.getUserId(), (byte) 1));
        vo.setSomeReply(entity.getReplyL2().stream().map(l2entity -> {
            var l2vo = new com.yunzen.jijuaner.comment.vo.FundCommentReplyL1Vo.FundCommentReplyL2();
            BeanUtils.copyProperties(l2entity, l2vo);
            return l2vo;
        }).toList());
        return vo;
    }

    /**
     * 获得回复页
     *
     * @see FundCommentService#getReplyL1Page(String, Integer, Integer)
     */
    @GetMapping("/getReplyL1Page")
    public R getReplyL1Page(@RequestParam("id") String commentId, @RequestParam("page") Integer page,
            @RequestParam("size") Integer size) {
        List<FundCommentReplyL1Entity> data = fundCommentService.getReplyL1Page(commentId, page, size);
        return R.ok().putData(data.stream().map(this::fundCommentReplyEntityToVo).toList());
    }

    /**
     * 如果该用户已经点赞, 则取消赞; 如果没点赞, 则加上赞
     *
     * @param commentLevel 如果是 0, 表示点赞评论, 如果是 1, 表示点赞回复
     * @see FundCommentService#toggleLikes(Integer, String, Byte)
     */
    @GetMapping("/toggleLikes")
    public R toggleLikes(@RequestParam("commentId") String commentId,
            @RequestParam("commentLevel") Byte commentLevel) {
        fundCommentService.toggleLikes(SignInUtils.getUserId(), commentId, commentLevel);
        return R.ok();
    }

    /**
     * 对回复进行回复
     *
     * @see FundCommentService#replyToReply(FundCommentReplyL2)
     */
    @PostMapping("/replyToReply")
    public R replyToReply(@RequestBody FundCommentReplyL2 to) {
        fundCommentService.replyToReply(to);
        return R.ok().putMsg("回复回复成功");
    }
}
