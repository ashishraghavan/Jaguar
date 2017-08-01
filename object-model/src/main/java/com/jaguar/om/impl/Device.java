package com.jaguar.om.impl;


import com.jaguar.om.IAccount;
import com.jaguar.om.IDevice;
import com.jaguar.om.IDeviceApplication;
import com.jaguar.om.IUser;

import org.hibernate.annotations.ForeignKey;
import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "jaguar_device",uniqueConstraints = {@UniqueConstraint(columnNames = {"device_uid","user_id"})})
@AttributeOverride(name = "id",column = @Column(name = "device_id"))
public class Device extends CommonObject implements IDevice {

    public Device(){}

    public Device(final IAccount account) {
        this();
        this.account = account;
    }

    public Device(final IAccount account,final String deviceUid,final IUser user) {
        this(account);
        this.deviceUid = deviceUid;
        this.user = user;
    }

    public Device(final IAccount account, final String deviceUid, final IUser user, final String model,
                  final Integer apiVersion) {
        this(account,deviceUid,user);
        this.model = model;
        this.apiVersion = apiVersion;
    }

    @ManyToOne(targetEntity = Account.class,optional = false)
    @JoinColumn(name = "account_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_device_account_id")
    private IAccount account;

    @OneToMany(mappedBy = "application",targetEntity = DeviceApplication.class,fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private Set<IDeviceApplication> deviceApplications;

    @Column(name = "device_uid",nullable = false,insertable = true,updatable = true,length = 150)
    private String deviceUid;

    @Column(name = "model",nullable = false,insertable = true,updatable = true,length = 150)
    private String model;

    @ManyToOne(targetEntity = User.class,optional = false,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_device_user_id")
    private IUser user;

    @Column(name = "api_version",nullable = false,insertable = true,updatable = true,length = 50)
    private Integer apiVersion;

    public String getDeviceUId() {
        return this.deviceUid;
    }

    public void setDeviceUId(String deviceUId) {
        this.deviceUid = deviceUId;
    }

    public Set<IDeviceApplication> getDeviceApplications() {
        return this.deviceApplications;
    }

    public void setDeviceApplications(Set<IDeviceApplication> deviceApplications) {
        this.deviceApplications = deviceApplications;
    }

    public IAccount getAccount() {
        return account;
    }

    public void setAccount(IAccount account) {
        this.account = account;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public IUser getUser() {
        return this.user;
    }

    public void setUser(IUser user) {
        this.user = user;
    }

    public Integer getApiVersion() {
        return this.apiVersion;
    }

    public void setApiVersion(Integer apiVersion) {
        this.apiVersion = apiVersion;
    }
}
