import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 测试文件
 *
 * @author BaoQiang
 * @version 2.0
 * @Create at 2016/11/4 22:28
 */
public class Hello {

    @Test
    public void test(){
//        String s = "org/wltea/analyzer/dic/main2012.dic";
        String s = "me/1.txt";
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(s);
//        InputStream in = this.getClass().getResourceAsStream(s);
        System.out.println(in);

        String url = "http://127.0.0.1:8765/?q=monitor";
    }

    @Test
    public void test2(){
        System.out.println(Charset.defaultCharset());
    }
}
