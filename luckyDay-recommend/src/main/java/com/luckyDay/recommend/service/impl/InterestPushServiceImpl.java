package com.luckyDay.recommend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyDay.common.utils.RedisCacheUtil;
import com.luckyDay.dubbo.service.InterestPushService;
import com.luckyDay.dubbo.service.TypeService;
import com.luckyDay.model.constants.RedisConstant;
import com.luckyDay.model.user.User;
import com.luckyDay.model.user.vo.Model;
import com.luckyDay.model.user.vo.UserModel;
import com.luckyDay.model.video.Video;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@DubboService
@Service
public class InterestPushServiceImpl implements InterestPushService {
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @DubboReference
    private TypeService typeService;

    @Autowired
    private RedisTemplate redisTemplate;

    final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Async
    public void pushSystemStockIn(Video video) {
        final List<String> labels = video.buildLabel();
        final Long videoId = video.getId();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            RedisSetCommands setCommands = connection.setCommands();
            for (String label : labels) {
                String key = RedisConstant.SYSTEM_STOCK + label;
                byte[] keyBytes = redisTemplate.getKeySerializer().serialize(key);
                byte[] valueBytes = redisTemplate.getValueSerializer().serialize(videoId);
                setCommands.sAdd(keyBytes, valueBytes);
            }
            return null;
        });
    }

    @Override
    @Async
    public void pushSystemTypeStockIn(Video video) {
        final Long typeId = video.getTypeId();
        redisCacheUtil.sSet(RedisConstant.SYSTEM_TYPE_STOCK + typeId, video.getId());
    }

    @Override
    public Collection<Long> listVideoIdByTypeId(Long typeId) {
        //根据分类随机推送十个视频
        final List<Object> list = redisTemplate.opsForSet().randomMembers(RedisConstant.SYSTEM_TYPE_STOCK + typeId,10);
        final HashSet<Long> result = new HashSet<>();
        for (Object obj : list){
            if(obj!=null){
                result.add(Long.valueOf(obj.toString()));
            }
        }
        return result;
    }

    @Override
    @Async
    public void deleteSystemStockIn(Video video) {
        final List<String> labels = video.buildLabel();
        final Long videoId = video.getId();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
           RedisSetCommands setCommands = connection.setCommands();
           for(String label : labels){
               String key = RedisConstant.SYSTEM_STOCK + label;
               byte[] keyBytes = redisTemplate.getKeySerializer().serialize(key);
               byte[] valueBytes = redisTemplate.getValueSerializer().serialize(videoId);
               setCommands.sRem(keyBytes, valueBytes);
           }
           return null;
        });
    }

    @Override
    @Async
    public void initUserModel(Long userId, List<String> labels) {
        final String key = RedisConstant.USER_MODEL + userId;
        Map<Object, Object> modelMap = new HashMap<>();
        if(!ObjectUtils.isEmpty(labels)){
            final int size = labels.size();
            double probabilityValue = 100 / size;
            for (String labelName : labels) {
                modelMap.put(labelName, probabilityValue);
            }
        }
        redisCacheUtil.del(key);
        redisCacheUtil.hmset(key, modelMap);
    }

    @Override
    @Async
    public void updateUserModel(UserModel userModel) {
        final Long userId = userModel.getUserId();
        if (userId != null) {
            final List<Model> models = userModel.getModels();
            String key = RedisConstant.USER_MODEL + userId;
            Map<Object, Object> modelMap = redisCacheUtil.hmget(key);

            if(modelMap == null){
                modelMap = new HashMap<>();
            }
            for(Model model : models){
                if(modelMap.containsKey(model.getLabel())){
                    modelMap.put(model.getLabel(), Double.valueOf(modelMap.get(model.getLabel()).toString()) + model.getScore());
                    final Object o = modelMap.get(model.getLabel());
                    if(o == null || Double.valueOf(o.toString()) > 0.0){
                        modelMap.remove(o);
                    }
                } else {
                    modelMap.put(model.getLabel(), model.getScore());
                }
            }

            final int labelSize = modelMap.keySet().size();
            for(Object o : modelMap.keySet()){
                modelMap.put(o,(Double.valueOf(modelMap.get(o).toString()) + labelSize)/labelSize);
            }
            redisCacheUtil.hmset(key, modelMap);
        }
    }

    @Override
    public Collection<Long> listVideoIdByUserModel(User user) {
        //创建结果集
        Set<Long> videoIds = new HashSet<>(10);

        if(user != null){
            final Long userId = user.getId();
            final Map<Object, Object> modelMap = redisCacheUtil.hmget(RedisConstant.USER_MODEL + userId);
            if(!ObjectUtils.isEmpty(modelMap)) {
                final String[] probabilityArray = initProbabilityArray(modelMap);
                final Boolean sex = user.getSex();
                final Random randomObject = new Random();
                final ArrayList<String> labelNames = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    String labelName = probabilityArray[randomObject.nextInt(probabilityArray.length)];
                    labelNames.add(labelName);
                }
                String t = RedisConstant.SYSTEM_STOCK;
                List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    RedisSetCommands setCommands = connection.setCommands();
                    for (String labelName : labelNames) {
                        String key = t + labelName;
                        setCommands.sRandMember(key.getBytes());
                    }
                    return null;
                });
                Set<Long> ids = list.stream().filter(id -> id != null).map(id -> Long.valueOf(id.toString())).collect(Collectors.toSet());
                String key2 = RedisConstant.HISTORY_VIDEO;

                List simpIds = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    RedisStringCommands stringCommands = connection.stringCommands();
                    for (Long id : ids) {
                        stringCommands.get((key2 + id + ":" + userId).getBytes());
                    }
                    return null;
                });
                simpIds = (List) simpIds.stream().filter(o -> !ObjectUtils.isEmpty(o)).collect(Collectors.toList());
                if (!ObjectUtils.isEmpty(simpIds)) {
                    for (Object simpId : simpIds) {
                        final Long l = Long.valueOf(simpId.toString());
                        if (ids.contains(l)) {
                            ids.remove(l);
                        }
                    }
                }

                videoIds.addAll(ids);

                final Long aLong = randomVideoId(sex);
                if (aLong != null) {
                    videoIds.add(aLong);
                }
                return videoIds;
            }
        }
        final List<String> labels = typeService.random10Labels();
        final ArrayList<String> labelNames = new ArrayList<>();
        int size = labels.size();
        final Random random = new Random();
        // 获取随机的标签
        for (int i = 0; i < 10; i++) {
            final int randomIndex = random.nextInt(size);
            labelNames.add(RedisConstant.SYSTEM_STOCK + labels.get(randomIndex));
        }
        // 获取videoId
        final List<Object> list = redisCacheUtil.sRandom(labelNames);
        if (!org.springframework.util.ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id ->!org.springframework.util.ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toSet());
        }

        return videoIds;
    }

    @Override
    public Collection<Long> listVideoIdByLabels(List<String> labelNames) {
        final ArrayList<String> labelKeys = new ArrayList<>();
        for (String labelName : labelNames) {
            labelKeys.add(RedisConstant.SYSTEM_STOCK + labelName);
        }
        Set<Long> videoIds = new HashSet<>();
        final List<Object> list = redisCacheUtil.sRandom(labelKeys);
        if (!org.springframework.util.ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id ->!org.springframework.util.ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toSet());
        }
        return videoIds;
    }

    @Override
    public void deleteSystemTypeStockIn(Video video) {
        final Long typeId = video.getTypeId();
        redisCacheUtil.setRemove(RedisConstant.SYSTEM_TYPE_STOCK + typeId,video.getId());
    }

    public Long randomVideoId(Boolean sex) {
        String key = RedisConstant.SYSTEM_STOCK + (sex ? "美女" : "宠物");
        final Object o = redisCacheUtil.sRandom(key);
        if (o!=null){
            return Long.parseLong(o.toString());
        }
        return null;
    }

    // 初始化概率数组 -> 保存的元素是标签
    public String[] initProbabilityArray(Map<Object, Object> modelMap) {
        // key: 标签  value：概率
        Map<String, Integer> probabilityMap = new HashMap<>();
        int size = modelMap.size();
        final AtomicInteger n = new AtomicInteger(0);
        modelMap.forEach((k, v) -> {
            // 防止结果为0,每个同等加上标签数
            int probability = (((Double) v).intValue() + size) / size;
            n.getAndAdd(probability);
            probabilityMap.put(k.toString(), probability);
        });
        final String[] probabilityArray = new String[n.get()];

        final AtomicInteger index = new AtomicInteger(0);
        // 初始化数组
        probabilityMap.forEach((labelsId, p) -> {
            int i = index.get();
            int limit = i + p;
            while (i < limit) {
                probabilityArray[i++] = labelsId;
            }
            index.set(limit);
        });
        return probabilityArray;
    }
}
