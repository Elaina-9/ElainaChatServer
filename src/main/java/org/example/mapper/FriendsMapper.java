package org.example.mapper;

import org.example.entity.Friends;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author elaina
 * @since 2025-03-15
 */
public interface FriendsMapper extends BaseMapper<Friends> {
    int updateFriendStatus(Friends friends);
}

