package cc.mmall.dao;

import cc.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUidProductId(@Param("uid") Integer uid, @Param("productId") Integer productId);

    List<Cart> selectCartByUid(Integer uid);
    List<Cart> selectCheckedCartByUid(Integer uid);

    int deleteByProductIdsUid(@Param("uid") Integer uid, @Param("productIds") String[] productIds);

    int updateCheckedByUid(@Param("uid") Integer uid, @Param("checked") Integer checked);

    int updateCheckedByUidProductId(@Param("uid") Integer uid, @Param("checked") Integer checked,@Param("productId") Integer productId);

    int selectProductCountByUid(Integer uid);

    int deleteByUid(Integer uid);
}