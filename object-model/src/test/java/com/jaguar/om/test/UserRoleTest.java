package com.jaguar.om.test;

import com.jaguar.om.IAccount;
import com.jaguar.om.IRole;
import com.jaguar.om.IUser;
import com.jaguar.om.IUserRole;
import com.jaguar.om.impl.Account;
import com.jaguar.om.impl.Role;
import com.jaguar.om.impl.User;
import com.jaguar.om.impl.UserRole;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "user_role",dependsOnGroups = {"user","role"})
public class UserRoleTest extends BaseTestCase {

    private Long userRoleId;

    @Test
    @Rollback(value = false)
    public void createUserRole() throws Exception {
        IAccount account  = new Account("Jaguar");
        account = getDao().loadSingleFiltered(account,null,false);
        Assert.assertNotNull(account);
        IUser user  = new User(account,"ashish.raghavan@google.com");
        user = getDao().loadSingleFiltered(user,null,false);
        Assert.assertNotNull(user);
        IRole role = new Role("seller");
        role = getDao().loadSingleFiltered(role,null,false);
        Assert.assertNotNull(role);
        IUserRole userRole = new UserRole(user,role);
        IUserRole userRoleFromDB = getDao().loadSingleFiltered(userRole,null,false);
        Assert.assertNull(userRoleFromDB);
        userRole = getDao().save(userRole);
        userRoleId = userRole.getId();
    }

    @Test(dependsOnMethods = "createUserRole")
    @Rollback(value = false)
    public void deleteUserRole() throws Exception {
        Assert.assertNotNull(userRoleId);
        IUserRole userROle = getDao().load(UserRole.class,userRoleId);
        Assert.assertNotNull(userROle);
        getDao().remove(userROle);
    }
}
