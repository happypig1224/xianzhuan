package com.xianzhuan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
public class XianZhuanApplication {
    public static void main(String[] args) {
        SpringApplication.run(XianZhuanApplication.class, args);
        log.info("server started");
    }
}
