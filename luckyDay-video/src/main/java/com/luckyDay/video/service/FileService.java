package com.luckyDay.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyDay.model.video.File;

public interface FileService extends IService<File> {
    Object save(String fileKey, Long userId);

    File getFileTrustUrl(Long fileId);
}
