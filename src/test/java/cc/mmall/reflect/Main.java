package cc.mmall.reflect;

import java.lang.reflect.*;
import java.util.Scanner;

/**
 * Created by Administrator on 2017/7/16.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入全限定类名：");
        String str = scanner.next();
        try {
            Class clazz = Class.forName(str);
            Class supperClazz = clazz.getSuperclass();
            // 获取类修饰符
            String modifier = Modifier.toString(clazz.getModifiers());
            if (modifier.length() > 0) {
                System.out.print(modifier + " class" + " ");
            }
            System.out.print(clazz.getName() + " ");
            // 拼接继承关系
            if (supperClazz != null && supperClazz != Object.class) {
                System.out.print("extends " + supperClazz.getName());
            }
            System.out.print(" {");
            System.out.println();
            // 打印构造方法
            printConstractors(clazz);
            // 打印普通方法
            printMethods(clazz);
            // 打印属性
            printFields(clazz);
            System.out.println("}");
        } catch (ClassNotFoundException e) {
            System.out.println("【" + str + "】类不存在");
            return;
            //e.printStackTrace();
        }

    }

    // 打印属性
    private static void printFields(Class clazz) {
        Field[] fields = clazz.getFields();
        for (Field f : fields){
            String modifier = Modifier.toString(f.getModifiers());
            if (modifier.length() > 0){
                System.out.print("    " + modifier + " ");
            }
            System.out.print(f.getType().getName() + " " + f.getName());
            System.out.println();
        }
    }

    // 打印普通方法
    private static void printMethods(Class clazz) {
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            String modifier = Modifier.toString(m.getModifiers());
            System.out.print("    ");
            if (modifier.length() > 0) {
                System.out.print(modifier + " ");
            }
            // 打印方法返回值
            Class retType = m.getReturnType();
            System.out.print(retType.getName() + " ");
            // 打印方法名
            System.out.print(m.getName() + "(");
            // 打印方法参数
            Class<?>[] parameterTypes = m.getParameterTypes();
            for (int j = 0; j < parameterTypes.length; j++) {
                Class param = parameterTypes[j];
                if (j == 0){
                    System.out.print(param.getName());
                }else {
                    System.out.print("," + param.getName());
                }
            }
            System.out.print(")");
            System.out.println();
        }
    }

    // 打印构造方法
    private static void printConstractors(Class clazz) {
        Constructor[] constractors = clazz.getConstructors();
        for (Constructor c : constractors) {
            String modifier = Modifier.toString(c.getModifiers());
            System.out.print("    ");
            if (modifier.length() > 0) {
                System.out.print(modifier + " ");
            }
            System.out.print(c.getName() + "(");
            // 打印构造器参数
            Class[] parameterTypes = c.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class param = parameterTypes[i];
                if (i == 0) {
                    System.out.print(param.getName());
                } else {
                    System.out.print("," + param.getName());
                }
            }
            System.out.print(")");
            System.out.println();
        }
    }
}
