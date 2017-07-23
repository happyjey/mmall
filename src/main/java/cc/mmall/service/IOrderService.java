package cc.mmall.service;

import cc.mmall.common.ServerResponse;
import cc.mmall.pojo.Order;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

import java.util.Map;

/**
 * Created by Administrator on 2017/7/9.
 */
public interface IOrderService {
    ServerResponse pay(Integer uid, Long orderNo, String path);

    ServerResponse alipayCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer uid, Long orderNo);

    Long generateOrderNo();

    ServerResponse<Long> create(Integer uid, Integer shippingId, Integer paymentType);

    ServerResponse cancel(Integer uid, Long orderNo);

    ServerResponse getOrderCartProduct(Integer uid);

    ServerResponse detail(Integer uid, Long orderNo);

    ServerResponse<PageInfo> getList(Integer uid, int pageNum, int pageSize);

    ServerResponse manageList(int pageNum, int pageSize);

    ServerResponse manageDetail(Long orderNo);

    ServerResponse manageSendGoods(Long orderNo);
}
