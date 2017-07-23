package cc.mmall.service.impl;

import cc.mmall.common.Const;
import cc.mmall.common.ServerResponse;
import cc.mmall.dao.*;
import cc.mmall.pojo.*;
import cc.mmall.service.ICartService;
import cc.mmall.service.IOrderService;
import cc.mmall.util.BigDecimalUtil;
import cc.mmall.util.DateTimeUtil;
import cc.mmall.util.FTPUtil;
import cc.mmall.util.PropertiesUtil;
import cc.mmall.vo.*;
import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/9.
 */
@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private ICartService iCartService;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse pay(Integer uid, Long orderNo, String path) {
        Map<String,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUidOrderNo(uid, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = orderNo.toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("扫码支付，订单号：").append(orderNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(orderNo).append("商品总价").append(totalAmount).toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.selectByUidOrderNo(uid, orderNo);
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        for (OrderItem orderItem : orderItemList) {
            goodsDetailList.add(GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(), BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(), orderItem.getQuantity()));
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png",
                        response.getOutTradeNo());
                logger.info("qrPath:" + qrPath);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(qrPath);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("二维码FTP上传异常",e);
                    e.printStackTrace();
                }
                logger.info("qrPath:"+qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    @Override
    public ServerResponse alipayCallback(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");
        // 1. 判断订单是否存在
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        // 2. 订单状态为已支付或已完成
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createByErrorMessage("支付宝重复调用");
        }
        // 3. 支付宝返回成功时修改订单状态
        if (Const.AlipayCallback.TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setUpdateTime(new Date());
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        // 4. 所有的支付宝回调都插入至pay_info表
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse queryOrderPayStatus(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByUidOrderNo(uid,orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    /**
     * 生成订单号
     * @return Long
     */
    @Override
    public Long generateOrderNo() {
        return new Date().getTime();
    }

    /**
     * 生成订单
     * @param uid
     * @param shippingId
     * @param paymentType
     * @return
     */
    @Override
    @Transactional
    public ServerResponse create(Integer uid, Integer shippingId, Integer paymentType) {
        Long orderNo = this.generateOrderNo();
        CartVo cartVo = iCartService.getList(uid).getData();
        if (cartVo == null){
            return ServerResponse.createByErrorMessage("购物车中没有商品.");
        }
        logger.info("cartVo:{}",cartVo.toString());
        ServerResponse<List<OrderItem>> serverResponse = this.assembleOrderItem(cartVo,orderNo,uid);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = serverResponse.getData();
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            logger.info("BigDecimalUtil.add({},{}),payment{}",payment.doubleValue(),orderItem.getTotalPrice().doubleValue(),payment);
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        Order order = this.assembleOrder(uid,orderNo,shippingId,payment,paymentType);
        orderMapper.insert(order);
        // 减少库存
        this.reduceProductStock(orderItemList);
        // 清空购物车
        this.clearCart(uid);
        // 返回订单相关信息
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse cancel(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByUidOrderNo(uid,orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createByErrorMessage("订单已完成无法取消");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CLOSED.getCode());
        int resCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (resCount > 0){
            return ServerResponse.createBySuccess();
        }else{
            return ServerResponse.createByErrorMessage("取消订单失败");
        }
    }

    /**
     * 获取订单购物车商品信息
     * @param uid
     * @return
     * 1. 获取购物车选中商品
     * 2. 计算选中商品总价
     * 3. 图片域名
     */
    @Override
    public ServerResponse getOrderCartProduct(Integer uid) {
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        List<Cart> cartList = cartMapper.selectCheckedCartByUid(uid);
        ServerResponse<List<OrderItem>> serverResponse = this.getCartOrderItem(uid,cartList);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = serverResponse.getData();
        BigDecimal totalPrice = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        OrderProductVo orderProductVo = new OrderProductVo();
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(totalPrice);
        return ServerResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServerResponse detail(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByUidOrderNo(uid,orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByUidOrderNo(uid, orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<PageInfo> getList(Integer uid,int pageNum,int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUid(uid);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,uid);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer uid){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (uid == null){
                // 管理员列表
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.selectByUidOrderNo(uid,order.getOrderNo());
            }
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer uid,List<Cart> cartList){
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (Cart cart : cartList){
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"不在售卖状态");
            }
            if (product.getStock() < cart.getQuantity()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"库存不足");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setTotalPrice(BigDecimalUtil.mul(cart.getQuantity().doubleValue(),product.getPrice().doubleValue()));
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(PropertiesUtil.getProperty("ftp.server.http.prefix"));
            orderItem.setUserId(uid);
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    private Order assembleOrder(Integer uid,Long orderNo,Integer shippingId,BigDecimal payment,Integer paymentType){
        Order order = new Order();
        order.setUserId(uid);
        order.setOrderNo(orderNo);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(paymentType);
        order.setStatus(Const.OrderStatusEnum.NO_PAID.getCode());
        return order;
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setUserId(order.getUserId());
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PayPlatformEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(0);
        orderVo.setShippingId(order.getShippingId());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null){
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }
        if (CollectionUtils.isNotEmpty(orderItemList)){
            List<OrderItemVo> orderItemVoList = Lists.newArrayList();
            for (OrderItem orderItem : orderItemList){
                orderItemVoList.add(this.assembleOrderItemVo(orderItem));
            }
            orderVo.setOrderItemVoList(orderItemVoList);
        }
        return orderVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private void clearCart(Integer uid) {
        cartMapper.deleteByUid(uid);
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKey(product);
        }
    }

    private ServerResponse<List<OrderItem>> assembleOrderItem(CartVo cartVo,Long orderNo,Integer uid){
        List<OrderItem> orderItemList = Lists.newArrayList();
        for (CartProductVo cartProductVo : cartVo.getCartProductVoList()){
            if (cartProductVo.getProductStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
                return ServerResponse.createByErrorMessage("商品【"+cartProductVo.getProductName()+"】未开始发售");
            }
            if (cartProductVo.getChecked() == Const.Cart.CHECKED){
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo(orderNo);
                orderItem.setUserId(uid);
                orderItem.setProductId(cartProductVo.getProductId());
                orderItem.setProductName(cartProductVo.getProductName());
                orderItem.setProductImage(cartProductVo.getProductMainImage());
                orderItem.setCurrentUnitPrice(cartProductVo.getProductPrice());
                orderItem.setQuantity(cartProductVo.getQuantity());
                orderItem.setTotalPrice(cartProductVo.getProductTotalPrice());
                orderItemList.add(orderItem);
            }
        }
        if (CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("购物车中没有商品");
        }
        orderItemMapper.batchInsert(orderItemList);
        logger.info("orderItemList{}"+orderItemList.toString());
        return ServerResponse.createBySuccess(orderItemList);
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    // backend
    public ServerResponse<PageInfo> manageList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectList();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
    public ServerResponse<OrderVo> manageDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }
    public ServerResponse<OrderVo> manageSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.SHIPPINGED.getCode()){
            return ServerResponse.createByErrorMessage("订单已经发货");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.SHIPPINGED.getCode());
        updateOrder.setSendTime(new Date());
        orderMapper.updateByPrimaryKeySelective(updateOrder);
        return ServerResponse.createBySuccessMessage("发货成功");
    }


}
