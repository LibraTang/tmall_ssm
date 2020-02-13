package com.how2java.tmall.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.logging.Logger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:*.xml")
public class TestRedis {
    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger log = Logger.getLogger(TestRedis.class.getName());

    @Test
    public void test() {
        redisTemplate.opsForValue().set("name", "libra");
        log.info("value: " + redisTemplate.opsForValue().get("name"));
    }
}
