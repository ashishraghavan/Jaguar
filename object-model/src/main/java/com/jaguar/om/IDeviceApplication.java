package com.jaguar.om;


public interface IDeviceApplication {
    IDevice getDevice();
    IApplication getApplication();
    void setDevice(final IDevice device);
    void setApplication(IApplication application);
}
