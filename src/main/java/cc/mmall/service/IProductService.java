package cc.mmall.service;

import cc.mmall.common.ServerResponse;
import cc.mmall.pojo.Product;
import cc.mmall.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;

/**
 * Created by Administrator on 2017/6/8.
 */
public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);
    ServerResponse updateStatus(Integer productId, Integer status);
    ServerResponse<ProductDetailVo> getDetail(Integer productId);
    ServerResponse getList(int pageNum,int pageSize);
    ServerResponse<PageInfo<Product>> productSearch(String productName, Integer productId, int pageNum, int pageSize);
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
    ServerResponse<PageInfo> productByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize,String orderBy);

}
