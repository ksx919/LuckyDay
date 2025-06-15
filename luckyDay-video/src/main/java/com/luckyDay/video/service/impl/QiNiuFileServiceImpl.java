package com.luckyDay.video.service.impl;

import com.luckyDay.common.config.QiNiuConfig;
import com.luckyDay.video.service.QiNiuFileService;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QiNiuFileServiceImpl implements QiNiuFileService {

    @Autowired
    private QiNiuConfig qiNiuConfig;


    @Override
    public FileInfo getFileInfo(String url) {
        Configuration cfg = new Configuration(Region.region0());
        final Auth auth = qiNiuConfig.buildAuth();
        final String bucket = qiNiuConfig.getBucketName();

        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            FileInfo fileInfo = bucketManager.stat(bucket, url);
            return fileInfo;
        } catch (QiniuException ex) {
            System.err.println(ex.response.toString());
        }
        return null;
    }

    @Override
    public String getToken() {
        return qiNiuConfig.videoUploadToken();
    }
}
