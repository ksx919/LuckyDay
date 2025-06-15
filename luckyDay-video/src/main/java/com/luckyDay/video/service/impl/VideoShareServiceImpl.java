package com.luckyDay.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyDay.model.video.VideoShare;
import com.luckyDay.video.mapper.VideoShareMapper;
import com.luckyDay.video.service.VideoShareService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
public class VideoShareServiceImpl extends ServiceImpl<VideoShareMapper, VideoShare> implements VideoShareService {
    @Override
    public boolean share(VideoShare videoShare) {
        try{
            this.save(videoShare);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    @Override
    public List<Long> getShareUserId(Long videoId) {
        return this.list(new LambdaQueryWrapper<VideoShare>().eq(VideoShare::getVideoId,videoId)).stream().map(VideoShare::getUserId).collect(Collectors.toList());
    }
}
