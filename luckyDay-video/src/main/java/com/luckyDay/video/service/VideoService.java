package com.luckyDay.video.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyDay.model.video.Video;
import com.luckyDay.model.video.vo.BasePage;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public interface VideoService extends IService<Video> {

    void publishVideo(Video video);

    void deleteVideo(Long id);

    IPage<Video> listByUserIdVideo(BasePage basePage, Long userId);

    boolean starVideo(Long videoId);

    void historyVideo(Long videoId, Long userId) throws Exception;

    LinkedHashMap<String, List<Video>> getHistory(BasePage basePage);

    Collection<Video> listVideoByFavorites(Long favoritesId);

    boolean favoritesVideo(Long fId, Long vId);

    String getAuditQueueState();

    Collection<Video> followFeed(Long userId, Long lastTime);

    void initFollowFeed(Long userId);
}
