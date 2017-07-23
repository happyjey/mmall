package cc.mmall.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 生成代理实例，并调用了move()方法
 */
public class Main {
    public static void main(String[] args) {

        // 这里指定被代理类
        Car car = new Car();
        Class<?> clazz = car.getClass();
        InvocationHandler h = new MyInvocationHandler(car);
        // 以下是一次性生成代理
        ITraffic carPoxy = (ITraffic) Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),h);
        System.out.println("运行结果为：");
        carPoxy.move();

        //这里可以通过运行结果证明carPoxy是Proxy的一个实例，这个实例实现了Subject接口
        System.out.println(carPoxy instanceof Proxy);

        //这里可以看出carPoxy的Class类是$Proxy0,这个$Proxy0类继承了Proxy，实现了ITraffic接口
        System.out.println("subject的Class类是："+carPoxy.getClass().toString());

        System.out.print("carPoxy中的属性有：");
        Field[] field=carPoxy.getClass().getDeclaredFields();
        for(Field f:field){
            System.out.print(f.getName()+", ");
        }

        System.out.print("\n"+"carPoxy中的方法有：");
        Method[] method=carPoxy.getClass().getDeclaredMethods();
        for(Method m:method){
            System.out.print(m.getName()+", ");
        }

        System.out.println("\n"+"carPoxy的父类是："+carPoxy.getClass().getSuperclass());

        System.out.print("\n"+"carPoxy实现的接口是：");
        Class<?>[] interfaces=carPoxy.getClass().getInterfaces();
        for(Class<?> i:interfaces){
            System.out.print(i.getName()+", ");
        }

    }
}
