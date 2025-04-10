package org.example.service;

import org.example.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author elaina
 * @since 2025-02-28
 */
public interface IUsersService extends IService<Users> {
    boolean checkUserExits(Long userId);
    Users login(long id, String password);
    boolean updateUser(Users user);
    Users register(Users user);
    Long getUserIdByToken(String token);
    Users getUserById(Long id);
}
