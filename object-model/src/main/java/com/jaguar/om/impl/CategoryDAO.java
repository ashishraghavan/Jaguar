package com.jaguar.om.impl;

import com.jaguar.om.ICategory;
import com.jaguar.om.ICategoryDAO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@Component("categoryDao")
@Transactional
public class CategoryDAO extends CommonDao implements ICategoryDAO {

    //Query all categories for which parentCategory = NULL.
    private static final String queryAllParentCategories = "select distinct category from Category category where category.parentCategory = NULL";
    //Query for all categories where parentCategory = "some_category";
    private static final String queryForCategoriesWithParentCategory = "select distinct category from Category category where category.parentCategory.name =:parentCategoryName";

    @Override
    @SuppressWarnings("unchecked")
    public List<ICategory> getAllCategories(int start, int size) {
        Query query = getEntityManager()
                .createQuery(queryAllParentCategories);
        query.setFirstResult(start);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ICategory> getAllChildCategoriesForParentCategory(String name, int start, int size) {
        final Query query = getEntityManager()
                .createQuery(queryForCategoriesWithParentCategory)
                .setParameter("parentCategoryName",name)
                .setFirstResult(start)
                .setMaxResults(size);
        return query.getResultList();
    }
}
