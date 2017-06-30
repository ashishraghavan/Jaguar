package com.jaguar.om.test;


import com.jaguar.om.IAccount;
import com.jaguar.om.IApplication;
import com.jaguar.om.impl.Account;
import com.jaguar.om.impl.Application;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Very simple group of tests demonstrating
 * CRUD operations for {@link com.jaguar.om.impl.Application}
 */
@Test(groups={"application"})
public class ApplicationTest extends BaseTestCase {

    private Long applicationId;
    private Long accountId;

    @Test
    @Rollback(value=false)
    public void createApplication() throws Exception {
        IAccount account = new Account("Jaguar");
        account = getDao().loadSingleFiltered(account,COMMON_EXCLUDE_PROPERTIES,false);
        Assert.assertNotNull(account);
        accountId = account.getId();
        IApplication application = new Application(account,"AppSense","http://localhost:8080/api/client");
        application.setActive(true);
        application.setClientId(515207);
        application.setClientSecret("");
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void getApplication() throws Exception {

    }

    @Test(dependsOnMethods = {"getApplication"})
    public void searchApplication() throws Exception {

    }

    @Test(dependsOnMethods = {"searchApplication"})
    public void deleteApplication() throws Exception {

    }
}
