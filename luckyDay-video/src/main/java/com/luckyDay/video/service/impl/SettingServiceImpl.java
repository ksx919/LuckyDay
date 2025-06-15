package com.luckyDay.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyDay.model.video.Setting;
import com.luckyDay.video.mapper.SettingMapper;
import com.luckyDay.video.service.SettingService;
import org.springframework.stereotype.Service;

@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements SettingService {

}
