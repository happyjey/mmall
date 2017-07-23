package cc.mmall.service.impl;

import cc.mmall.common.Const;
import cc.mmall.common.ResponseCode;
import cc.mmall.common.ServerResponse;
import cc.mmall.dao.CartMapper;
import cc.mmall.dao.ProductMapper;
import cc.mmall.pojo.Cart;
import cc.mmall.pojo.Product;
import cc.mmall.pojo.User;
import cc.mmall.service.ICartService;
import cc.mmall.util.BigDecimalUtil;
import cc.mmall.util.PropertiesUtil;
import cc.mmall.vo.CartProductVo;
import cc.mmall.vo.CartVo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Administrator on 2017/6/28.
 */
@Service
public class CartServiceImpl implements ICartService{

    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductMapper productMapper;

    public ServerResponse<CartVo> getList(Integer uid) {
        return ServerResponse.createBySuccess(getCartVo(uid));
    }

    public ServerResponse<CartVo> addCart(Integer productId, Integer count, Integer uid){
        if (productId == null || count == null || count < 0){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUidProductId(uid,productId);
        if (cart == null){
            // 新增
            Cart cartItem = new Cart();
            cartItem.setUserId(uid);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartItem);
        }else{
            // 更新
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return ServerResponse.createBySuccess(getCartVo(uid));
    }

    public ServerResponse<CartVo> updateCart(Integer productId, Integer count,Integer uid){
        if (productId == null || count == null || count < 0){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUidProductId(uid,productId);
        if (cart != null){
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return ServerResponse.createBySuccess(getCartVo(uid));
    }

    public ServerResponse<CartVo> deleteProduct(String productIds, Integer uid){
        if (StringUtils.isBlank(productIds)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        String[] productIdArray = productIds.split(",");
        cartMapper.deleteByProductIdsUid(uid,productIdArray);
        return ServerResponse.createBySuccess(getCartVo(uid));
    }

    public ServerResponse<CartVo> checkedAll(Integer uid, Integer checked){
        cartMapper.updateCheckedByUid(uid,checked);
        return ServerResponse.createBySuccess(getCartVo(uid));
    }
    public ServerResponse<CartVo> checkedOne(Integer uid, Integer checked, Integer productId){
        cartMapper.updateCheckedByUidProductId(uid,checked,productId);
        return ServerResponse.createBySuccess(getCartVo(uid));
    }

    public ServerResponse<Integer> getCartProductCount(Integer uid){
        if (uid == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectProductCountByUid(uid));
    }

    private CartVo getCartVo(Integer uid){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUid(uid);
        if (CollectionUtils.isEmpty(cartList)){
            return null;
        }
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        CartProductVo cartProductVo;
        Product product;
        BigDecimal cartTotalPrice = new BigDecimal("0");
        Boolean allChecked = true;
        for (Cart cartItem : cartList){
            cartProductVo = new CartProductVo();
            cartProductVo.setId(cartItem.getId());
            cartProductVo.setChecked(cartItem.getChecked());
            cartProductVo.setUserId(uid);
            cartProductVo.setProductId(cartItem.getProductId());
            product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (product != null){
                cartProductVo.setProductName(product.getName());
                cartProductVo.setProductMainImage(product.getMainImage());
                cartProductVo.setProductSubtitle(product.getSubtitle());
                cartProductVo.setProductStatus(product.getStatus());
                cartProductVo.setProductStock(product.getStock());
                cartProductVo.setProductPrice(product.getPrice());
                cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity().doubleValue()));
                // 判断库存
                if (cartItem.getQuantity() > product.getStock()){
                    cartProductVo.setLimitQuantity(Const.Cart.LIMI_NUM_FAIL);
                    // 更新购买车数量
                    Cart updateCart = new Cart();
                    updateCart.setId(cartItem.getId());
                    updateCart.setQuantity(product.getStock());
                    cartMapper.updateByPrimaryKeySelective(updateCart);
                    cartProductVo.setQuantity(product.getStock());
                }else{
                    cartProductVo.setLimitQuantity(Const.Cart.LIMI_NUM_SUCCESS);
                    cartProductVo.setQuantity(cartItem.getQuantity());
                }
            }
            cartProductVoList.add(cartProductVo);
            if (cartItem.getChecked() == Const.Cart.CHECKED){
                cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
            }else {
                allChecked = false;
            }
        }
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(allChecked);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }


}
