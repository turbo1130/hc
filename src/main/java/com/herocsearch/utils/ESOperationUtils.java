package com.herocsearch.utils;

import com.alibaba.fastjson.JSON;
import com.herocsearch.config.ESConfig;
import com.herocsearch.pojo.Info;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/19
 * Time: 22:07
 * Description: ES相关操作的工具类
 * Version: V1.0
 */
public class ESOperationUtils {
    private Logger logger = Logger.getLogger(ESOperationUtils.class);
    private BulkRequest bulkRequest;
    private Lock lock = new ReentrantLock();
    private volatile int addInfoCount;

    /**
     * 本方法为添加新数据到es中，通过多线程来完成抓取数据的操作
     * 规定抓取数据只抓取10页的数据信息，转存到es中指定的索引库中
     * 通过加锁和解锁的方式，防止线程安全的发生，通过CountDownLatch类，
     * 规定次数减为0时，被唤醒继续执行await之后的任务。
     * 通过es客户端的BulkRequest类完成信息的批处理存入。
     * 该方法批量添加数据之后，并返回添加数据的总条数。
     * @param wd 关键字
     * @return
     */
    public int addNewInfos(String wd){
        addInfoCount = 0;
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0 ; i < 10; i++) {
            int finalI = i;
            new Thread(()->{
                lock.lock();
                BulkResponse bulkResponse = null;
                List<Info> infoList = JsoupUtils.resolveHTML(wd, finalI);
                logger.info(getMethodName("addNewInfos",String.class)+Thread.currentThread().getName()+" 百度搜索数据抓取完成");
                RestHighLevelClient client = ESConfig.getClient();
                bulkRequest = new BulkRequest();
                bulkRequest.timeout("2m");
                for (int j = 0; j < infoList.size(); j++) {
                    bulkRequest.add(
                            new IndexRequest(HCConstant.HC_INDEX)
                                    .source(JSON.toJSONString(infoList.get(j)), XContentType.JSON));
                }
                try {
                    bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                } catch (IOException e) {
                    logger.error(getMethodName("addNewInfos",String.class)+"/n"+Thread.currentThread().getName()+" 向ES添加数据失败！");
                }finally {
                    if( !bulkResponse.hasFailures() ){
                        addInfoCount++;
                        countDownLatch.countDown();
                        logger.info(getMethodName("addNewInfos",String.class)+Thread.currentThread().getName()+" 向ES中添加数据成功");
                    }
                    try {
                        client.close();
                    } catch (IOException e) {
                        logger.error(getMethodName("addNewInfos",String.class)+"ES客户端关闭异常！");
                    }
                    lock.unlock();
                }
            }, "Thread"+i ).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error(getMethodName("addNewInfos",String.class)+"/n"+"await()方法InterruptedException");
        }
        return (addInfoCount*10);
    }


    /**
     * 删除ES中指定索引库的所有数据，并返回删除的数据条数
     * @return
     */
    public long delAllInfos(){
        RestHighLevelClient client = ESConfig.getClient();
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(HCConstant.HC_INDEX);
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        deleteByQueryRequest.setQuery(matchAllQueryBuilder);
        BulkByScrollResponse scrollResponse = null;
        try {
            scrollResponse = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error(getMethodName("delAllInfos")+"删除ES数据失败！IOException");
        }finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error(getMethodName("delAllInfos")+"ES客户端关闭异常！");
            }
        }
        long deleted = scrollResponse.getStatus().getDeleted();
        return deleted;
    }

    /**
     * 只接收关键字去搜索匹配与关键字的信息，默认获取第一页的数据
     * 第一页就是获取es中指定索引库通过匹配后数据中前10的数据，
     * es中搜索后的数据默认排序权重最高的排在前。查询结果以List集合
     * 返回
     * @param wd 关键字
     * @return
     */
    public List<Info> getInfos(String wd){
        return getInfos(wd,1);
    }

    /**
     * 通过关键字和页码去es中获取匹配的数据。
     * 查询的关键字做高亮处理之后，将数据封装到info对象中，
     * 并返回info类型的list集合
     * @param wd 查询的关键字
     * @param page 页数
     * @return
     */
    public List<Info> getInfos(String wd, int page){
        List<Info> infoList = new ArrayList<>();
        RestHighLevelClient client = ESConfig.getClient();
        SearchRequest searchRequest = new SearchRequest(HCConstant.HC_INDEX);

        // 获取搜索资源建造对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 通过检查关键字，并返回符合查询要求的QueryBuilder对象
        sourceBuilder.query(checkText(wd));
        // 设置高亮对象
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(HCConstant.HC_INDEX_TITLE);
        // highlightBuilder.requireFieldMatch(false); // 关闭多个高亮
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");
        sourceBuilder.highlighter(highlightBuilder);
        // 根据页码，设置获取从from索引开始的数据，条数为size个
        sourceBuilder.from((page-1)*10);
        sourceBuilder.size(10);
        sourceBuilder.timeout(new TimeValue(10,TimeUnit.SECONDS)); // 超时设置

        // 将设置好的搜索资源建造对象传递给请求做处理
        searchRequest.source(sourceBuilder);
        SearchResponse search=null;
        try {
             search = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error(getMethodName("getInfosForPage",String.class,Integer.class)+"删除ES数据失败！IOException");
        }
        // 如果返回的结果不为null，那么就将高亮的title遍历出来，覆盖原本的title数据
        if(search!=null){
            SearchHit[] hits = search.getHits().getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField title = highlightFields.get(HCConstant.HC_INDEX_TITLE);
                if(title!=null){
                    Text[] fragments = title.fragments();
                    String n_title = "";
                    for (Text fragment : fragments) {
                        n_title += fragment;
                    }
                    sourceAsMap.put(HCConstant.HC_INDEX_TITLE,n_title);
                }
                infoList.add(new Info((String) sourceAsMap.get(HCConstant.HC_INDEX_TITLE),
                                      (String) sourceAsMap.get(HCConstant.HC_INDEX_HREF),
                                      (String) sourceAsMap.get(HCConstant.HC_INDEX_CONTENT))
                            );
            }
        }
        try {
            client.close();
        } catch (IOException e) {
            logger.error(getMethodName("getInfosForPage",String.class,Integer.class)+"ES客户端关闭异常！");
        }
        return infoList;
    }

    /**
     * 获取es中指定索引库中数据的总条数。
     * 通过关键字查询含有该关键字的数据，并对数据统计条数，并返回。
     * @param wd 关键字
     * @return
     */
    public long getCountNumber(String wd){
        RestHighLevelClient client = ESConfig.getClient();
        CountRequest countRequest = new CountRequest(HCConstant.HC_INDEX);
        countRequest.query(checkText(wd));
        CountResponse countResponse = null;
        try {
            countResponse = client.count(countRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error(getMethodName("getCountNumber",String.class)+"获取数据失败！");
        }finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error(getMethodName("getCountNumber",String.class)+"ES客户端关闭异常！");
            }
        }
        if(countResponse!=null){
            long count = countResponse.getCount();
            return count;
        }
        return 0;
    }

    /**
     * 该方法是将全中文关键字区分出来执行不同的查询条件，
     * 如果关键字全为中文，则通过构建布尔条件查询，以达到
     * 满足所有中文字被全部绑定一块查询匹配到正确结果。
     * 英文或其他关键字直接通过模糊查询即可。返回QueryBuilder对象
     * @param wd 关键字
     * @return
     */
    public QueryBuilder checkText(String wd){
        if(CNUtils.checkWd(wd)){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            String[] split = wd.split("");
            for (String ch: split) {
                boolQueryBuilder.must(QueryBuilders.termQuery(HCConstant.HC_INDEX_TITLE,ch));
            }
            return boolQueryBuilder;
        }else {
            MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery(HCConstant.HC_INDEX_TITLE, wd);
            return queryBuilder;
        }
    }

    /**
     * 传递进来的方法名这个参数，通过反射的形式获取该方法的信息
     * 并返回该方法的修饰符、返回类型、全限方法名
     * 用于日志，定位方法
     * @param methodName 表示方法名
     * @return
     */
    public String getMethodName(String methodName){
         return getMethodName(methodName, null);
    }

    /**
     * 传递进来的方法名和参数类型类参数，通过反射的形式获取该方法的信息
     * 并返回该方法的修饰符、返回类型、全限方法名
     * 用于日志，定位方法
     * @param methodName 需要被反射获取信息的方法名
     * @param parameterTypes 该方法名对应的参数类
     * @return
     */
    public String getMethodName(String methodName, Class<?>... parameterTypes){
        String[] split = null;
        String str = null;
        try {
            if(parameterTypes!=null){
                split = this.getClass().getDeclaredMethod(methodName, parameterTypes).toString().split(" ");
                str = split[split.length-1];
            }else {
                split = this.getClass().getDeclaredMethod(methodName).toString().split(" ");
                str = split[split.length-1];
            }
        } catch (Exception e) {
            logger.error("com.herocsearch.utils.ESOperationUtils.getMethodName --> "+"NoSuchMethodException异常！");
        }
        return str;
    }
}
