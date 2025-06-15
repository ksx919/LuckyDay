package com.luckyDay.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyDay.common.config.LocalCache;
import com.luckyDay.common.config.QiNiuConfig;
import com.luckyDay.model.exception.BaseException;
import com.luckyDay.model.video.File;
import com.luckyDay.video.mapper.FileMapper;
import com.luckyDay.video.service.FileService;
import com.luckyDay.video.service.QiNiuFileService;
import com.qiniu.storage.model.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Autowired
    private QiNiuFileService qiNiuFileService;

    @Override
    public Object save(String fileKey, Long userId) {
        // 判断文件
        final FileInfo videoFileInfo = qiNiuFileService.getFileInfo(fileKey);

        if (videoFileInfo == null){
            throw new IllegalArgumentException("参数不正确");
        }

        final File videoFile = new File();
        String type = videoFileInfo.mimeType;
        videoFile.setFileKey(fileKey);
        videoFile.setFormat(type);
        videoFile.setType(type.contains("video") ? "视频" : "图片");
        videoFile.setUserId(userId);
        videoFile.setSize(videoFileInfo.fsize);
        save(videoFile);

        return videoFile.getId();
    }

    @Override
    public File getFileTrustUrl(Long fileId) {
        File file = getById(fileId);
        if (Objects.isNull(file)) {
            throw new BaseException("未找到该文件");
        }
        final String s = UUID.randomUUID().toString();
        LocalCache.put(s,true);
        String url = QiNiuConfig.CNAME + "/" + file.getFileKey();

        if (url.contains("?")){
            url = url+"&uuid="+s;
        }else {
            url = url+"?uuid="+s;
        }
        file.setFileKey(url);
        return file;
    }
}
