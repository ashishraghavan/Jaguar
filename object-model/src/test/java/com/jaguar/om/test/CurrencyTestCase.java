package com.jaguar.om.test;

import com.jaguar.om.ICurrency;
import com.jaguar.om.enums.CurrencySymbolPosition;
import com.jaguar.om.impl.Currency;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "currency")
public class CurrencyTestCase extends BaseTestCase {

    //Create currency
    @Test
    @Rollback(value = false)
    public void createCurrencies() throws Exception {
        ICurrency currency = new Currency("$","US");
        currency.setCountryName("United States of America");
        currency.setSymbolPosition(CurrencySymbolPosition.BEFORE);
        currency = getDao().save(currency);
        Assert.assertNotNull(currency);
    }

    @Test(dependsOnMethods = "createCurrencies")
    public void getCurrencyUnique() throws Exception {
        ICurrency currency = new Currency();
        currency.setCountryCode("US");
        currency = getDao().loadSingleFiltered(currency,null,false);
        Assert.assertNotNull(currency);
        Assert.assertTrue(currency.getCountryCode().equals("US"));
        Assert.assertTrue(currency.getCountryName().equals("United States of America"));
    }
}
