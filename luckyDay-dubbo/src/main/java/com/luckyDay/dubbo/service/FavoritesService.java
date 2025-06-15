package com.luckyDay.dubbo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyDay.model.user.Favorites;

import java.util.List;

public interface FavoritesService extends IService<Favorites> {
    //删除收藏夹
    void remove(Long id,Long userId);

    //根据用户获取收藏夹
    List<Favorites> listByUserId(Long userId);

    //获取收藏夹下的所有视频id
    List<Long> listVideoIds(Long favoritesId,Long userId);

    //收藏视频
    boolean favorites(Long fId,Long vId);

    //收藏状态
    Boolean favoritesState(Long videoId,Long userId);

    void exist(Long userId,Long defaultFavoritesId);
}
