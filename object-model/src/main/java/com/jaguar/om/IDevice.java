package com.jaguar.om;


import java.util.Set;

/**
 * Only Android devices are supported.
 */
public interface IDevice {
    //This has to be unique when tied to accounts.
    String getDeviceUId();
    void setDeviceUId(final String deviceUId);
    Set<IDeviceApplication> getDeviceApplications();
    void setDeviceApplications(Set<IDeviceApplication> deviceApplications);
    IAccount getAccount();
    void setAccount(IAccount account);
    String getModel();
    void setModel(final String model);
    //This corresponds to the bundle version (incremental or not)
    Long getVersionCode();
    void setVersionCode(final Long versionCode);
    //Corresponds to the android package name.
    String getPackageName();
    void setPackageName(final String packageName);
    //One user can have multiple devices.
    //device_id,user_id is the unique combination.
    //account_id,device_id is also a unique combination.
    IUser getUser();
    void setUser(final IUser user);
    //Corresponds to the android API version code.
    //We deal only with API versions greater than 15 [which covers maximum of devices]
    Long getApiVersion();
    void setApiVersion(Long apiVersion);
}
