package com.yunzen.jijuaner.user.controller;

import java.util.HashSet;
import java.util.List;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.user.config.UserUtils;
import com.yunzen.jijuaner.user.entity.UserListEntity;
import com.yunzen.jijuaner.user.entity.UserOptionEntity;
import com.yunzen.jijuaner.user.feign.FundFeignService;
import com.yunzen.jijuaner.user.interceptor.UserInterceptor;
import com.yunzen.jijuaner.user.service.UserOptionService;
import com.yunzen.jijuaner.user.vo.UserOptionVo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

    @RequestMapping("/enableOption")
    public R enableOption() {
        userOptionService.enableOption(UserInterceptor.toThreadLocal.get().getUserId());
        return R.ok().putMsg("开通基金自选功能成功！");
    }

    @RequestMapping("/getGroups")
    public R getGroups() {
        UserListEntity to = UserInterceptor.toThreadLocal.get();
        if (to == null) {
            return R.error().putCode(JiJuanerException.SIGN_IN_EXCEPTION.getCode()).putMsg("用户需要登录");
        }
        List<UserOptionEntity> entities = userOptionService.getByUserId(to.getUserId(), "group_id", "group_name", "sort");
        if (entities == null || entities.isEmpty()) {
            return R.error().putCode(JiJuanerException.OPTION_GROUP_EXCEPTION.getCode()).putMsg("该用户尚未开通自选服务");
        }
        entities.sort((e1, e2)-> Short.compare(e1.getSort(), e2.getSort()));
        List<UserOptionVo> vos = entities.stream().map(entity -> {
            var vo = new UserOptionVo();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
        return R.ok().putData(vos);
    }

    @RequestMapping("/getFunds")
    public R getFunds(@RequestParam("groupId") Integer groupId) {
        UserOptionEntity entity = userOptionService.getByUserIdAndGroupId(UserInterceptor.toThreadLocal.get().getUserId(), groupId, "*");
        List<String> funds = UserUtils.stringToStringList(entity.getFunds(), ",");

        var vo = new UserOptionVo();
        vo.setGroupId(groupId);
        vo.setFunds(funds);
        R fundNamesR = fundFeignService.getNames(funds);
        if (fundNamesR.getCode() == 0) {
            @SuppressWarnings("unchecked")
            List<String> fundNames = (List<String>) fundNamesR.getData();
            vo.setFundNames(fundNames);
        } else {
            return R.error().putMsg(fundNamesR.getMsg());
        }
        return R.ok().putData(vo);
    }

    @RequestMapping("/isOptional")
    public R isOptional(@RequestParam("fundCode") String fundCode) {
        Boolean isOptional = userOptionService.isOptional(UserInterceptor.toThreadLocal.get().getUserId(), fundCode);
        return R.ok().putData(isOptional);
    }

    private String groupNameValidator(String groupName) {
        if (!StringUtils.hasText(groupName)) {
            return "分组名称不能为空！";
        } else if (groupName.length() > 6) {
            return "分组名称字符不能超过6！";
        }
        return null;
    }

    @RequestMapping("/addNewGroup")
    public R addNewGroup(@RequestParam("groupName") String groupName) {
        groupName = groupName.trim();
        String msg = groupNameValidator(groupName);
        if (msg != null) {
            return R.error().putMsg(msg);
        }
        userOptionService.addNewGroup(UserInterceptor.toThreadLocal.get().getUserId(), groupName);
        return R.ok().putMsg("添加分组成功！");
    }

    @RequestMapping("/renameGroup")
    public R renameGroup(@RequestParam("groupId") Integer groupId, @RequestParam("groupName") String groupName) {
        groupName = groupName.trim();
        String msg = groupNameValidator(groupName);
        if (msg != null) {
            return R.error().putMsg(msg);
        }
        userOptionService.renameGroup(UserInterceptor.toThreadLocal.get().getUserId(), groupId, groupName);
        return R.ok().putMsg("重命名分组成功！");
    }


    @RequestMapping("/addNewFunds")
    // userId=9&groupId=1&fundCode=000001
    public R addNewFunds(@RequestBody UserOptionVo vo) {
        userOptionService.addNewFunds(UserInterceptor.toThreadLocal.get().getUserId(), vo.getGroupId(),
                vo.getFunds());
        return R.ok().putMsg("添加基金成功！");
    }

    @RequestMapping("/delGroup")
    public R delGroup(@RequestParam("groupId") Integer groupId) {
        userOptionService.delGroup(UserInterceptor.toThreadLocal.get().getUserId(), groupId);
        return R.ok().putMsg("删除分组成功！");
    }

    @RequestMapping("/delFunds")
    public R delFunds(@RequestBody UserOptionVo vo) {
        var fundSet = new HashSet<String>();
        fundSet.addAll(vo.getFunds());
        userOptionService.delFunds(UserInterceptor.toThreadLocal.get().getUserId(), vo.getGroupId(),
                fundSet);
        return R.ok().putMsg("删除分组中的对应基金成功！");
    }

    @RequestMapping("/swapGroup")
    public R swapGroup(@RequestParam("idx1") Short idx1, @RequestParam("idx2") Short idx2) {
        userOptionService.swapGroup(UserInterceptor.toThreadLocal.get().getUserId(), idx1, idx2);
        return R.ok().putMsg("交换分组成功！");
    }

    @RequestMapping("/editFund")
    public R editFund(@RequestBody UserOptionVo vo) {
        userOptionService.editFund(UserInterceptor.toThreadLocal.get().getUserId(), vo.getGroupId(),
                vo.getFunds());
        return R.ok().putMsg("编辑分组中的基金成功！");
    }
}
