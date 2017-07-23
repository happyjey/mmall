package cc.mmall.service.impl;

import cc.mmall.common.Const;
import cc.mmall.common.ResponseCode;
import cc.mmall.common.ServerResponse;
import cc.mmall.dao.CategoryMapper;
import cc.mmall.dao.ProductMapper;
import cc.mmall.pojo.Category;
import cc.mmall.pojo.Product;
import cc.mmall.service.ICategoryService;
import cc.mmall.service.IProductService;
import cc.mmall.util.DateTimeUtil;
import cc.mmall.util.PropertiesUtil;
import cc.mmall.vo.ProductDetailVo;
import cc.mmall.vo.ProductListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/8.
 */
@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;
    private Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品参数不正确");
        }
        // sub_images第一张图作为主图
        if (StringUtils.isNotBlank(product.getSubImages())) {
            String[] mainImageArray = product.getSubImages().split(",");
            if (mainImageArray.length > 0) {
                product.setMainImage(mainImageArray[0]);
            }
        }
        // 判断添加或修改
        if (product.getId() == null) {
            int resultCount = productMapper.updateByPrimaryKey(product);
            if (resultCount == 0) {
                return ServerResponse.createByErrorMessage("产品更新失败");
            }
            return ServerResponse.createBySuccessMessage("产品更新成功");
        } else {
            int resultCount = productMapper.insertSelective(product);
            if (resultCount == 0) {
                return ServerResponse.createByErrorMessage("产品添加失败");
            }
            return ServerResponse.createBySuccessMessage("产品添加成功");
        }
    }

    /**
     * 此处参数使用包装类型Integer而不是int，主要是方便null判断
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse updateStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int resultCount = productMapper.updateByPrimaryKeySelective(product);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("产品状态更新失败");
        }
        return ServerResponse.createBySuccessMessage("产品状态更新成功");
    }

    public ServerResponse<ProductDetailVo> getDetail(Integer productId){
        if (productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if (productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("商品不存在");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("商品不存在或已下线");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse getList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo>  productListVoList = new ArrayList<ProductListVo>();
        for (Product productItem : productList){
            productListVoList.add(assembleProductListVo(productItem));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<PageInfo<Product>> productSearch(String productName, Integer productId, int pageNum, int pageSize){
        if (StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectByNameAndId(productName,productId);
        List productListvoList = Lists.newArrayList();
        for (Product productItem : productList){
            productListvoList.add(assembleProductListVo(productItem));
        }
        PageInfo<Product> pageInfo = new PageInfo<Product>(productList);
        pageInfo.setList(productListvoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<PageInfo> productByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy){
        if (StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();
        if (categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)){
                return ServerResponse.createBySuccess();
            }
            if (category != null){
                categoryIdList = iCategoryService.selectCategoryAndChildById(categoryId).getData();
            }
        }
        if (StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        logger.info("categoryIdList:"+categoryIdList.toString());
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(orderBy)){
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(keyword,categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList){
            productListVoList.add(assembleProductListVo(productItem));
        }
        PageInfo pageInfo = new PageInfo<Product>(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setStatus(product.getStatus());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setCategoryid(product.getCategoryId());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setPrice(product.getPrice());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return productListVo;
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setName(product.getName());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        productDetailVo.setParentCategoryId(category.getParentId());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }
}
