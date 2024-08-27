package com.qiankun.matt.broker.topic;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @Description:
 * @Date : 2024/08/27 11:28
 * @Auther : tiankun
 */
@Slf4j
public class TopicManage {

    // 使用TreeMap来保持Topic Filter的自然排序，便于遍历
    private final NavigableMap<String, Set<String>> subscriptions = new TreeMap<>();

    /**
     * 订阅Topic Filter
     * @param clientId      客户端ID
     * @param topicFilter   主题过滤器
     */
    public void subscribe(String clientId, String topicFilter) {
        subscriptions.compute(topicFilter, (key, clients) -> {
            if (clients == null) {
                clients = new HashSet<>();
            }
            clients.add(clientId);
            return clients;
        });
        log.info("Client " + clientId + " subscribed to: " + topicFilter);
    }

    /**
     * 取消订阅
     * @param clientId
     * @param topicFilter
     */
    public void unsubscribe(String clientId, String topicFilter) {
        subscriptions.computeIfPresent(topicFilter, (key, clients) -> {
            clients.remove(clientId);
            if (clients.isEmpty()) {
                return null;
            }
            return clients;
        });
    }

    /**
     * 发布消息到指定Topic
     * @param topic
     * @return
     */
    public Set<String> topicRouter(String topic) {
        log.info("router to topic[{}] ",topic );
        // 从最具体的Topic Filter开始匹配，逐步放宽至通配符匹配
        NavigableSet<String> keys = subscriptions.navigableKeySet();
        Set<String> clientIds = Sets.newHashSet();
        for (String filter : keys) {
            if (isMatch(topic, filter)) {
                clientIds.addAll(subscriptions.get(filter));
            } else if (filter.contains("+") || filter.contains("#")) {
                break; // 已经尝试过最具体的，接下来的是更宽泛的通配符，不必继续
            }
        }
        return clientIds;
    }

    /**
     * 匹配算法，考虑了+和#的复杂情况
     * @param topic
     * @param filter
     * @return
     */
    private boolean isMatch(String topic, String filter) {
        String[] topicParts = topic.split("/");
        String[] filterParts = filter.split("/");

        if (filterParts.length != topicParts.length) {
            return false;
        }

        for (int i = 0; i < filterParts.length; i++) {
            if ("+".equals(filterParts[i])) continue; // +匹配任意单个层级
            if ("#".equals(filterParts[i])) {
                if (i < topicParts.length - 1) return false; // #必须是最后一个或者单独存在
                break; // 匹配完成，#可以代表之后的所有层级
            }
            if (!filterParts[i].equals(topicParts[i])) {
                return false;
            }
        }
        return true;
    }
}
