package com.luckyDay.user.service;

import com.luckyDay.model.user.Captcha;
import com.luckyDay.model.user.User;
import com.luckyDay.model.user.vo.FindPWVO;
import com.luckyDay.model.user.vo.RegisterVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface LoginService {
    User login(User user);

    void captcha(String uuId, HttpServletResponse response) throws IOException;

    boolean getCode(Captcha captcha);

    Boolean checkCode(String email, Integer code);

    boolean register(RegisterVO registerVO);

    Boolean findPassword(FindPWVO findPWVO);
}
