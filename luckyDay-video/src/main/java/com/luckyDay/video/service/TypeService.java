package com.luckyDay.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyDay.model.video.Type;

import java.util.List;

public interface TypeService extends IService<Type> {

    List<String> getLabels(Long typeId);

    List<String> random10Labels();
}
