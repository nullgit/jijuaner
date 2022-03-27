package com.yunzen.jijuaner.user.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.user.config.UserUtils;
import com.yunzen.jijuaner.user.dao.UserOptionDao;
import com.yunzen.jijuaner.user.entity.UserOptionEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userOptionService")
public class UserOptionService extends ServiceImpl<UserOptionDao, UserOptionEntity> {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String USER_ID = "user_id";
    private static final String GROUP_ID = "group_id";
    private static final String SORT = "sort";

    /**
     * 判断一个基金是否被一个用户自选, 检查该用户在 redis 的"全部"分组中是否包含该基金
     *
     * @param userId   要判断的用户
     * @param fundCode 要判断的基金
     */
    public Boolean isOptional(Integer userId, String fundCode) {
        return redisTemplate.opsForSet().isMember(JiJuanerConstantString.ALL_OPTION_FUNDS.getConstant() + userId,
                fundCode);
    }

    public UserOptionEntity getByUserIdAndGroupId(Integer userId, Integer groupId, String... columns) {
        return baseMapper.selectOne(
                new QueryWrapper<UserOptionEntity>().select(columns).eq(USER_ID, userId)
                        .and(qw -> qw.eq(GROUP_ID, groupId)));
    }

    public UserOptionEntity getByUserIdAndSort(Integer userId, Byte sort, String... columns) {
        return baseMapper.selectOne(
                new QueryWrapper<UserOptionEntity>().select(columns).eq(USER_ID, userId)
                        .and(qw -> qw.eq(SORT, sort)));
    }

    public List<UserOptionEntity> getByUserId(Integer userId, String... columns) {
        return baseMapper.selectList(
                new QueryWrapper<UserOptionEntity>().select(columns).eq(USER_ID, userId));
    }

    /**
     * 开启自选分组功能
     */
    public void enableOption(Integer userId) {
        addNewGroup(userId, "全部");
    }

    /**
     * 新增一个分组
     */
    public void addNewGroup(Integer userId, String groupName) {
        groupName = groupName.trim();
        String msg = UserUtils.groupNameValidator(groupName);
        if (msg != null) {
            throw JiJuanerException.VALID_EXCEPTION.putMessage(msg);
        }

        // 获取到该用户已经创建的分组数
        Integer groupCount = baseMapper.selectCount(new QueryWrapper<UserOptionEntity>().eq(USER_ID, userId));
        var userOptionEntity = new UserOptionEntity();
        userOptionEntity.setUserId(userId);
        userOptionEntity.setGroupName(groupName);
        userOptionEntity.setSort(Byte.parseByte(groupCount.toString()));
        userOptionEntity.setFunds("[]");
        baseMapper.insert(userOptionEntity);
    }

    /**
     * 重命名一个分组
     */
    public void renameGroup(Integer userId, Integer groupId, String groupName) {
        groupName = groupName.trim();
        String msg = UserUtils.groupNameValidator(groupName);
        if (msg != null) {
            throw JiJuanerException.VALID_EXCEPTION.putMessage(msg);
        }

        UserOptionEntity entity = this.getByUserIdAndGroupId(userId, groupId, GROUP_ID);
        if (entity == null) {
            throw JiJuanerException.OPTION_EXCEPTION.putMessage("用户没有该分组");
        }
        entity.setGroupName(groupName);
        baseMapper.updateById(entity);
    }

    /**
     * 删除用户的某一个分组
     */
    @Transactional
    public void delGroup(Integer userId, Integer groupId) {
        // 删除 groupId 对应的组，并将所有小于该组的 sort - 1
        UserOptionEntity userOptionEntity = this.getByUserIdAndGroupId(userId, groupId, SORT);
        Byte sort = userOptionEntity.getSort();
        List<UserOptionEntity> groupList = baseMapper
                .selectList(new QueryWrapper<UserOptionEntity>().select(GROUP_ID, SORT).eq(USER_ID, userId)
                        .and(qw -> qw.gt(SORT, sort)));
        for (UserOptionEntity group : groupList) {
            group.setSort((byte) (group.getSort() - 1));
        }
        this.updateBatchById(groupList);
        baseMapper.deleteById(groupId);
    }

