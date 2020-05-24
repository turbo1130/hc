package com.herocsearch.utils;


import com.herocsearch.pojo.Info;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/19
 * Time: 22:08
 * Description: 解析网站的工具类 ,爬取百度数据，其中Accept和Cookie很重要
 * 其中cookie的设置，可以先用浏览器访问一次百度首页，将请求头的cookie复制粘贴过来即可
 * Version: V1.0
 */
public class JsoupUtils {
    /**
     * href 爬取到的连接
     * title 爬取到的标题
     * content 爬取到的详细介绍内容
     */
    private static String href;
    private static String title;
    private static String content;


    /**
     * 用于解析百度网站的方法，获取需要的数据，封装为info类型的list集合并返回
     * 通过分析，百度搜索请求的url是get请求，wd是搜索的关键字，pn是当前页显示内容的起始索引
     * 百度每一页只显示10条真实的信息(去除广告)，广告与真实内容的div区别在于有无srcid属性
     * @param wd 关键字
     * @param page 页码
     * @return
     */
    public static List<Info> resolveHTML(String wd,int page) {
        List<Info> infoList = new ArrayList<>();
        if(wd==null){
            System.out.println("无值！");
            return null;
        }else {
            String url = "https://www.baidu.com/s?wd="+wd+"&pn="+(page*10);
            Connection connect = Jsoup.connect(url); // 需要连接的url
            connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            //connect.header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3314.0 Safari/537.36 SE 2.X MetaSr 1.0");
            //connect.header("Host","www.baidu.com");
            connect.header("Cookie","BAIDUID=4C2B896A1A1BEF47CFBEBB0058E12212:FG=1; PSTM=1584620157; BIDUPSID=9957C2E3FC32C5304572C98DBF951A88; BD_UPN=19314753; ispeed=1; ispeed_lsm=0; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; H_PS_PSSID=31654_1428_31671_21117_31111_31590_31661_31464_30823; BDSFRCVID=0eKOJexroG3HG05ukvPfMB11J2KK0gOTDYLEOwXPsp3LGJLVNUtkEG0Ptf8gJx_-XBNAogKK0gOTH6KF_2uxOjjg8UtVJeC6EG0Ptf8g0M5; H_BDCLCKID_SF=JRAJoK0MtKvffPFk-PI3qqIFXP6-hnjy3b77KKtabRv-JCjEL6Lay6DUyN3MWh3RymJ4_K5jMfO5SMJbDl34XlL3MPoxJpOJ5JbMatn7HxnI8ljvbURvD--g3-AqBM5dtjTO2bc_5KnlfMQ_bf--QfbQ0hOhqP-jBRIE_C82tK02hIvPKITD-tFO5eT22-usJTFJ2hcHMPoosIOSQT0h5tPB0M7u3j_DWJriaKJjBMbUoqRHXnJi0btQDPvxBf7pBHTJBh5TtUJMeCnTyR7rqqkYX4QyKMnitIv9-pPKWhQrh459XP68bTkA5bjZKxtq3mkjbPbDfn028DKujj-MDTbQDNRabK6aKC5bL6rJabC38nRDXU6q2bDeQNb7058tbC3qbpRz0DDaOCooyT3JXp0vWtv4WbbvLT7johRTWqR48x5mMUonDh83bG7MBJvdHGbG2qjO5hvvhb3O3MA-yUKmDloOW-TB5bbPLUQF5l8-sq0x0bOte-bQXH_Et6tJtbkjoKvt-5rDHJTg5DTjhPrMbf6WWMT-MTryKKOd3KthfJvC053YL6oLyG5iB5OMBanRhlRNB-3iV-OxDUvnyxAZXRoJaMQxtNRJ0DnjWtnrKfntXxQobUPUDMJ9LUkqW2cdot5yBbc8eIna5hjkbfJBQttjQn3hfIkj2CKLJC8MhI0xDTRDKICV-frb-C62aKDson5cBhcqJ-ovQTb1QTO05fRqQP5u0JCDaDocWKJJ8UbeWfvpXn-R0hbjJM7xWeJp2f522h5nhMJmb67JyRkmqJ7GWfoy523ion3vQpP-OpQ3DRoWXPIqbN7P-p5Z5mAqKl0MLPbtbb0xXj_0-nDSHH_DJT-D3J; yjs_js_security_passport=3148872e360dce82b56453aff2732c0f1b0972a8_1589893771_js; delPer=0; BD_CK_SAM=1; PSINO=1; COOKIE_SESSION=11_0_9_9_0_7_0_1_9_3_0_1_0_0_0_0_0_0_1589902076%7C9%23266694_63_1589437046%7C9; H_PS_645EC=fa84oq4ggL%2BWzkAEXpCq4lBWRW8%2FKjCYoB7P7SxW77M3jSnEpxsCoKk4PYo; BDSVRTM=1");
            Document document = null;
            try {
                document = connect.get(); //获取返回的html
            } catch (IOException e) {
                System.err.println("JsoupUtils ---> connect.get()获取异常!");
            }
            if(document != null){
                // 解析html，获取需要的数据
                Element contentLeft = document.getElementById("content_left");
                Elements srcid = contentLeft.getElementsByAttribute("srcid");
                for (Element el: srcid) {
                    Elements t = el.getElementsByClass("t");
                    for (int i = 0; i < t.size(); i++) {
                        href = t.get(i).select("a").attr("href");
                        // 处理href中没有www.baidu.com的情况
                        if (!href.contains("www.baidu.com")) {
                            href = "https://www.baidu.com" + href;
                        }
                        String getTitle = t.select("a").text();
                        StringBuilder stringBuilder = new StringBuilder(getTitle);
                        for (int j = 0; j < stringBuilder.length(); j++) {
                            if (stringBuilder.charAt(j) == '_') {
                                stringBuilder.replace(j, j + 1, " ");
                            }
                        }
                        title = stringBuilder.toString();
                    }
                    Elements cAbstract = el.getElementsByClass("c-abstract");
                    content = cAbstract.text();
                    // 处理没有content的情况
                    if (content.equals("")) {
                        content = "抱歉，暂无描述信息...";
                    }
                    // 添加到list集合中
                    infoList.add(new Info(title, href, content));
                }
            }
        }
        return infoList;
    }
}
