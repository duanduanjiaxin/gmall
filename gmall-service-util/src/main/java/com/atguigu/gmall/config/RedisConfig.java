package com.atguigu.gmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

   /*
        1. RedisUtil 工具类配置好，放入到spring 容器中

     */

    // 获取到host，port ，timeOut ,database
    // :disabled 如果在配置文件中没有找到host ，则表示给host 一个默认值！
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.timeOut:10000}")
    private int timeOut;


    @Bean
    public RedisUtil getRedisUtil(){
        if ("disabled".equals(host)){
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        // 初始化initJedisPool 获取Jedis
        redisUtil.initJedisPool(host,port,timeOut,database);
        return redisUtil;
    }


}
