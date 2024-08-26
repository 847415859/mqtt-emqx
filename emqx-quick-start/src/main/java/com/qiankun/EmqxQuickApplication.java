package com.qiankun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmqxQuickApplication {

   private static final Logger logger = LoggerFactory.getLogger(EmqxQuickApplication.class);
   public static void main(String[] args) {
       SpringApplication.run(EmqxQuickApplication.class, args);
       logger.info("==================== emqx-example 启动完成 ====================");
   }

}