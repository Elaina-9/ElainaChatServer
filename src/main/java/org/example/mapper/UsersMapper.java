package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author elaina
 * @since 2025-02-28
 */
public interface UsersMapper extends BaseMapper<Users> {
    int checkUserExistsById(Long userId);
    //返回账号密码匹配的用户数量
    Users login(@Param("id") long id, @Param("password") String password);
    int updateUser(Users user);
    Long getUserIdByToken(String token);
}

