package com.qingcheng.service.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @author 李洋
 * @date 2019/11/10 14:51
 */
public class RestClientFactory {

    private static RestHighLevelClient restHighLevelClient;
    private static final String HOSTNAME = "127.0.0.1";
    private static final int PORT = 9200;

    static {
        restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(HOSTNAME, PORT, "http")));
    }

    public static RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }
}
