package com.jaguar.om.impl;


import com.jaguar.om.IApplication;
import com.jaguar.om.IDevice;
import com.jaguar.om.IDeviceApplication;

import org.hibernate.annotations.ForeignKey;
import javax.persistence.*;

@Entity
@Table(name = "device_application",uniqueConstraints = {@UniqueConstraint(columnNames = {"device_id","application_id"})})
@AttributeOverride(name = "id",column = @Column(name = "device_application_id"))
public class DeviceApplication extends CommonObject implements IDeviceApplication{

    public DeviceApplication(){}
    public DeviceApplication(final IDevice device,IApplication application) {
        this.device = device;
        this.application = application;
    }

    @Column(name = "authorization_status")
    @Enumerated(value = EnumType.STRING)
    private Authorization authorization;

    @ManyToOne(targetEntity = Device.class,optional = false)
    @JoinColumn(name = "device_id",nullable = false,insertable = true,updatable = true)
    @ForeignKey(name = "fk_device_application_device_id")
    private IDevice device;

    @ManyToOne(targetEntity = Application.class,optional = false)
    @JoinColumn(name = "application_id",nullable = false,insertable = true,updatable = true)
    @ForeignKey(name = "fk_device_application_application_id")
    private IApplication application;

    public IDevice getDevice() {
        return this.device;
    }

    public IApplication getApplication() {
        return this.application;
    }

    public void setDevice(IDevice device) {
        this.device = device;
    }

    public void setApplication(IApplication application) {
        this.application = application;
    }

    @Override
    public Authorization getAuthorization() {
        return this.authorization;
    }

    @Override
    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }
}
