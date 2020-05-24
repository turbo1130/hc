package com.herocsearch.controller;

import com.herocsearch.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/19
 * Time: 21:37
 * Description: 与前端发送的请求进行交互处理
 * Version: V1.0
 */
@RestController
public class ContentController {
    private ContentService contentService;

    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * 处理搜索关键字请求
     * @param keywd 关键字
     * @return
     */
    @GetMapping(value = "/search/{wd}",produces="text/html;charset=utf-8")
    public String searchInfo(@PathVariable("wd") String keywd){
        String info = contentService.searchInfo(keywd);
        return info;
    }

    /**
     * 处理关键字和页码请求，返回分页之后的数据
     * @param keywd 关键字
     * @param page 页码
     * @return
     */
    @GetMapping(value = "/search/{wd}/{pn}",produces="text/html;charset=utf-8")
    public String searchInfoForPage(@PathVariable("wd") String keywd, @PathVariable("pn") int page){
        String info = contentService.searchInfo(keywd, page);
        return info;
    }

    /**
     * 清空es数据，该方法有极大安全问题，如果被窃取，该请求
     * 会被恶意使用，而恶意清除es数据。由于作者，为了方便
     * 清除es中的数据，es中的数据没有实际意义，所以设定了该接口。
     */
    @GetMapping(value = "/delall/es/hc",produces="text/html;charset=utf-8")
    public void delAllInfo(){
        contentService.delAllInfo();
    }
}
