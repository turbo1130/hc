import com.herocsearch.pojo.Info;
import com.herocsearch.utils.CNUtils;
import com.herocsearch.utils.ESOperationUtils;
import com.herocsearch.utils.JsoupUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/19
 * Time: 21:33
 * Description: 单元测试
 * Version: V1.0
 */
public class MyTest {

    /**
     * 测试抓取百度网页的数据，并输出抓取内容
     * @throws IOException
     */
    @Test
    public void getHtmlTest() throws IOException {
        long start = System.currentTimeMillis();
        List<Info> infoList = JsoupUtils.resolveHTML("java",1);
        System.out.println(System.currentTimeMillis()-start);
        for (Info info : infoList) {
            System.out.println(info);
        }
    }

    /**
     * 测试查询es中指定的索引库含关键字的数据条数
     */
    @Test
    public void getInfoCountTest(){
        ESOperationUtils esOperationUtils = new ESOperationUtils();
        long count = esOperationUtils.getCountNumber("林俊杰");
        System.out.println(count);
    }

    /**
     * 测试添加新关键字的数据到es指定索引库中，并测试所消耗的时间
     * @throws InterruptedException
     */
    @Test
    public void addNewInfoTest() throws InterruptedException {
        ESOperationUtils esOperationUtils = new ESOperationUtils();
        long millis = System.currentTimeMillis(); // 测试时间
        int java = esOperationUtils.addNewInfos("林俊杰");
        System.out.println(System.currentTimeMillis()-millis);
        TimeUnit.SECONDS.sleep(15);
        System.out.println("成功添加了: "+java+"数据到ES");
    }

    /**
     * 测试删除es中指定索引库的数据
     */
    @Test
    public void  delAllInfoTest(){
        ESOperationUtils esOperationUtils = new ESOperationUtils();
        System.out.println(esOperationUtils.delAllInfos());
    }

    /**
     * 测试是否能够得到正确的数据
     */
    @Test
    public void getInfoTest(){
        ESOperationUtils esOperationUtils = new ESOperationUtils();
        System.out.println(esOperationUtils.getInfos("林俊杰").toString());
    }

    /**
     * 测试关键字是否能够正常检查得到正确结果
     */
    @Test
    public void checkWdTest(){
        System.out.println(CNUtils.checkWd("林俊杰"));
        System.out.println(CNUtils.checkWd("java"));
        System.out.println(CNUtils.checkWd("林俊杰java"));
        System.out.println(CNUtils.checkWd("林俊杰&！java"));
    }
}
