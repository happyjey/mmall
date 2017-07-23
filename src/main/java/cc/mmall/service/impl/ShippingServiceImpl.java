package cc.mmall.service.impl;

import cc.mmall.common.ServerResponse;
import cc.mmall.dao.ShippingMapper;
import cc.mmall.pojo.Shipping;
import cc.mmall.service.IShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/3.
 */
@Service
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    ShippingMapper shippingMapper;

    public ServerResponse<PageInfo> list(Integer uid,int pageNum){
        int pageSize = 10;
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectListByUid(uid);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse add(Integer uid, Shipping shipping){
        shipping.setUserId(uid);
        int intCount = shippingMapper.insert(shipping);
        if (intCount > 0){
            Map resMap = Maps.newHashMap();
            resMap.put("id",shipping.getId());
            return ServerResponse.createBySuccess(resMap);
        }
        return ServerResponse.createByErrorMessage("添加地址失败");
    }

    @Override
    public ServerResponse update(Integer uid,Shipping shipping) {
        shipping.setUserId(uid);
        int intCount = shippingMapper.updateByShipping(shipping);
        if (intCount > 0){
            return ServerResponse.createBySuccess("修改地址成功");
        }
        return ServerResponse.createByErrorMessage("修改地址失败");
    }

    @Override
    public ServerResponse delete(Integer uid, Integer shippingId) {
        int intCount = shippingMapper.deleteByShippingIdUid(uid,shippingId);
        if (intCount > 0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }
}
