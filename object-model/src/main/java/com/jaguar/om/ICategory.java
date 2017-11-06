package com.jaguar.om;

import java.util.Set;

/**
 * A category object having association with
 */
public interface ICategory extends ICommonObject {
    void setName(final String name);
    void setDescription(final String description);
    void setParentCategory(final ICategory parentCategory);
    void setFilters(final Set<IFilter> filters);
    void setChildCategories(final Set<ICategory> childCategories);

    Set<IFilter> getFilters();
    ICategory getParentCategory();
    Set<ICategory> getChildCategories();
    String getName();
    String getDescription();
}
