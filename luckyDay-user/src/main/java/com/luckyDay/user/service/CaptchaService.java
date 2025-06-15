package com.luckyDay.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyDay.model.user.Captcha;

import java.awt.image.BufferedImage;

public interface CaptchaService extends IService<Captcha> {
    BufferedImage getCaptcha(String uuId);

    boolean validate(Captcha captcha);
}
