package com.jaguar.om.impl;

import com.jaguar.om.IFilter;
import com.jaguar.om.IProduct;
import com.jaguar.om.IProductDAO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.collections.Sets;
import org.testng.util.Strings;

import javax.persistence.Query;
import java.util.List;
import java.util.Set;

@Component("productDao")
@SuppressWarnings("unchecked")
@Transactional
public class ProductDAO extends CommonDao implements IProductDAO {

    private static final String queryProductsByCategory = "select distinct product from Product product where product.category.name =:categoryName";
    //Search all products by free text search. Either the product title or product description contains this free text.
    private static final String queryProductsByFreeText = "select distinct product from Product product where lower(product.title) like :freetext or lower(product.description) like :freetext";
    private static final String queryProductsByTitle = "select distinct product from Product product where lower(product.title) like :productTitle";
    private static final String queryProductsByFilterName = "select distinct product from Product product where product.category.filters in :filterIds";

    @Override
    public List<IProduct> getAllProductsForCategory(String categoryName, int start, int size) throws Exception {
        if(Strings.isNullOrEmpty(categoryName)) {
            daoLogger.error("Expected category name to be non-null, but found "+categoryName);
            throw new Exception("Expected category name to be non-null");
        }
        final Query query = getEntityManager().createQuery(queryProductsByCategory);
        query.setParameter("categoryName",categoryName);
        query.setFirstResult(start);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Override
    public List<IProduct> getAllProductsByFreeText(String freeText, int start, int size) throws Exception {
        if(Strings.isNullOrEmpty(freeText)) {
            daoLogger.error("Expected description in query getAllProductsByFreeText to non-null, but found "+ freeText);
            throw new IllegalArgumentException("Expected description in query getAllProductsByFreeText to non-null, but found "+ freeText);
        }
        final Query query = getEntityManager().createQuery(queryProductsByFreeText);
        query.setParameter("freetext", freeText);
        query.setFirstResult(start);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Override
    public List<IProduct> getAllProductsForCategoryByFilterName(Set<IFilter> filters, int start, int size) throws Exception {
        if(filters == null || filters.isEmpty()) {
            daoLogger.error("Expected filter id set in query getAllProductsForCategoryByFilterName to be non-null && non-empty, but found "+ filters);
            throw new IllegalArgumentException("Expected filter id set in query getAllProductsForCategoryByFilterName to be non-null && non-empty, but found "+ filters);
        }
        final Set<Long> filterIds = Sets.newHashSet();
        for(IFilter filter : filters) {
            filterIds.add(filter.getId());
        }
        final Query query = getEntityManager().createQuery(queryProductsByFilterName);
        query.setParameter("filterIds",filterIds);
        query.setFirstResult(start);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Override
    public List<IProduct> getProductsByTitle(String title, int start, int size) {
        if(Strings.isNullOrEmpty(title)) {
            daoLogger.error("The title is a required field for search by title method getProductsByTitle.");
            throw new IllegalArgumentException("Expected title in query getProductsByTitle to be non-null && non-empty, but found "+title);
        }
        final Query query = getEntityManager().createQuery(queryProductsByTitle);
        query.setFirstResult(start);
        query.setMaxResults(size);
        query.setParameter("productTitle",title);
        return query.getResultList();
    }


    @Override
    public List<IProduct> getAllProductsByFilterValue(String filterValue, int start, int size) throws Exception {
        return null;
    }
}
