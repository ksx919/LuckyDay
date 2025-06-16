package com.luckyDay.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyDay.model.video.VideoStar;

import java.util.List;

public interface VideoStarService extends IService<VideoStar> {
    //视频点赞
    boolean starVideo(VideoStar videoStar);

    //获取视频的点赞用户id
    List<Long> getStarUserIds(Long videoId);

    //获取用户点赞状态
    Boolean starState(Long videoId, Long userId);
}
