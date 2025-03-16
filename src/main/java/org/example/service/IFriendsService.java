package org.example.service;

import org.example.entity.Friends;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author elaina
 * @since 2025-03-15
 */
public interface IFriendsService extends IService<Friends> {
    boolean addFriendRecord(Friends friends);
    List<Friends> getFriendsByUserId(Long userId);
    boolean updateFriendRecord(Friends friends);
}
