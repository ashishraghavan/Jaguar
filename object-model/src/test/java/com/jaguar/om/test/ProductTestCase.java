package com.jaguar.om.test;

import com.google.common.io.ByteStreams;
import com.jaguar.om.*;
import com.jaguar.om.common.Utils;
import com.jaguar.om.impl.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

@Test(groups = "product_test",dependsOnGroups = {"category_test","user","currency"})
public class ProductTestCase extends BaseTestCase {

    private String itemNumber = null;

    @Test
    @Transactional
    @Rollback(value = false)
    public void testCreateProducts() throws Exception {
        //Set images for this product.
        //We go by path of the context class loader because our file is not within a jar file.
        //We list all files within the current directory then use filters to get only JPG files.
        //This will load the test-classes folder under which all test resources will also be added.
        final URL resourcesURL = Thread.currentThread().getContextClassLoader().getResource(".");
        Assert.assertNotNull(resourcesURL);
        final File resourcesDirectory = new File(resourcesURL.getPath());
        Assert.assertTrue(resourcesDirectory.exists());
        Assert.assertNotNull(resourcesDirectory.getPath());
        final File[] fileList = resourcesDirectory.listFiles(pathname -> {
            //We are looking only for JPG files.
            return pathname.getName().endsWith("JPG") || pathname.getName().endsWith("jpg");
        });
        Assert.assertNotNull(fileList);
        IProduct cellPhone = new Product("iPhone 32GB Rose Gold","Seller refurbished iPhone 323 GB in excellent condition");
        //Assign the category of Cell Phones
        ICategory cellPhoneCategory = new Category("Cell Phones");
        cellPhoneCategory = getDao().loadSingleFiltered(cellPhoneCategory,null,false);
        Assert.assertNotNull(cellPhoneCategory);
        cellPhone.setCategory(cellPhoneCategory);
        cellPhone.setBuyingFormat(BuyingFormat.BUY_IT_NOW);
        cellPhone.setPrice(750);

        //Set the currency
        ICurrency currency  = new Currency();
        currency.setCountryCode("US");
        currency = getDao().loadSingleFiltered(currency,null,false);
        Assert.assertNotNull(currency);

        cellPhone.setCurrency(currency);

        //Set user.
        IAccount account = new Account("Jaguar");
        account = getDao().loadSingleFiltered(account,null,false);
        Assert.assertNotNull(account);
        IUser user = new User(account,"ashish.raghavan@google.com");
        user = getDao().loadSingleFiltered(user,null,false);
        Assert.assertNotNull(user);

        //Create a different product
        cellPhone.setUser(user);
        //For test purposes, we use the generateItemNumber method to set the UPC.
        cellPhone.setUPC(Utils.generateItemNumber());
        //For test purposes, we use the generateItemNumber method to set the MPN.
        cellPhone = getDao().save(cellPhone);
        Assert.assertNotNull(cellPhone);
        Assert.assertNotNull(cellPhone.getItemNumber());
        itemNumber = cellPhone.getItemNumber();
        for(File imageFile : fileList) {
            Assert.assertTrue(imageFile.getName().endsWith("JPG") ||
                    imageFile.getName().endsWith("jpg"),"Assertion failed because "+imageFile.getName()+ " is not a JPG file");
            Image image = new Image();
            image.setProductImage(ByteStreams.toByteArray(new FileInputStream(imageFile)));
            image.setProduct(cellPhone);
            image.setFileName(imageFile.getName());
            image = getDao().save(image);
            Assert.assertNotNull(image);
            Assert.assertTrue(image.getProduct().getItemNumber().equals(cellPhone.getItemNumber()));
        }
    }

    @Test(dependsOnMethods = "testCreateProducts")
    public void testGetProducts() throws Exception {
        Assert.assertNotNull(itemNumber);
        //Get the product using the item number.
        IProduct product = new Product(itemNumber);
        final List<IProduct> products = getDao().loadFiltered(product,null,false);
        Assert.assertNotNull(products);
        Assert.assertTrue(products.size() >= 1);
        final IProduct productFromList = products.get(0);
        Assert.assertTrue(productFromList.getItemNumber().equals(product.getItemNumber()));
        Assert.assertNotNull(productFromList.getProductImages());
        Assert.assertTrue(productFromList.getProductImages().size() > 0);
        Assert.assertTrue(productFromList.getProductImages().size() == 6);
    }
}
