package com.luckyDay.video.service;

import com.qiniu.storage.model.FileInfo;

public interface QiNiuFileService {
    FileInfo getFileInfo(String url);

    String getToken();
}
