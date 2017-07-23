package cc.mmall.proxy;

/**
 * Created by Administrator on 2017/7/14.
 */
public class Car implements ITraffic {
    @Override
    public void move() {
        System.out.println("小轿车行驶！");
    }
}
