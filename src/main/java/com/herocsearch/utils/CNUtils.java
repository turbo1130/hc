package com.herocsearch.utils;

/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/23
 * Time: 13:39
 * Description: 关键字是否为中文的判断工具
 * Version: V1.0
 */
public class CNUtils {
    /**
     * 检查关键字wd是否为全中文，是则返回true，不是则返回false
     * @param wd
     * @return
     */
    public static boolean checkWd(String wd)
    {
        int n = 0;
        for(int i = 0; i < wd.length(); i++) {
            n = (int)wd.charAt(i);
            if(!(19968 <= n && n <40869)) {
                return false;
            }
        }
        return true;
    }
}
