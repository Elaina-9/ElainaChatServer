package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.entity.Friends;
import org.example.mapper.FriendsMapper;
import org.example.service.IFriendsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author elaina
 * @since 2025-03-15
 */
@Service
public class FriendsServiceImpl extends ServiceImpl<FriendsMapper, Friends> implements IFriendsService {
    @Override
    public boolean addFriendRecord(Friends friends) {
        return this.save(friends);
    }
    @Override
    public List<Friends> getFriendsByUserId(Long userId) {
        LambdaQueryWrapper<Friends> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friends::getUserId, userId)
                .or()
                .eq(Friends::getFriendId, userId);
        return this.list(wrapper);
    }

    @Override
    public boolean updateFriendRecord(Friends friends) {
        if(friends.getStatus() == 1) {
            return this.baseMapper.updateFriendStatus(friends) > 0;
        }
            return this.removeById(friends.getId());
        }
    }
