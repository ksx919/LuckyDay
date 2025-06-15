package com.luckyDay.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckyDay.common.utils.RedisCacheUtil;
import com.luckyDay.model.constants.RedisConstant;
import com.luckyDay.model.user.Captcha;
import com.luckyDay.model.user.User;
import com.luckyDay.model.user.vo.FindPWVO;
import com.luckyDay.model.user.vo.RegisterVO;
import com.luckyDay.model.exception.BaseException;
import com.luckyDay.user.service.CaptchaService;
import com.luckyDay.user.service.LoginService;
import com.luckyDay.user.service.UserService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public User login(User user) {
        final String password = user.getPassword();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        user = userService.getOne(wrapper.eq(User::getEmail,user.getEmail()));
        if(ObjectUtils.isEmpty(user)){
            throw new BaseException("该账号不存在");
        }

        //todo 验证密码是否正确

        return user;
    }

    @Override
    public void captcha(String uuId, HttpServletResponse response) throws IOException {
        if (ObjectUtils.isEmpty(uuId)) throw new IllegalArgumentException("uuid不能为空");
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        BufferedImage image = captchaService.getCaptcha(uuId);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    @Override
    public boolean getCode(Captcha captcha) {
        return captchaService.validate(captcha);
    }

    @Override
    public Boolean checkCode(String email, Integer code) {
        if (ObjectUtils.isEmpty(email) || ObjectUtils.isEmpty(code)){
            throw new BaseException("参数为空");
        }
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + email);

        if (!code.toString().equals(o)){
            throw new BaseException("验证码不正确");

        }
        return true;
    }

    @Override
    public boolean register(RegisterVO registerVO) {
        // 注册成功后删除图形验证码
        if (userService.register(registerVO)){
            captchaService.removeById(registerVO.getUuid());
            return true;
        }
        return false;
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {
        final Boolean b = userService.findPassword(findPWVO);
        return b;
    }
}
