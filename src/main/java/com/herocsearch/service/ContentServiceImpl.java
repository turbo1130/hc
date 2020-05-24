package com.herocsearch.service;

import com.alibaba.fastjson.JSON;
import com.herocsearch.exception.GetInfoCountTimeoutException;
import com.herocsearch.pojo.Info;
import com.herocsearch.utils.ESOperationUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/19
 * Time: 21:38
 * Description: 对ContentService方法的实现
 * Version: V1.0
 */
@Service
public class ContentServiceImpl implements ContentService {
    private ESOperationUtils esOperationUtils;
    private Logger logger = Logger.getLogger(ContentServiceImpl.class);
    // map用于存储所查询到的info信息，status成功失败状态码，count总数据记录数
    private Map<String,Object> map;

    @Autowired
    public void setEsOperationUtils(ESOperationUtils esOperationUtils) {
        this.esOperationUtils = esOperationUtils;
    }

    /**
     * 对只通过关键字进行查询的接口方法的具体实现。
     * 该方法，只会在前端通过搜索框发送请求的时候被执行。
     * 首先从es中获取该关键字索引条数，如果总条数小于60条，就从百度爬取10页的数据，
     * 转存到es索引中，如果总条数大于60条，那么直接从es中获取数据，存放到map中，并
     * 以json的格式返回。如果wd为空，那么直接返回status失败状态404；如果不为空并且
     * 查询成功有数据，则返回info查询到的数据，status成功状态200，count总数据记录数。
     * @param wd 关键字
     * @return
     */
    @Override
    public String searchInfo(String wd) {
        long count = esOperationUtils.getCountNumber(wd);
        logger.info("查询关键字 "+wd+" 存在 "+count+" 条数据...");
        map = new HashMap<>();
        if(!wd.isEmpty()){
            if(count < 60L){
                System.out.println("<60 "+wd);
                int newInfosCount = esOperationUtils.addNewInfos(wd);
                long time = System.currentTimeMillis();
                try{
                    while (true){
                        if(newInfosCount!=0){
                            break;
                        }else if((System.currentTimeMillis()-time) == 20000){
                            throw new GetInfoCountTimeoutException();
                        }
                    }
                }catch (GetInfoCountTimeoutException e){
                    logger.error(e);
                }
                long n_count = newInfosCount + count;
                if( (n_count) > 75){
                    List<Info> infos = esOperationUtils.getInfos(wd);
                    if(infos!=null){
                        return getInfoMapJson(infos,map,n_count);
                    }
                }
            }else {
                List<Info> infos = esOperationUtils.getInfos(wd);
                if(infos!=null){
                    return getInfoMapJson(infos,map,count);
                }
            }
        }
        map.put("status","404");
        return JSON.toJSONString(map);
    }

    /**
     * 对通过关键字和页数进行查询的接口方法的具体实现。
     * 该方法只会在前端通过分页栏的页码发送的请求时候被执行。
     * 如果wd为空，那么直接返回status失败状态404；如果不为空并且查询成功有数据，
     * 则返回info查询到的数据，status成功状态200，count总数据记录数。
     * @param wd 关键字
     * @param page 页码
     * @return
     */
    @Override
    public String searchInfo(String wd, int page) {
        map = new HashMap<>();
        if(!wd.isEmpty()){
            List<Info> infos = esOperationUtils.getInfos(wd,page);
            if(infos!=null){
                return getInfoMapJson(infos,map);
            }
        }
        map.put("status","404");
        return JSON.toJSONString(map);
    }

    /**
     * 删除es中的所有记录
     */
    @Override
    public void delAllInfo() {
        long infos = esOperationUtils.delAllInfos();
        logger.info("已删除ES所有数据："+infos+"条...");
    }

    /**
     * 方法用于将es放回的数据进行封装到map中，并返回json数据，
     * 方法中只用于封装infos和status信息到map
     * @param infos es查询返回的数据
     * @param newMap 存储信息的map
     * @return
     */
    public String getInfoMapJson(List<Info> infos, Map<String,Object> newMap){
        return getInfoMapJson(infos,newMap,-1L);
    }

    /**
     * 方法用于将infolist、count和status封装到map中，并返回json数据
     * 根据count判断是否需要封装count数据
     * @param infoList es查询返回的数据
     * @param newMap 存储信息的map
     * @param count es查询返回的数据条数
     * @return
     */
    public String getInfoMapJson(List<Info> infoList, Map<String,Object> newMap, long count){
        if(count==-1L){
            newMap.put("info",JSON.toJSONString(infoList));
            newMap.put("status","200");
            String json = JSON.toJSONString(newMap);
            return json;
        }else {
            newMap.put("info",JSON.toJSONString(infoList));
            newMap.put("count",count);
            newMap.put("status","200");
            String json = JSON.toJSONString(newMap);
            return json;
        }
    }
}
