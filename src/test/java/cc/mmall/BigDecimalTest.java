package cc.mmall;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/6/28.
 */
public class BigDecimalTest {

    @Test
    public void test1(){
        System.out.println(0.05 + 0.01);
        System.out.println(2.01*100);
        System.out.println(123.3 / 100);
        System.out.println();
    }

    @Test
    public void test2(){
        BigDecimal a = new BigDecimal(0.05);
        BigDecimal b = new BigDecimal(0.01);
        System.out.println(a.add(b));
        System.out.println();
    }

    @Test
    public void test3(){
        BigDecimal a = new BigDecimal("0.05");
        BigDecimal b = new BigDecimal("0.01");
        System.out.println(a.add(b));
    }
}