    /**
     * 交换两个分组的位置
     */
    @Transactional
    public void swapGroup(Integer userId, Byte idx1, Byte idx2) {
        List<UserOptionEntity> list = baseMapper.selectList(
                new QueryWrapper<UserOptionEntity>().select(GROUP_ID, SORT).eq(USER_ID, userId)
                        .and(qw -> qw.in(SORT, idx1, idx2)));
        Byte tmp = list.get(0).getSort();
        list.get(0).setSort(list.get(1).getSort());
        list.get(1).setSort(tmp);
        this.updateBatchById(list);
    }

    /**
     * 向某一个分组添加一组基金
     */
    @Transactional
    public void addNewFunds(Integer userId, Integer groupId, List<String> funds) {
        // 向 user_option 表中对应的组追加基金列表
        UserOptionEntity userOptionEntity;
        if (groupId == null) {
            userOptionEntity = this.getByUserIdAndSort(userId, (byte) 0, "*");
        } else {
            userOptionEntity = this.getByUserIdAndGroupId(userId, groupId, "*");
        }
        List<String> oldFunds = JSON.parseObject(userOptionEntity.getFunds(), new TypeReference<List<String>>() {
        });
        oldFunds.addAll(funds);
        UserOptionEntity newEntity = new UserOptionEntity();
        newEntity.setGroupId(userOptionEntity.getGroupId());
        newEntity.setFunds(JSON.toJSONString(oldFunds));
        baseMapper.updateById(newEntity);

        // 如果是向“全部”分组添加，则同时要向 redis 的 jijuaner:allOptionFunds:userId 中加入新的基金
        if (groupId == null) {
            redisTemplate.opsForSet().add(JiJuanerConstantString.ALL_OPTION_FUNDS.getConstant() + userId,
                    funds.toArray(new String[] {}));
        }
    }

    /**
     * 向某一个分组删除一组基金
     */
    @Transactional
    public void delFunds(Integer userId, Integer groupId, Set<String> delFundSet) {
        // 向 user_option 表中对应的组删除基金列表
        UserOptionEntity userOptionEntity;
        if (groupId == null) {
            userOptionEntity = this.getByUserIdAndSort(userId, (byte) 0, "*");
        } else {
            userOptionEntity = this.getByUserIdAndGroupId(userId, groupId, "*");
        }

        List<String> oldFunds = JSON.parseObject(userOptionEntity.getFunds(), new TypeReference<List<String>>() {
        });
        Iterator<String> iterator = oldFunds.iterator();
        while (iterator.hasNext()) {
            String fundCode = iterator.next();
            if (delFundSet.contains(fundCode)) {
                iterator.remove();
            }
        }

        var newEntity = new UserOptionEntity();
        newEntity.setGroupId(userOptionEntity.getGroupId());
        newEntity.setFunds(JSON.toJSONString(oldFunds));
        baseMapper.updateById(newEntity);

        // 如果是向“全部”分组删除，则同时要删除 redis jijuaner:allOptionFunds:userId 中的基金，并删除其他分组中的列表基金
        if (groupId == null) {
            List<UserOptionEntity> group = this.getByUserId(userId, GROUP_ID, SORT);
            group.stream().filter(entity -> entity.getSort() != 0).parallel()
                    .forEach(entity -> delFunds(userId, entity.getGroupId(), delFundSet));
            redisTemplate.opsForSet().remove(JiJuanerConstantString.ALL_OPTION_FUNDS.getConstant() + userId,
                    delFundSet.toArray());
        }
    }

    // public void editFund(Integer userId, Integer groupId, List<String>
    // groupFunds) {
    // String newGroupFunds = UserUtils.listToString(groupFunds, ",");
    // var entity = new OptionGroupFundEntity();
    // entity.setGroupId(groupId);
    // entity.setGroupFunds(newGroupFunds);
    // baseMapper.update(entity,
    // new UpdateWrapper<OptionGroupFundEntity>().eq("user_id", userId).and(w ->
    // w.eq("group_id", groupId)));
    // }

}
