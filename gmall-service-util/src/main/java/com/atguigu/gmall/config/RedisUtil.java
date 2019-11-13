package com.atguigu.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {


    // 创建jedisPool 连接池
    private JedisPool jedisPool;

    // 初始化操作
    public void initJedisPool(String host,int port,int timeOut,int database){

        // 配置jedisPool 的配置参数类
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 获取到jedis以后自检
        jedisPoolConfig.setTestOnBorrow(true);
        // 设置阻塞队列
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 设置剩余数
        jedisPoolConfig.setMinIdle(10);
        // 设置最大数
        jedisPoolConfig.setMaxTotal(200);
        // 设置等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);

        jedisPool=new JedisPool(jedisPoolConfig,host,port,timeOut);
    }
    // 表示获取Jedis 对象
    public Jedis getJedis(){
        return jedisPool.getResource();
    }

}
