package org.example.controller;

import org.example.entity.Users;
import org.example.service.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author elaina
 * @since 2025-02-28
 */
@Controller
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private IUsersService usersService;
    //user是否存在
    @GetMapping("/checkUserExit")
    @ResponseBody
    public boolean checkUserExit(Long userId) {
        return usersService.checkUserExits(userId);
    }

//    @PostMapping("/login")
//    @ResponseBody
//    public String login(@RequestParam("id") long id,@RequestParam("password") String password) {
//        String token = usersService.login(id, password);
//        if(token != null) {
//            return token;
//        }
//        return "login failed";
//    }

    @PostMapping("/updateUser")
    @ResponseBody
    public boolean updateUser(@RequestBody Users user) {
        return usersService.updateUser(user);
    }


}
