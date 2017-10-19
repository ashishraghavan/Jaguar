package com.jaguar.om.test;

import com.jaguar.om.IRole;
import com.jaguar.om.impl.Role;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "role")
public class RoleTest extends BaseTestCase{

    @Test
    @Rollback(value = false)
    public void testCreateRole() throws Exception{
        IRole role = new Role("seller");
        role.setActive(true);
        role.setDescription("Seller account");
        role = getDao().save(role);
        Assert.assertNotNull(role);
    }
}
