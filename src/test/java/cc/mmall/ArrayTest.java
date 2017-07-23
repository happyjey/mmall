package cc.mmall;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Administrator on 2017/7/20.
 */
public class ArrayTest {
    public static void main(String[] args) {
        ArrayList<String> arrayList = new ArrayList<String>(10);
        for (int i=0;i<10;i++){
            arrayList.add(i+""+i+""+i);
        }
        arrayList.remove(3);
        for (String str : arrayList){
            System.out.println(str);
        }
    }
}
