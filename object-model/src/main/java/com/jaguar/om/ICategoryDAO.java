package com.jaguar.om;

import java.util.List;

public interface ICategoryDAO extends ICommonDao {
    List<ICategory> getAllCategories(int start, int size);
    List<ICategory> getAllChildCategoriesForParentCategory(final String name, int start, int size);
}
