package com.luckyDay.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyDay.model.video.VideoShare;

import java.util.List;

public interface VideoShareService extends IService<VideoShare> {

    //添加分享记录
    boolean share(VideoShare videoShare);

    //获取分享用户id
    List<Long> getShareUserId(Long videoId);
}
