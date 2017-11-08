package com.jaguar.om.test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.jaguar.om.ICategory;
import com.jaguar.om.IFilter;
import com.jaguar.om.IFilterValue;
import com.jaguar.om.impl.Category;
import com.jaguar.om.impl.Filter;
import com.jaguar.om.impl.FilterValue;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

@Test(groups = "filter_test",dependsOnGroups = "category_test")
public class FilterTestCase extends BaseTestCase {

    private static final Set<String> filterValueSet = ImmutableSet.of("8GB","16GB","32GB","64GB");

    @Test
    @Rollback(value = false)
    public void createFilters() throws Exception {
        ICategory cellPhoneCategory = new Category("Cell Phones");
        cellPhoneCategory = getDao().loadSingleFiltered(cellPhoneCategory,null,false);
        Assert.assertNotNull(cellPhoneCategory);

        IFilter cellPhoneFilter = new Filter("Storage");
        cellPhoneFilter.setCategory(cellPhoneCategory);
        cellPhoneFilter = getDao().save(cellPhoneFilter);

        //Create a filter value of 8 GB.
        IFilterValue filterValue = new FilterValue(cellPhoneFilter,"8GB");
        getDao().save(filterValue);

        //Create a filter value of 16GB.
        filterValue = new FilterValue(cellPhoneFilter,"16GB");
        getDao().save(filterValue);

        //Create a filter value of 32GB.
        filterValue = new FilterValue(cellPhoneFilter,"32GB");
        getDao().save(filterValue);

        //Create a filter value of 64GB
        filterValue = new FilterValue(cellPhoneFilter,"64GB");
        getDao().save(filterValue);
    }

    @Test(dependsOnMethods = "createFilters")
    public void getFiltersOnCategories() throws Exception {
        //Get all filters for a catgory.
        ICategory cellPhoneCategory = new Category("Cell Phones");
        cellPhoneCategory = getDao().loadSingleFiltered(cellPhoneCategory,null,false);
        Assert.assertNotNull(cellPhoneCategory);

        final Set<IFilter> filters = cellPhoneCategory.getFilters();
        Assert.assertNotNull(filters);

        final List<IFilter> filterList = Lists.newArrayList(filters);
        Assert.assertTrue(filterList.size() == 1);
        //Query the filter.
        final Set<IFilterValue> cellPhoneFilterValues = filterList.get(0).getFilterValues();
        Assert.assertNotNull(cellPhoneFilterValues);
        for(IFilterValue filterValue : cellPhoneFilterValues) {
            Assert.assertTrue(filterValueSet.contains(filterValue.getValue()));
        }
    }

    @Test(dependsOnMethods = "getFiltersOnCategories",alwaysRun = true,enabled = false)
    @Transactional
    @Rollback(value = false)
    public void deleteAllFilters() throws Exception {
        IFilter cellPhoneFilter = new Filter("Storage");
        cellPhoneFilter = getDao().loadSingleFiltered(cellPhoneFilter,null,false);
        if(cellPhoneFilter != null) {
            getDao().remove(cellPhoneFilter);
        }
    }
}
