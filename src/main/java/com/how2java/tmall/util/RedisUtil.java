package com.how2java.tmall.util;

import com.sun.beans.decoder.ValueObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisUtil {
    private static final Logger logger = LogManager.getLogger(RedisUtil.class);

    private RedisTemplate<Serializable, Object> redisTemplate;

    public RedisTemplate<Serializable, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<Serializable, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 批量删除对应的value
     * 可变长参数，可以传多个String参数
     *
     * @param keys
     */
    public void remove(String... keys) {
        for(String key : keys) {
            logger.info("batches delete value by key");
            remove(key);
        }
    }

    /**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(String pattern) {
        Set<Serializable> keys = redisTemplate.keys((pattern));
        if(keys.size() > 0) {
            logger.info("batches delete keys");
            redisTemplate.delete(keys);
        }
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(String key) {
        if(exists(key)) {
            logger.info("delete value by key");
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应value
     *
     * @param key
     * @return
     */
    public boolean exists(String key) {
        logger.info("exists value");
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        Object result = null;
        result = redisTemplate.opsForValue().get(key);
        logger.info("get value by key");
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, Object value) {
        boolean result = false;
        try {
            logger.info("set key: " + key + ", value: " + value);
            redisTemplate.opsForValue().set(key, value);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public boolean set(String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            logger.info("set key: " + key + ", value: " + value + ", expireTime: " + expireTime);
            redisTemplate.opsForValue().set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }
}
