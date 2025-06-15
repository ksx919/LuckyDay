package com.luckyDay.video.controller;

import com.luckyDay.common.utils.Result;
import com.luckyDay.model.user.UserHolder;
import com.luckyDay.model.video.Video;
import com.luckyDay.model.video.limit.Limit;
import com.luckyDay.model.video.vo.BasePage;
import com.luckyDay.video.service.QiNiuFileService;
import com.luckyDay.video.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/luckyjourney/video")
public class VideoController {
    @Autowired
    private VideoService videoService;


    @Autowired
    private QiNiuFileService fileService;

    /**
     * 获取文件上传token
     * @return
     */
    @GetMapping("/token")
    public Result getToken(){
        return Result.ok().data(fileService.getToken());
    }

    /**发布视频/修改视频
     * @param video
     * @return
     */
    @PostMapping
    @Limit(limit = 5,time = 3600L,msg = "发布视频一小时内不可超过5次")
    public Result publishVideo(@RequestBody @Validated Video video){
        videoService.publishVideo(video);
        return Result.ok().message("发布成功,请等待审核");
    }

    /**
     * 删除视频
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteVideo(@PathVariable Long id){
        videoService.deleteVideo(id);
        return Result.ok().message("删除成功");
    }

    /**
     * 查看用户所管理的视频 -稿件管理
     * @param basePage
     * @return
     */
    @GetMapping
    public Result listVideo(BasePage basePage){
        return Result.ok().data(videoService.listByUserIdVideo(basePage,UserHolder.get()));
    }


    /**
     * 点赞视频
     */
    @PostMapping("/star/{id}")
    public Result starVideo(@PathVariable Long id){
        String msg = "已点赞";
        if (!videoService.starVideo(id)) {
            msg = "取消点赞";
        }
        return Result.ok().message(msg);
    }

    /**
     * 添加浏览记录
     * @return
     */
    @PostMapping("/history/{id}")
    public Result addHistory(@PathVariable Long id) throws Exception {
        videoService.historyVideo(id, UserHolder.get());
        return Result.ok();
    }

    /**
     * 获取用户的浏览记录
     * @return
     */
    @GetMapping("/history")
    public Result getHistory(BasePage basePage){
        return Result.ok().data(videoService.getHistory(basePage));
    }

    /**
     * 获取收藏夹下的视频
     */
    @GetMapping("/favorites/{favoritesId}")
    public Result listVideoByFavorites(@PathVariable Long favoritesId){
        return Result.ok().data(videoService.listVideoByFavorites(favoritesId));
    }

    /**
     * 收藏视频
     * @param fId
     * @param vId
     * @return
     */
    @PostMapping("/favorites/{fId}/{vId}")
    public Result favoritesVideo(@PathVariable Long fId,@PathVariable Long vId){
        String msg = videoService.favoritesVideo(fId,vId) ? "已收藏" : "取消收藏";
        return Result.ok().message(msg);
    }

    /**
     * 返回当前审核队列状态
     * @return
     */
    @GetMapping("/audit/queue/state")
    public Result getAuditQueueState(){
        return Result.ok().message(videoService.getAuditQueueState());
    }


    /**
     * 推送关注的人视频 拉模式
     * @param lastTime 滚动分页
     * @return
     */
    @GetMapping("/follow/feed")
    public Result followFeed(@RequestParam(required = false) Long lastTime) throws ParseException {
        final Long userId = UserHolder.get();

        return Result.ok().data(videoService.followFeed(userId,lastTime));
    }

    /**
     * 初始化收件箱
     * @return
     */
    @PostMapping("/init/follow/feed")
    public Result initFollowFeed(){
        final Long userId = UserHolder.get();
        videoService.initFollowFeed(userId);
        return Result.ok();
    }

}
