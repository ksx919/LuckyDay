package com.luckyDay.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyDay.model.video.VideoStar;
import com.luckyDay.video.mapper.VideoStarMapper;
import com.luckyDay.video.service.VideoStarService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
public class VideoStarServiceImpl extends ServiceImpl<VideoStarMapper, VideoStar> implements VideoStarService {
    @Override
    public boolean starVideo(VideoStar videoStar) {
        try{
            this.save(videoStar);
        }catch (Exception e){
            //存在则取消点赞
            this.remove(new LambdaQueryWrapper<>(VideoStar.class).eq(VideoStar::getVideoId, videoStar.getVideoId()).eq(VideoStar::getUserId, videoStar.getUserId()));
            return false;
        }
        return true;
    }

    @Override
    public List<Long> getStarUserIds(Long videoId) {
        return this.list(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId,videoId)).stream().map(VideoStar::getUserId).collect(Collectors.toList());
    }

    @Override
    public Boolean starState(Long videoId, Long userId) {
        if(userId == null) return false;
        return this.count(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId,videoId).eq(VideoStar::getUserId,userId)) == 1;
    }
}
