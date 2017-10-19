package com.jaguar.om.test;

import com.jaguar.om.IAccount;
import com.jaguar.om.IUser;
import com.jaguar.om.impl.Account;
import com.jaguar.om.impl.User;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Very simple group of tests demonstrating
 * CRUD operations on {@link com.jaguar.om.impl.User}
 */
@Test(groups = "user",dependsOnGroups = {"application"})
public class UserTest extends BaseTestCase {


    private Long userId;
    private Long accountId;

    //create user
    @Test
    @Rollback(value = false)
    public void testCreateUser() throws Exception {
        IAccount account = new Account("Jaguar");
        account = getDao().loadSingleFiltered(account, COMMON_EXCLUDE_PROPERTIES,false);
        Assert.assertNotNull(account);
        accountId = account.getId();
        IUser user = new User(account,"Ashish Raghavan","Ashish","Raghavan","ashish.raghavan@google.com","4082216275");
        user.setPassword("12345");
        user.setActive(true);
        getDao().save(user);

        //create another dummy user for test purposes with the same account.
        user = new User(account,"Test User","Test","User","test.user@google.com","4082216275");
        user.setPassword("12345");
        user.setActive(true);
        user = getDao().save(user);
        userId = user.getId();
    }

    //get user by id.
    @Test(dependsOnMethods = {"testCreateUser"})
    public void testGetUser() throws Exception {
        final IUser user = getDao().load(User.class,userId);
        Assert.assertNotNull(user);
    }

    @Test(dependsOnMethods = {"testGetUser"})
    public void testSearchUser() throws Exception {
        final IAccount account = getDao().load(Account.class,accountId);
        Assert.assertNotNull(account);
        IUser user = new User(account,"ashish.raghavan@google.com");
        user = getDao().loadSingleFiltered(user, COMMON_EXCLUDE_PROPERTIES,false);
        Assert.assertNotNull(user);
    }

    @Test(dependsOnMethods = {"testSearchUser"})
    @Rollback(value = false)
    public void testDeleteUser() throws Exception {
        final IUser user = getDao().load(User.class, userId);
        Assert.assertNotNull(user);
        getDao().remove(user);
    }
}
