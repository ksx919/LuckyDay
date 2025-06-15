package com.luckyDay.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luckyDay.model.user.Captcha;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CaptchaMapper extends BaseMapper<Captcha> {
}
