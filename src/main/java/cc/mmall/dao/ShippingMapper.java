package cc.mmall.dao;

import cc.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByShippingIdUid(@Param("uid") Integer uid, @Param("shippingId") Integer shippingId);

    int updateByShipping(Shipping shipping);

    List<Shipping> selectListByUid(Integer uid);
}