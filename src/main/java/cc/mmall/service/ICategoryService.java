package cc.mmall.service;

import cc.mmall.common.ServerResponse;
import cc.mmall.pojo.Category;

import java.util.List;

/**
 * Created by Administrator on 2017/6/5.
 */
public interface ICategoryService {
    ServerResponse addCategory(int parentId,String categoryName);
    ServerResponse updateCategoryName(int categoryId,String categoryName);
    ServerResponse<List<Category>> getChildrenParallelCategory(int categoryId);
    ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(int categoryId);
    ServerResponse<List<Integer>> selectCategoryAndChildById(int categoryId);
}
