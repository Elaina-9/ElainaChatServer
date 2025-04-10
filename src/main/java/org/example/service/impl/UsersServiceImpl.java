package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.entity.Users;
import org.example.mapper.UsersMapper;
import org.example.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author elaina
 * @since 2025-02-28
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    @Override
    public boolean checkUserExits(Long userId) {
        return this.baseMapper.checkUserExistsById(userId) > 0;
    }
    @Override
    public Users login(long id, String password) {
        Users user = this.baseMapper.login(id, password);
        //更新token
        if(user != null) {
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            this.baseMapper.updateUser(user);
            return user;
        }
        return null;
    }

    @Override
    public boolean updateUser(Users user) {
        return this.baseMapper.updateUser(user) > 0;
    }

    @Override
    public Users register(Users user) {
        if(this.checkUserExits(user.getId())) {
            return null;
        }
        user.setStatus((byte)1);
        boolean success = this.save(user);
        return success?user:null;
    }

    @Override
    public Long getUserIdByToken(String token) {
        return this.baseMapper.getUserIdByToken(token);
    }

    @Override
    public Users getUserById(Long userId) {
        return this.baseMapper.getUserById(userId);
    }
}
