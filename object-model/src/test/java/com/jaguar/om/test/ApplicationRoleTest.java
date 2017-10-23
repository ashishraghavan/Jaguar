package com.jaguar.om.test;

import com.jaguar.om.IAccount;
import com.jaguar.om.IApplication;
import com.jaguar.om.IApplicationRole;
import com.jaguar.om.IRole;
import com.jaguar.om.impl.Account;
import com.jaguar.om.impl.Application;
import com.jaguar.om.impl.ApplicationRole;
import com.jaguar.om.impl.Role;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "application_role",dependsOnGroups = {"application","role"})
public class ApplicationRoleTest extends BaseTestCase {

    @Test
    public void testAddApplicationGroup() throws Exception {
        //Get the account
        IAccount account = new Account("Jaguar");
        account.setActive(true);
        account = getDao().loadSingleFiltered(account, null,false);
        Assert.assertNotNull(account);
        //Get the application
        IApplication application = new Application(1095369);
        application.setAccount(account);
        application.setActive(true);
        application = getDao().loadSingleFiltered(application,null,false);
        Assert.assertNotNull(application);
        //Get the role
        IRole role = new Role("seller");
        role.setActive(true);
        role = getDao().loadSingleFiltered(role,null,false);
        Assert.assertNotNull(role);
        //Create the application role.
        IApplicationRole applicationRole = new ApplicationRole(application,role);
        applicationRole = getDao().save(applicationRole);
        Assert.assertNotNull(applicationRole);
    }
}
