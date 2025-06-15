package com.luckyDay.user.controller;

import com.luckyDay.common.utils.JwtUtils;
import com.luckyDay.common.utils.Result;
import com.luckyDay.model.user.Captcha;
import com.luckyDay.model.user.User;
import com.luckyDay.model.user.vo.FindPWVO;
import com.luckyDay.model.user.vo.RegisterVO;
import com.luckyDay.user.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;


@RestController
@RequestMapping("/luckyjourney")
public class LoginController {
    @Autowired
    private LoginService loginService;

    /**
     * 登录
     * @param user
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody @Validated User user){
        user = loginService.login(user);
        //若登陆成功
        String token = JwtUtils.getJwtToken(user.getId(),user.getNickName());
        final HashMap<Object, Object> map = new HashMap<>();
        map.put("token",token);
        map.put("name",user.getNickName());
        map.put("user",user);
        return Result.ok().data(map);
    }

    /**
     * 获取图形验证码
     * @param response
     * @param uuId
     * @throws IOException
     */
    @GetMapping("/login/captcha.jpg/{uuId}")
    public void captcha(HttpServletResponse response, @PathVariable String uuId) throws IOException {
        loginService.captcha(uuId,response);
    }

    /**
     * 获取验证码
     * @param captcha
     * @return
     * @throws Exception
     */
    @PostMapping("/login/getCode")
    public Result getCode(@RequestBody @Validated Captcha captcha) throws Exception {
        if (!loginService.getCode(captcha)) {
            return Result.error().message("验证码发送失败");
        }
        return Result.ok().message("发送成功,请耐心等待");
    }

    /**
     * 检测邮箱验证码
     * @param email
     * @param code
     * @return
     */
    @PostMapping("/login/check")
    public Result check(String email,Integer code){
        loginService.checkCode(email,code);
        return Result.ok().message("验证成功");
    }

    /**
     * 注册
     * @param registerVO
     * @return
     * @throws Exception
     */
    @PostMapping("/register")
    public Result register(@RequestBody @Validated RegisterVO registerVO) throws Exception {
        if (!loginService.register(registerVO)) {
            return Result.error().message("注册失败,验证码错误");
        }
        return Result.ok().message("注册成功");
    }

    /**
     * 找回密码
     * @param findPWVO
     * @return
     */
    @PostMapping("/findPassword")
    public Result findPassword(@RequestBody @Validated FindPWVO findPWVO, HttpServletResponse response){
        final Boolean b = loginService.findPassword(findPWVO);
        return Result.ok().message(b ? "修改成功" : "修改失败,验证码不正确");
    }
}
