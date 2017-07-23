package cc.mmall.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvocationHandler implements InvocationHandler {

    Object target = null;

    public MyInvocationHandler(Object target) {
        this.target = target;
    }

    /**
     *
     * @param proxy   代理类的实例
     * @param method  被代理对象的方法
     * @param args    方法的参数
     * @return
     * @throws Throwable
     * ---------------------------------------
     * target : 被代理类的实例，需要从外部实例化后传入
     * ---------------------------------------
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("before calling: "+method);
        method.invoke(target,args);
        System.out.println("after calling: "+method);
        return null;
    }
}
