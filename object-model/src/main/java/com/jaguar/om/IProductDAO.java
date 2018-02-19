package com.jaguar.om;

import java.util.List;
import java.util.Set;

public interface IProductDAO extends ICommonDao {
    List<IProduct> getAllProductsForCategory(final String categoryName,int start,int size) throws Exception;
    List<IProduct> getAllProductsByFreeText(final String freeText, int start, int size) throws Exception;
    List<IProduct> getAllProductsForCategoryByFilterName(final Set<IFilter> filterIds, int start, int size) throws Exception;
    List<IProduct> getProductsByTitle(final String title,int start,int size);
    List<IProduct> getAllProductsByFilterValue(final String filterValue,int start,int size) throws Exception;
}
