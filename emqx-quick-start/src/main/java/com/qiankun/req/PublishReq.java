package com.qiankun.req;

import lombok.Data;

/**
 * PublishReq
 */
@Data
public class PublishReq {
    private String topic;
    private String sendMessage;
}
