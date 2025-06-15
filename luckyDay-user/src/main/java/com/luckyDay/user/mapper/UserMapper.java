package com.luckyDay.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luckyDay.model.user.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
