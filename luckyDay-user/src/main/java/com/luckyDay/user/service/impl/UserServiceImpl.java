package com.luckyDay.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyDay.common.utils.RedisCacheUtil;
import com.luckyDay.dubbo.service.FavoritesService;
import com.luckyDay.model.constants.RedisConstant;
import com.luckyDay.model.user.Favorites;
import com.luckyDay.model.user.User;
import com.luckyDay.model.user.vo.FindPWVO;
import com.luckyDay.model.user.vo.RegisterVO;
import com.luckyDay.model.exception.BaseException;
import com.luckyDay.user.mapper.UserMapper;
import com.luckyDay.user.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@DubboService
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @DubboReference
    private FavoritesService favoritesService;

    @Override
    public boolean register(RegisterVO registerVO) {
        // 邮箱是否存在
        final int count = (int)count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if (count == 1){
            throw new BaseException("邮箱已被注册");
        }
        final String code = registerVO.getCode();
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + registerVO.getEmail());
        if (o == null){
            throw new BaseException("验证码为空");
        }
        if (!code.equals(o)){
            return false;
        }

        final User user = new User();
        user.setNickName(registerVO.getNickName());
        user.setEmail(registerVO.getEmail());
        user.setDescription("这个人很懒...");
        user.setPassword(registerVO.getPassword());
        save(user);

        // 创建默认收藏夹
        final Favorites favorites = new Favorites();
        favorites.setUserId(user.getId());
        favorites.setName("默认收藏夹");
        favoritesService.save(favorites);

        user.setDefaultFavoritesId(favorites.getId());
        updateById(user);
        return true;
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {
        // 从redis中取出
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + findPWVO.getEmail());
        if (o==null){
            return false;
        }
        // 校验
        if (Integer.parseInt(o.toString()) != findPWVO.getCode()){
            return false;
        }
        // 修改
        final User user = new User();
        user.setEmail(findPWVO.getEmail());
        user.setPassword(findPWVO.getNewPassword());
        update(user,new UpdateWrapper<User>().lambda().set(User::getPassword,findPWVO.getNewPassword()).eq(User::getEmail,findPWVO.getEmail()));
        return true;
    }
}
