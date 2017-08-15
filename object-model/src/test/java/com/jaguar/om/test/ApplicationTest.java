package com.jaguar.om.test;


import com.jaguar.om.IAccount;
import com.jaguar.om.IApplication;
import com.jaguar.om.enums.ApplicationType;
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
        IApplication application = new Application(account,"AppSense","http://localhost:8080/api/client", ApplicationType.MOBILE_APP,"com.jaguar.jaguarxf");
        application.setVersionCode("1.0");
        application.setActive(true);
        application = getDao().save(application);
        Assert.assertNotNull(application);

        application = new Application(account,"TestApp","http://localhost:8080/api/client", ApplicationType.MOBILE_APP,"com.jaguar.jaguarrf");
        application.setVersionCode("1.0");
        application.setActive(true);
        application = getDao().save(application);
        Assert.assertNotNull(application);
        applicationId = application.getId();
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void getApplication() throws Exception {
        final IApplication application = getDao().load(Application.class,applicationId);
        Assert.assertNotNull(application);
    }

    @Test(dependsOnMethods = {"getApplication"})
    public void searchApplication() throws Exception {
        final IAccount account = getDao().load(Account.class,accountId);
        Assert.assertNotNull(account);
        IApplication application = new Application(account,"TestApp");
        application = getDao().loadSingleFiltered(application,COMMON_EXCLUDE_PROPERTIES,false);
        Assert.assertNotNull(application);
    }

    @Test(dependsOnMethods = {"searchApplication"})
    @Rollback(value = false)
    public void deleteApplication() throws Exception {
        final IApplication application = getDao().load(Application.class,applicationId);
        Assert.assertNotNull(application);
        getDao().remove(application);
    }
}
