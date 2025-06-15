package com.luckyDay.video.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.luckyDay.common.config.LocalCache;
import com.luckyDay.common.config.QiNiuConfig;
import com.luckyDay.common.utils.Result;
import com.luckyDay.model.user.UserHolder;
import com.luckyDay.model.video.Setting;
import com.luckyDay.model.video.File;
import com.luckyDay.video.service.FileService;
import com.luckyDay.video.service.SettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/luckyjourney/file")
public class FileController implements InitializingBean {

    @Autowired
    FileService fileService;

    @Autowired
    QiNiuConfig qiNiuConfig;

    @Autowired
    SettingService settingService;

    /**
     * 保存到文件表
     * @return
     */
    @PostMapping
    public Result save(String fileKey){

        return Result.ok().data(fileService.save(fileKey, UserHolder.get()));
    }

    @GetMapping("/getToken")
    public Result token(String type){

        return Result.ok().data(qiNiuConfig.uploadToken(type));
    }

    @GetMapping("/{fileId}")
    public void getUUid(HttpServletRequest request, HttpServletResponse response, @PathVariable Long fileId) throws IOException {

     /*   String ip = request.getHeader("referer");
        if (!LocalCache.containsKey(ip)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }*/
        // 如果不是指定ip调用的该接口，则不返回
        File url = fileService.getFileTrustUrl(fileId);
        response.setContentType(url.getType());
        response.sendRedirect(url.getFileKey());
    }

    @PostMapping("/auth")
    public void auth(@RequestParam(required = false) String uuid, HttpServletResponse response) throws IOException {
        if (uuid == null || LocalCache.containsKey(uuid) == null){
            response.sendError(401);
        }else {
            LocalCache.rem(uuid);
            response.sendError(200);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Setting setting = settingService.list((Wrapper<Setting>) null).get(0);
        for (String s : setting.getAllowIp().split(",")) {
            LocalCache.put(s,true);
        }
    }
}
