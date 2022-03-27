package com.yunzen.jijuaner.user.controller;

import java.util.HashSet;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.to.FundSimpleAndRealTimeInfoTo;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.common.utils.SignInUtils;
import com.yunzen.jijuaner.user.config.UserUtils;
import com.yunzen.jijuaner.user.entity.UserOptionEntity;
import com.yunzen.jijuaner.user.feign.FundFeignService;
import com.yunzen.jijuaner.user.service.UserOptionService;
import com.yunzen.jijuaner.user.vo.UserOptionVo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/userOption")
public class UserOptionContoller {
    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund user option!";
    }

    @Autowired
    private UserOptionService userOptionService;

    @Autowired
    private FundFeignService fundFeignService;

    /**
     * 开启自选分组功能
     *
     * @see UserOptionService#enableOption(Integer)
     */
    @RequestMapping("/enableOption")
    public R enableOption() {
        userOptionService.enableOption(SignInUtils.getUserId());
        return R.ok().putMsg("开通基金自选功能成功！");
    }

    /**
     * 获取该用户已经创建的分组
     */
    @RequestMapping("/getGroups")
    public R getGroups() {
        List<UserOptionEntity> entities = userOptionService.getByUserId(SignInUtils.getUserId(), "group_id",
                "group_name", "sort");
        if (entities.isEmpty()) {
            throw JiJuanerException.OPTION_EXCEPTION.putMessage("该用户尚未开通自选服务");
        }

        entities.sort((e1, e2) -> Byte.compare(e1.getSort(), e2.getSort()));
        List<UserOptionVo> vos = entities.stream().map(entity -> {
            var vo = new UserOptionVo();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
        return R.ok().putData(vos);
    }

    /**
     * 获取该用户某分组的基金
     */
    @RequestMapping("/getFunds")
    public R getFunds(@RequestParam("groupId") Integer groupId) {
        UserOptionEntity entity = userOptionService
                .getByUserIdAndGroupId(SignInUtils.getUserId(), groupId, "*");
        List<String> funds = JSON.parseObject(entity.getFunds(), new TypeReference<List<String>>() {
        });

        var vo = new UserOptionVo();
        vo.setGroupId(groupId);
        vo.setFunds(funds);
        R infosR = fundFeignService.getSimpleAndRealTimeInfos(funds);
        if (infosR.getCode() == 0) {
            vo.setInfos((List<FundSimpleAndRealTimeInfoTo>) infosR.getData());
        } else {
            return R.error().putMsg(infosR.getMsg());
        }
        return R.ok().putData(vo);
    }

    /**
     * 判断一个基金是否被一个用户自选
     *
     * @see UserOptionService#isOptional(Integer, String)
     */
    @RequestMapping("/isOptional")
    public R isOptional(@RequestParam("fundCode") String fundCode) {
        Boolean isOptional = userOptionService.isOptional(SignInUtils.getUserId(), fundCode);
        return R.ok().putData(isOptional);
    }

    /**
     * 新增一个分组
     *
     * @see UserOptionContoller#addNewGroup(String)
     */
    @RequestMapping("/addNewGroup")
    public R addNewGroup(@RequestParam("groupName") String groupName) {
        userOptionService.addNewGroup(SignInUtils.getUserId(), groupName);
        return R.ok().putMsg("添加分组成功！");
    }

    /**
     * 重命名一个分组
     *
     * @see UserOptionService#renameGroup(Integer, Integer, String)
     */
    @RequestMapping("/renameGroup")
    public R renameGroup(@RequestParam("groupId") Integer groupId, @RequestParam("groupName") String groupName) {
        userOptionService.renameGroup(SignInUtils.getUserId(), groupId, groupName);
        return R.ok().putMsg("重命名分组成功！");
    }

    @RequestMapping("/addNewFunds")
    // userId=9&groupId=1&fundCode=000001
    public R addNewFunds(@RequestBody UserOptionVo vo) {
        userOptionService.addNewFunds(SignInUtils.getUserId(), vo.getGroupId(),
                vo.getFunds());
        return R.ok().putMsg("添加基金成功！");
    }

    /**
     * 删除用户的某一个分组
     *
     * @see UserOptionService#delGroup(Integer, Integer)
     */
    @RequestMapping("/delGroup")
    public R delGroup(@RequestParam("groupId") Integer groupId) {
        userOptionService.delGroup(SignInUtils.getUserId(), groupId);
        return R.ok().putMsg("删除分组成功！");
    }

    /**
     * 向某一个分组删除一组基金
     *
     * @see UserOptionService#delFunds(Integer, Integer, java.util.Set)
     */
    @RequestMapping("/delFunds")
    public R delFunds(@RequestBody UserOptionVo vo) {
        var fundSet = new HashSet<String>();
        fundSet.addAll(vo.getFunds());
        userOptionService.delFunds(SignInUtils.getUserId(), vo.getGroupId(),
                fundSet);
        return R.ok().putMsg("删除分组中的对应基金成功！");
    }

    /**
     * 交换两个分组的位置
     *
     * @see UserOptionService#swapGroup(Integer, Byte, Byte)
     */
    @RequestMapping("/swapGroup")
    public R swapGroup(@RequestParam("idx1") Byte idx1, @RequestParam("idx2") Byte idx2) {
        userOptionService.swapGroup(SignInUtils.getUserId(), idx1, idx2);
        return R.ok().putMsg("交换分组成功！");
    }

    // @RequestMapping("/editFund")
    // public R editFund(@RequestBody UserOptionVo vo) {
    // userOptionService.editFund(SignInUtils.getUserId(),
    // vo.getGroupId(),
    // vo.getFunds());
    // return R.ok().putMsg("编辑分组中的基金成功！");
    // }
}
