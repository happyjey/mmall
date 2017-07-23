package cc.mmall.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/6/4.
 */
public interface Const {

    String CURRENT_USER = "current_user";

    String EMAIL = "email";
    String USERNAME = "username";

    interface Role{
        int ROLE_CUSTOMER = 0; // 普通用户
        int ROLE_ADMIN = 1; // 管理员用户
    }

    interface Cart{
        int CHECKED = 1;
        int UN_CHECKED = 0;

        String LIMI_NUM_SUCCESS = "LIMI_NUM_SUCCESS";
        String LIMI_NUM_FAIL = "LIMI_NUM_FAIL";
    }

    enum ProductStatusEnum{
        ON_SALE(1,"在售");

        private int code;
        private String value;

        ProductStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }

    interface ProductListOrderBy{
        Set PRICE_ASC_DESC = Sets.newHashSet("price_asc","price_desc");
    }

    enum OrderStatusEnum{
        CLOSED(0,"已取消"),
        NO_PAID(10,"未付款"),
        PAID(20,"已付款"),
        SHIPPINGED(40,"已发货"),
        TRAD_SUCCESS(50,"交易成功"),
        TRAD_CLOSED(60,"交易关闭");

        private int code;
        private String value;

        OrderStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        public static OrderStatusEnum codeOf(int code){
            for (OrderStatusEnum orderStatusEnum : values()){
                if (orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有对应的枚举");
        }
    }

    interface AlipayCallback{
        // 支付成功
        public static final String TRADE_SUCCESS = "TRADE_SUCCESS";

        // 支付宝回调成功响应
        public static String RESPONSE_SUCCESS = "success";
        // 支付宝回调失败响应
        public static String RESPONSE_FAILED = "failed";
    }

    enum PayPlatformEnum{
        ALIPAY(1,"支付宝"),
        WX(2,"微信");

        private int code;
        private String value;

        PayPlatformEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        public static PayPlatformEnum codeOf(int code){
            for (PayPlatformEnum payPlatformEnum : values()){
                if (payPlatformEnum.getCode() == code){
                    return payPlatformEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }
}
