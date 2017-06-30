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

    public Device(final IAccount account,final String deviceUid,final IUser user,final String model,
                  final Long versionCode,final Long apiVersion,final String packageName) {
        this(account,deviceUid,user);
        this.model = model;
        this.versionCode = versionCode;
        this.apiVersion = apiVersion;
        this.packageName = packageName;
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

    @Column(name = "version_code",nullable = false,insertable = true,updatable = true,length = 150)
    private Long versionCode;

    @Column(name = "package_name",nullable = false,insertable = true,updatable = true,length = 150)
    private String packageName;

    @ManyToOne(targetEntity = User.class,optional = false)
    @JoinColumn(name = "user_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_device_user_id")
    private IUser user;

    @Column(name = "api_version",nullable = false,insertable = true,updatable = true,length = 50)
    private Long apiVersion;

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

    public Long getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public IUser getUser() {
        return this.user;
    }

    public void setUser(IUser user) {
        this.user = user;
    }

    public Long getApiVersion() {
        return this.apiVersion;
    }

    public void setApiVersion(Long apiVersion) {
        this.apiVersion = apiVersion;
    }
}
