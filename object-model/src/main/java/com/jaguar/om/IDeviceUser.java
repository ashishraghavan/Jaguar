package com.jaguar.om;


public interface IDeviceUser {
    void setDevice(final IDevice device);
    void setUser(final IUser user);
    IDevice getDevice();
    IUser getUser();
    void setAccount(final IAccount account);
    IAccount getAccount();
}
