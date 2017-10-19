package com.jaguar.om.test;


import com.jaguar.om.IAccount;
import com.jaguar.om.IDevice;
import com.jaguar.om.IUser;
import com.jaguar.om.impl.Account;
import com.jaguar.om.impl.Device;
import com.jaguar.om.impl.User;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "device",dependsOnGroups = "user")
public class DeviceTest extends BaseTestCase {

    private final Long[] userIdArr = new Long[3];
    private final String[] deviceIdAddr = new String[2];

    @Test
    @Rollback(value = false)
    public void testCreateDevice() throws Exception {
        IAccount account = new Account("Jaguar");
        account = getDao().loadSingleFiltered(account,COMMON_EXCLUDE_PROPERTIES,false);
        Assert.assertNotNull(account);

        //create a dummy user too to create the device (register the device)
        IUser user = new User(account,"Dummy User","Dummy","User","dummy.user@dummyweb.com","4082216275");
        user.setPassword("yourass");
        user = getDao().save(user);
        Assert.assertNotNull(user);
        userIdArr[0] = user.getId();

        //Create another dummy user and associate with the same device.
        IUser user2 = new User(account,"Dummy User 2","Dummy","User","dummy.user2@dummyweb.com","4082216275");
        user2.setPassword("myass");
        user2 = getDao().save(user2);
        userIdArr[1] = user2.getId();

        IUser mainTestUser = new User(account,"ashish.raghavan@google.com");
        mainTestUser.setActive(true);
        mainTestUser = getDao().loadSingleFiltered(mainTestUser,null,false);
        Assert.assertNotNull(mainTestUser);
        Assert.assertNotNull(mainTestUser.getId());
        userIdArr[2] = mainTestUser.getId();

        //We will persist this device
        IDevice device = new Device(account,"TXF5143",mainTestUser,"Motorola Moto Z Play", 14);
        device = getDao().save(device);
        Assert.assertNotNull(device);
        deviceIdAddr[0] = device.getDeviceUId();

        //Create another device for DeviceUserTest.
        device = new Device(account,"TXF5044",mainTestUser,"iPhone 6s Plus",17);
        device = getDao().save(device);
        Assert.assertNotNull(device);
        deviceIdAddr[1] = device.getDeviceUId();

        //create another dummy device to test CRUD operations.
        device = new Device(account,"TXF5144",user2,"Motorola Moto Z", 22);
        device = getDao().save(device);
        Assert.assertNotNull(device);
    }

    @Test(dependsOnMethods = {"testCreateDevice"})
    public void testGetUniqueDevice() throws Exception {
        IAccount account = new Account("Jaguar");
        account = getDao().loadSingleFiltered(account,COMMON_EXCLUDE_PROPERTIES,false);
        Assert.assertNotNull(account);
        //Load the mainTestUser first.
        IUser user = new User(account,"ashish.raghavan@google.com");
        user = getDao().loadSingleFiltered(user,null,false);
        Assert.assertNotNull(user);
        IDevice device = new Device(deviceIdAddr[0],user);
        device = getDao().loadSingleFiltered(device,null,false);
        Assert.assertNotNull(device);
        device = new Device(deviceIdAddr[1],user);
        device = getDao().loadSingleFiltered(device,null,false);
        Assert.assertNotNull(device);
    }

    @Test(dependsOnMethods = {"testGetUniqueDevice"})
    @Rollback(value = false)
    public void testDeleteUser() throws Exception {
        final Long user2Id = userIdArr[1];
        Assert.assertNotNull(user2Id);
        final IUser user = getDao().load(User.class,user2Id);
        Assert.assertNotNull(user);
        getDao().remove(user);
    }
}
