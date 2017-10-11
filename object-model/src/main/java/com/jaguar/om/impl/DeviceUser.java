package com.jaguar.om.impl;


import com.jaguar.om.IAccount;
import com.jaguar.om.IDevice;
import com.jaguar.om.IDeviceUser;
import com.jaguar.om.IUser;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "device_user",uniqueConstraints = {@UniqueConstraint(columnNames = {"device_id","user_id"})})
@AttributeOverride(name = "id", column = @Column(name = "device_user_id"))
public class DeviceUser extends CommonObject implements IDeviceUser {

    public DeviceUser(){}

    public DeviceUser(final IDevice device,final IUser user) {
        this();
        this.device = device;
        this.user = user;
    }

    @ManyToOne(targetEntity = Device.class,optional = false)
    @JoinColumn(name = "device_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_device_user_device_id")
    private IDevice device;

    @ManyToOne(targetEntity = User.class,optional = false)
    @JoinColumn(name = "user_id", insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_device_user_user_id")
    private IUser user;

    @Override
    public void setDevice(IDevice device) {
        this.device = device;
    }

    @Override
    public void setUser(IUser user) {
        this.user = user;
    }

    @Override
    public IDevice getDevice() {
        return this.device;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }
}
