package cc.mmall.service;

import cc.mmall.common.ServerResponse;
import cc.mmall.vo.CartVo;

/**
 * Created by Administrator on 2017/6/28.
 */
public interface ICartService {
    ServerResponse<CartVo> getList(Integer uid);
    ServerResponse<CartVo> addCart(Integer productId, Integer count, Integer uid);
    ServerResponse<CartVo> updateCart(Integer productId, Integer count,Integer uid);
    ServerResponse<CartVo> deleteProduct(String productIds, Integer uid);
    ServerResponse<CartVo> checkedAll(Integer uid, Integer checked);
    ServerResponse<CartVo> checkedOne(Integer uid, Integer checked, Integer productId);
    ServerResponse<Integer> getCartProductCount(Integer uid);
}
