package cc.mmall.util;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/6/28.
 */
public class BigDecimalUtil {
    private BigDecimalUtil(){

    }

    /**
     * 加
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal add(Double v1,Double v2){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.add(b2);
    }

    /**
     * 减
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal sub(Double v1,Double v2){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.subtract(b2);
    }

    /**
     * 乘
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal mul(Double v1,Double v2){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.multiply(b2);
    }

    /**
     * 除
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal div(Double v1,Double v2){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP); // 四舍五入，保留两位小数
    }


}
