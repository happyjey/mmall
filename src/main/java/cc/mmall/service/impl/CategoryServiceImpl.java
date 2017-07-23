package cc.mmall.service.impl;

import cc.mmall.common.ServerResponse;
import cc.mmall.dao.CategoryMapper;
import cc.mmall.pojo.Category;
import cc.mmall.service.ICategoryService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/6/5.
 */
@Service
public class CategoryServiceImpl implements ICategoryService {

    Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(int parentId, String categoryName) {
        if (StringUtils.isBlank(categoryName)){
            ServerResponse.createByErrorMessage("分类名称不能为空");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int resultCount = categoryMapper.insert(category);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("分类添加失败");
        }
        return ServerResponse.createBySuccessMessage("分类添加成功");
    }

    public ServerResponse updateCategoryName(int categoryId,String categoryName){
        if (categoryId == 0 || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("分类参数缺失");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int updateCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (updateCount == 0){
            return ServerResponse.createByErrorMessage("分类名称更新失败");
        }
        return ServerResponse.createBySuccessMessage("分类名称修改成功");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(int categoryId){
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        return ServerResponse.createBySuccess(list);
    }

    public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(int categoryId){
        Set<Category> categorySet = new HashSet<>();
        this._deepCategory(categorySet,categoryId);
        List<Integer> categoryIdList = Lists.newArrayList();
        for (Category category : categorySet){
            categoryIdList.add(category.getId());
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    public ServerResponse<List<Integer>> selectCategoryAndChildById(int categoryId){
        List<Integer> categoryIdList = getCategoryAndDeepChildrenCategory(categoryId).getData();
        categoryIdList.add(categoryId);
        return ServerResponse.createBySuccess(categoryIdList);
    }

    /**
     * categorySet参数传递的是对象的内存地址值，所以不需要return
     * @param categorySet
     * @param categoryId
     */
    private void _deepCategory(Set<Category> categorySet, int categoryId){
        List<Category> listCategory = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        categorySet.addAll(listCategory);
        logger.info("listCategory:"+listCategory.toString());
        logger.info("list:"+categorySet.toString());
        for(Category categoryItem : listCategory){
            _deepCategory(categorySet, categoryItem.getId());
        }
    }
}
