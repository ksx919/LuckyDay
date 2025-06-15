package com.luckyDay.video.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyDay.common.utils.RedisCacheUtil;
import com.luckyDay.dubbo.service.*;
import com.luckyDay.model.video.Video;
import com.luckyDay.model.video.vo.BasePage;
import com.luckyDay.video.mapper.VideoMapper;
import com.luckyDay.video.service.*;
import com.luckyDay.video.service.TypeService;
import com.luckyDay.video.service.audit.VideoPublishAuditServiceImpl;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Autowired
    private TypeService typeService;

    @DubboReference
    private InterestPushService interestPushService;

    @DubboReference
    private UserService userService;

    @Autowired
    private VideoStarService videoStarService;

    @Autowired
    private VideoShareService videoShareService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @DubboReference
    private FavoritesService favoritesService;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoPublishAuditServiceImpl videoPublishAuditService;

    @Autowired
    private RedisTemplate redisTemplate;

    @DubboReference
    private FollowService followService;

    @DubboReference
    private FeedService feedService;

    @Autowired
    private FileService fileService;

    @Override
    public void publishVideo(Video video) {

    }

    @Override
    public void deleteVideo(Long id) {

    }

    @Override
    public IPage<Video> listByUserIdVideo(BasePage basePage, Long userId) {
        return null;
    }

    @Override
    public boolean starVideo(Long videoId) {
        return false;
    }

    @Override
    public void historyVideo(Long videoId, Long userId) throws Exception {

    }

    @Override
    public LinkedHashMap<String, List<Video>> getHistory(BasePage basePage) {
        return null;
    }

    @Override
    public Collection<Video> listVideoByFavorites(Long favoritesId) {
        return List.of();
    }

    @Override
    public boolean favoritesVideo(Long fId, Long vId) {
        return false;
    }

    @Override
    public String getAuditQueueState() {
        return "";
    }

    @Override
    public Collection<Video> followFeed(Long userId, Long lastTime) {
        return List.of();
    }

    @Override
    public void initFollowFeed(Long userId) {

    }
}
