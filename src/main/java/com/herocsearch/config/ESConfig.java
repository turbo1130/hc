package com.herocsearch.config;

import com.herocsearch.utils.HCConstant;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;


/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/19
 * Time: 21:17
 * Description: 配置获取ES客户端对象
 * Version: V1.0
 */
public class ESConfig {

    /**
     * 获取ES客户端对象
     * @return
     */
    public static RestHighLevelClient getClient(){
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(HCConstant.ES_URL,HCConstant.ES_PORT,"http")));
    }

}
