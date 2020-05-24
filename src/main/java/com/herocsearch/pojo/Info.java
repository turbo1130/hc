package com.herocsearch.pojo;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/19
 * Time: 21:08
 * Description: 用于存储每一条内容的pojo
 * Version: V1.0
 */
@Component
public class Info implements Serializable {
    private final static long serialVersionUID = 1130001L;
    String title;
    String href;
    String content;

    public Info() {
    }

    public Info(String title, String href, String content) {
        this.title = title;
        this.href = href;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Info{" +
                "title='" + title + '\'' +
                ", href='" + href + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
