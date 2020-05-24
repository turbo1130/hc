package com.herocsearch.service;

/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/19
 * Time: 21:38
 * Description: 对信息处理的接口，可实现该类，进行拓展
 * Version: V1.0
 */
public interface ContentService {
    /**
     * 用于只通过关键字进行查询的抽象方法
     * @param wd 关键字
     * @return
     */
    String searchInfo(String wd);

    /**
     * 用于只通过关键字和页数进行查询的抽象方法
     * @param wd 关键字
     * @param page
     * @return
     */
    String searchInfo(String wd, int page);

    /**
     * 删除es中所有索引数据的抽象方法
     */
    void delAllInfo();
}
