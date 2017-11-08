package com.jaguar.om.test;

import com.google.common.collect.ImmutableSet;
import com.jaguar.om.ICategory;
import com.jaguar.om.ICategoryDAO;
import com.jaguar.om.impl.Category;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

@Test(groups = "category_test")
public class CategoryTestCase extends BaseTestCase {

    private final Set<String> phoneCategorySet = ImmutableSet.of("Cell Phones","Land line Phones");

    //Create a few top level categories with child categories.
    @Test
    @Rollback(value = false)
    public void createCategories() throws Exception {
        //Create a category for Phones
        ICategory phoneCategory = new Category("Phones","A category for all types of phones");
        phoneCategory.setActive(true);
        phoneCategory = getDao().save(phoneCategory);

        //Create a sub category Cell Phones with parent category as Phones.
        ICategory cellPhoneSubCategory = new Category("Cell Phones","A category for all types of cell phones");
        cellPhoneSubCategory.setActive(true);
        cellPhoneSubCategory.setParentCategory(phoneCategory);
        cellPhoneSubCategory = getDao().save(cellPhoneSubCategory);

        //Create another sub category Land Line Phones with parent category as Phones.
        ICategory landlinePhoneSubCategory = new Category("Land line Phones","A category for all types of land line phones");
        landlinePhoneSubCategory.setActive(true);
        landlinePhoneSubCategory.setParentCategory(phoneCategory);
        getDao().save(landlinePhoneSubCategory);

        //Create another sub category Cell phone Cases under Cell Phones sub category
        ICategory cellPhoneCoverSubCategory = new Category("Cell Phone Cover","A category for all cell phone covers");
        cellPhoneCoverSubCategory.setActive(true);
        cellPhoneCoverSubCategory.setParentCategory(cellPhoneSubCategory);
        getDao().save(cellPhoneCoverSubCategory);
    }



    @Test(dependsOnMethods = "createCategories")
    public void getCategories() throws Exception {
        //Get all child categories that belong to Phones.
        //First get the parent category
        ICategory category = new Category("Phones");
        category = getDao().loadSingleFiltered(category,null,false);
        Assert.assertNotNull(category);
        Assert.assertNotNull(category.getChildCategories());
        for(ICategory childCategory : category.getChildCategories()) {
            Assert.assertNotNull(childCategory);
            final String childCategoryName = childCategory.getName();
            Assert.assertNotNull(childCategoryName);
            Assert.assertTrue(phoneCategorySet.contains(childCategoryName));
        }
        //Now get the Cell Phone Cover category and check it's parent category.
        category = new Category("Cell Phone Cover");
        category = getDao().loadSingleFiltered(category,null,false);
        Assert.assertNotNull(category);
        Assert.assertNotNull(category.getParentCategory());
        Assert.assertTrue(category.getParentCategory().getName().equals("Cell Phones"));
        //Get all categories with NULL parent categories.
        final ICategoryDAO categoryDAO = getCategoryDAO();
        Assert.assertNotNull(categoryDAO);
        final List<ICategory> parentCategoryList = categoryDAO.getAllCategories(0,10);
        Assert.assertNotNull(parentCategoryList);
        Assert.assertTrue(parentCategoryList.size() == 1);

        //Get all categories with parent category = "Phones"
        List<ICategory> childCategoryList = categoryDAO.getAllChildCategoriesForParentCategory("Phones",0,10);
        Assert.assertNotNull(childCategoryList);
        Assert.assertTrue(childCategoryList.size() > 1);
        for(ICategory childCategory : childCategoryList) {
            Assert.assertTrue(phoneCategorySet.contains(childCategory.getName()));
        }
    }

    //Re-enable when running this test case separately.
    @Test(dependsOnMethods = "getCategories",alwaysRun = true,enabled = false)
    @Rollback(value = false)
    public void deleteAllCategories() throws Exception {
        ICategory phoneCategory = new Category("Phones");
        phoneCategory = getDao().loadSingleFiltered(phoneCategory,null,false);
        Assert.assertNotNull(phoneCategory);
        getDao().remove(phoneCategory);
    }
}
