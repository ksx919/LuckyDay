package com.luckyDay.recommend.service;

import com.luckyDay.model.user.User;
import com.luckyDay.model.user.vo.UserModel;
import com.luckyDay.model.video.Video;

import java.util.Collection;
import java.util.List;

public interface InterestPushService {
    //将视频推入标签库
    void pushSystemStockIn(Video video);

    //添加分类库，用于后续随机推送
    void pushSystemTypeStockIn(Video video);

    //根据分类随机推送视频
    Collection<Long> listVideoIdByTypeId(Long typeId);

    //删除标签内视频
    void deleteSystemStockIn(Video video);

    //用户初始化->订阅分类
    void initUserModel(Long userId, List<String> labels);

    /**
     * 用户模型修改概率 : 可分批次发送
     * 修改场景:
     * 1.观看浏览量到达总时长1/5  +1概率
     * 2.观看浏览量未到总时长1/5 -0.5概率
     * 3.点赞视频  +2概率
     * 4.收藏视频  +3概率
     */
    void updateUserModel(UserModel userModel);

    //兴趣推送
    Collection<Long> listVideoIdByUserModel(User user);

    //根据标签获取视频id
    Collection<Long> listVideoIdByLabels(List<String> labelNames);

    //删除分类库中的视频
    void deleteSystemTypeStockIn(Video video);
}
