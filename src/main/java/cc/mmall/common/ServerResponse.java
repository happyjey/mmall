package cc.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/6/2.
 */
// 保证序列化对象时，如果是null的对象，key也会消失include = JsonSerialize.Inclusion.NON_NULL
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    private ServerResponse(int status) {
        this.status = status;
    }

    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ServerResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static ServerResponse createBySuccess(){
       return new ServerResponse(ResponseCode.SUCCESS.getCode());
    }

    public static  ServerResponse createBySuccessMessage(String msg){
        return new ServerResponse(ResponseCode.SUCCESS.getCode(),msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse(ResponseCode.SUCCESS.getCode(),data);
    }

    public static <T> ServerResponse<T> createBySuccess(String msg, T data){
        return new ServerResponse(ResponseCode.SUCCESS.getCode(),msg,data);
    }

    public static ServerResponse createByError() {
        return new ServerResponse(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public static ServerResponse createByErrorMessage(String msg){
        return new ServerResponse(ResponseCode.ERROR.getCode(),msg);
    }

    public static ServerResponse createByErrorCodeMessage(int code,String msg){
        return new ServerResponse(code,msg);
    }

    @JsonIgnore
    // 使之不在json序列化结果之中
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
