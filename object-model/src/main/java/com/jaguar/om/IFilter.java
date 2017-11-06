package com.jaguar.om;

import java.util.Set;

/**
 * A filter interface describing filters that apply to a product category.
 */
public interface IFilter extends ICommonObject {
    void setName(final String name);
    void setValues(final Set<IFilterValue> filterValues);
    void setCategory(final ICategory category);

    Set<IFilterValue> getFilterValues();
    String getName();
    ICategory getCategory();
}
