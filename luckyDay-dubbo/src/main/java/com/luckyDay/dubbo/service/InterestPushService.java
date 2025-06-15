package com.luckyDay.dubbo.service;


import com.luckyDay.model.user.User;
import com.luckyDay.model.user.vo.UserModel;
import com.luckyDay.model.video.Video;

import java.util.Collection;
import java.util.List;

public interface InterestPushService {

    void pushSystemStockIn(Video video);

    void pushSystemTypeStockIn(Video video);

    Collection<Long> listVideoIdByTypeId(Long typeId);

    void deleteSystemStockIn(Video video);

    void initUserModel(Long userId, List<String> labels);

    void updateUserModel(UserModel userModel);

    Collection<Long> listVideoIdByUserModel(User user);

    Collection<Long> listVideoIdByLabels(List<String> labelNames);

    void deleteSystemTypeStockIn(Video video);
}
