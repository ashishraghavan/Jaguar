package com.jaguar.om.test;

import com.jaguar.om.IAccount;
import com.jaguar.om.impl.Account;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Very simple group of tests demonstrating
 * CRUD operations on the {@link Account}
 */
@Test(groups = {"account"})
public class AccountTestCase extends BaseTestCase {

    private Long accountId;
    //create an account
    @Test
    @Rollback(value = false)
    public void createAccount() throws Exception {
        //Add a default account
        IAccount account = new Account("Jaguar");
        account.setCity("Long Is City");
        account.setCountry("USA");
        account.setPostalCode("11101");
        account.setState("NY");
        account.setActive(true);
        getDao().save(account);

        account = new Account("Apple");
        account.setCity("Cupertino");
        account.setState("CA");
        account.setCountry("USA");
        account.setPostalCode("95014");
        account.setActive(true);
        account = getDao().save(account);
        accountId = account.getId();
    }

    //get account by name.
    @Test(dependsOnMethods = {"createAccount"})
    public void getAccount() throws Exception {
        final IAccount account = getDao().load(Account.class,accountId);
        Assert.assertNotNull(account);
    }

    //search for account.
    @Test(dependsOnMethods = {"getAccount"})
    public void searchAccount() throws Exception {
        final Map<String,Object> searchCriteria = ImmutableMap.<String,Object>builder().put("accountName","Apple").build();
        final List<Account> accountList = getDao().search(Account.class,searchCriteria,null,true,null,null);
        Assert.assertNotNull(accountList);
        Assert.assertTrue(!accountList.isEmpty());
    }

    //delete the account.
    @Test(dependsOnMethods = {"searchAccount"})
    @Rollback(value = false)
    public void deleteAccount() throws Exception {
        final IAccount account = getDao().load(Account.class,accountId);
        Assert.assertNotNull(account);
        getDao().remove(account);
    }

}
