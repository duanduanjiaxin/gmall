package com.atguigu.gmall.passpord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall")
public class GmallPasspordWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPasspordWebApplication.class, args);
    }

}
