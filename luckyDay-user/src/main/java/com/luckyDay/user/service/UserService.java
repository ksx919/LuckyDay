package com.luckyDay.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyDay.model.user.User;
import com.luckyDay.model.user.vo.FindPWVO;
import com.luckyDay.model.user.vo.RegisterVO;

public interface UserService extends IService<User> {
    boolean register(RegisterVO registerVO);

    Boolean findPassword(FindPWVO findPWVO);

}
