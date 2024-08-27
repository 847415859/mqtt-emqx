package com.qiankun.matt.broker.context;

import com.qiankun.matt.broker.security.DefaultAuthSecurity;
import com.qiankun.matt.broker.security.IAuthSecurity;
import com.qiankun.matt.broker.session.MQTTSessionContext;
import com.qiankun.matt.broker.topic.TopicManage;
import lombok.Getter;

/**
 * @Description:
 * @Date : 2024/08/27 11:38
 * @Auther : tiankun
 */
public class ApplicationContext {
    @Getter
    private static final IAuthSecurity authSecurity = new DefaultAuthSecurity();


    @Getter
    private static final MQTTSessionContext context = new MQTTSessionContext();


    @Getter
    private static final TopicManage topicManage = new TopicManage();

}
