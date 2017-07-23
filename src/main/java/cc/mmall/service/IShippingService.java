package cc.mmall.service;

import cc.mmall.common.ServerResponse;
import cc.mmall.pojo.Shipping;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/7/3.
 */
public interface IShippingService {

    ServerResponse<PageInfo> list(Integer uid,int pageNum);

    ServerResponse<Integer> add(Integer uid, Shipping shipping);

    ServerResponse update(Integer uid,Shipping shipping);

    ServerResponse delete(Integer uid, Integer shippingId);
}
