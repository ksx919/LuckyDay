package com.luckyDay.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyDay.common.config.LocalCache;
import com.luckyDay.common.config.QiNiuConfig;
import com.luckyDay.common.utils.FileUtil;
import com.luckyDay.common.utils.RedisCacheUtil;
import com.luckyDay.dubbo.service.*;
import com.luckyDay.model.constants.AuditStatus;
import com.luckyDay.model.constants.RedisConstant;
import com.luckyDay.model.exception.BaseException;
import com.luckyDay.model.user.User;
import com.luckyDay.model.user.UserHolder;
import com.luckyDay.model.user.vo.UserModel;
import com.luckyDay.model.user.vo.UserVO;
import com.luckyDay.model.video.*;
import com.luckyDay.model.video.vo.BasePage;
import com.luckyDay.video.mapper.VideoMapper;
import com.luckyDay.video.service.*;
import com.luckyDay.video.service.TypeService;
import com.luckyDay.video.service.audit.VideoPublishAuditServiceImpl;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        final Long userId = UserHolder.get();
        Video oldVideo = null;
        // 不允许修改视频
        final Long videoId = video.getId();
        if (videoId != null) {
            // url不能一致
            oldVideo = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId).eq(Video::getUserId, userId));
            if (!(video.buildVideoUrl()).equals(oldVideo.buildVideoUrl()) || !(video.buildCoverUrl().equals(oldVideo.buildCoverUrl()))) {
                throw new BaseException("不能更换视频源,只能修改视频信息");
            }
        }
        // 判断对应分类是否存在
        final Type type = typeService.getById(video.getTypeId());
        if (type == null) {
            throw new BaseException("分类不存在");
        }
        // 校验标签最多不能超过5个
        if (video.buildLabel().size() > 5) {
            throw new BaseException("标签最多只能选择5个");
        }

        // 修改状态
        video.setAuditStatus(AuditStatus.PROCESS);
        video.setUserId(userId);

        boolean isAdd = videoId == null ? true : false;

        // 校验
        video.setYv(null);

        if (!isAdd) {
            video.setVideoType(null);
            video.setLabelNames(null);
            video.setUrl(null);
            video.setCover(null);
        } else {

            // 如果没设置封面,我们帮他设置一个封面
            if (ObjectUtils.isEmpty(video.getCover())) {
                video.setCover(fileService.generatePhoto(video.getUrl(), userId));
            }

            video.setYv("YV" + UUID.randomUUID().toString().replace("-", "").substring(8));
        }

        // 填充视频时长 (若上次发布视频不存在Duration则会尝试获取)
        if (isAdd || !StringUtils.hasLength(oldVideo.getDuration())) {
            final String uuid = UUID.randomUUID().toString();
            LocalCache.put(uuid, true);
            try {
                Long url = video.getUrl();
                if (url == null || url == 0) url = oldVideo.getUrl();
                final String fileKey = fileService.getById(url).getFileKey();
                final String duration = FileUtil.getVideoDuration(QiNiuConfig.CNAME + "/" + fileKey + "?uuid=" + uuid);
                video.setDuration(duration);
            } finally {
                LocalCache.rem(uuid);
            }
        }

        this.saveOrUpdate(video);

        final VideoTask videoTask = new VideoTask();
        videoTask.setOldVideo(video);
        videoTask.setVideo(video);
        videoTask.setIsAdd(isAdd);
        videoTask.setOldState(isAdd ? true : video.getOpen());
        videoTask.setNewState(true);
        videoPublishAuditService.audit(videoTask, false);
    }

    @Override
    public void deleteVideo(Long id) {
        if (id == null) {
            throw new BaseException("删除指定的视频不存在");
        }

        final Long userId = UserHolder.get();
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, id).eq(Video::getUserId, userId));
        if (video == null) {
            throw new BaseException("删除指定的视频不存在");
        }
        final boolean b = removeById(id);
        if (b) {
            // 解耦
            new Thread(() -> {
                // 删除分享量 点赞量
                videoShareService.remove(new LambdaQueryWrapper<VideoShare>().eq(VideoShare::getVideoId, id).eq(VideoShare::getUserId, userId));
                videoStarService.remove(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId, id).eq(VideoStar::getUserId, userId));
                interestPushService.deleteSystemStockIn(video);
                interestPushService.deleteSystemTypeStockIn(video);
            }).start();
        }
    }

    @Override
    public IPage<Video> listByUserIdVideo(BasePage basePage, Long userId) {
        return null;
    }

    @Override
    public boolean starVideo(Long videoId) {
        final Video video = getById(videoId);
        if (video == null) throw new BaseException("指定视频不存在");

        final VideoStar videoStar = new VideoStar();
        videoStar.setVideoId(videoId);
        videoStar.setUserId(UserHolder.get());
        final boolean result = videoStarService.starVideo(videoStar);
        updateStar(video, result ? 1L : -1L);
        // 获取标签
        final List<String> labels = video.buildLabel();

        final UserModel userModel = UserModel.buildUserModel(labels, videoId, 1.0);
        interestPushService.updateUserModel(userModel);

        return result;
    }

    @Override
    @Async
    public void historyVideo(Long videoId, Long userId) throws Exception {
        String key = RedisConstant.HISTORY_VIDEO + videoId + ":" + userId;
        final Object o = redisCacheUtil.get(key);
        if (o == null) {
            redisCacheUtil.set(key, videoId, RedisConstant.HISTORY_TIME);
            final Video video = getById(videoId);
            video.setUser(userService.getInfo(video.getUserId()));
            video.setTypeName(typeService.getById(video.getTypeId()).getName());
            redisCacheUtil.zadd(RedisConstant.USER_HISTORY_VIDEO + userId, new Date().getTime(), video, RedisConstant.HISTORY_TIME);
            updateHistory(video, 1L);
        }
    }

    @Override
    public LinkedHashMap<String, List<Video>> getHistory(BasePage basePage) {
        final Long userId = UserHolder.get();
        String key = RedisConstant.USER_HISTORY_VIDEO + userId;
        final Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(key, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) {
            return new LinkedHashMap<>();
        }
        List<Video> temp = new ArrayList<>();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final LinkedHashMap<String, List<Video>> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<Object> typedTuple : typedTuples) {
            final Date date = new Date(typedTuple.getScore().longValue());
            final String format = simpleDateFormat.format(date);
            if (!result.containsKey(format)) {
                result.put(format, new ArrayList<>());
            }
            final Video video = (Video) typedTuple.getValue();
            result.get(format).add(video);
            temp.add(video);
        }
        setUserVoAndUrl(temp);

        return result;
    }

    @Override
    public Collection<Video> listVideoByFavorites(Long favoritesId) {
        final List<Long> videoIds = favoritesService.listVideoIds(favoritesId, UserHolder.get());
        if (ObjectUtils.isEmpty(videoIds)) {
            return Collections.EMPTY_LIST;
        }
        final Collection<Video> videos = listByIds(videoIds);
        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public boolean favoritesVideo(Long fId, Long vId) {
        final Video video = getById(vId);
        if (video == null) {
            throw new BaseException("指定视频不存在");
        }
        final boolean favorites = favoritesService.favorites(fId, vId);
        updateFavorites(video, favorites ? 1L : -1L);

        final List<String> labels = video.buildLabel();

        final UserModel userModel = UserModel.buildUserModel(labels, vId, 2.0);
        interestPushService.updateUserModel(userModel);

        return favorites;
    }

    @Override
    public String getAuditQueueState() {
        return videoPublishAuditService.getAuditQueueState() ? "快速" : "慢速";
    }

    @Override
    public Collection<Video> followFeed(Long userId, Long lastTime) {
        // 是否存在
        Set<Long> set = redisTemplate.opsForZSet()
                .reverseRangeByScore(RedisConstant.IN_FOLLOW + userId,
                        0, lastTime == null ? new Date().getTime() : lastTime, lastTime == null ? 0 : 1, 5);
        if (ObjectUtils.isEmpty(set)) {
            // 可能只是缓存中没有了,缓存只存储7天内的关注视频,继续往后查看关注的用户太少了,不做考虑 - feed流必然会产生的问题
            return Collections.EMPTY_LIST;
        }

        // 这里不会按照时间排序，需要手动排序
        final Collection<Video> videos = list(new LambdaQueryWrapper<Video>().in(Video::getId, set).orderByDesc(Video::getGmtCreated));

        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public void initFollowFeed(Long userId) {
        // 获取所有关注的人
        final Collection<Long> followIds = followService.getFollow(userId, null);
        feedService.initFollowFeed(userId, followIds);
    }

    public void updateStar(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("start_count = start_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getStartCount, video.getStartCount());
        update(video, updateWrapper);
    }

    public void updateHistory(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("history_count = history_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getHistoryCount, video.getHistoryCount());
        update(video, updateWrapper);
    }

    public void updateFavorites(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("favorites_count = favorites_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getFavoritesCount, video.getFavoritesCount());
        update(video, updateWrapper);
    }

    public void setUserVoAndUrl(Collection<Video> videos) {
        if (!ObjectUtils.isEmpty(videos)) {
            Set<Long> userIds = new HashSet<>();
            final ArrayList<Long> fileIds = new ArrayList<>();
            for (Video video : videos) {
                userIds.add(video.getUserId());
                fileIds.add(video.getUrl());
                fileIds.add(video.getCover());
            }
            final Map<Long, File> fileMap = fileService.listByIds(fileIds).stream().collect(Collectors.toMap(File::getId, Function.identity()));
            final Map<Long, User> userMap = userService.list(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
            for (Video video : videos) {
                final UserVO userVO = new UserVO();
                final User user = userMap.get(video.getUserId());
                userVO.setId(video.getUserId());
                userVO.setNickName(user.getNickName());
                userVO.setDescription(user.getDescription());
                userVO.setSex(user.getSex());
                video.setUser(userVO);
                final File file = fileMap.get(video.getUrl());
                video.setVideoType(file.getFormat());
            }
        }

    }
}
