package com.jaguar.om.impl;

import com.jaguar.om.IAccount;
import com.jaguar.om.IApplication;

import javax.persistence.*;

import com.jaguar.om.IApplicationRole;
import com.jaguar.om.common.Utils;
import org.hibernate.annotations.ForeignKey;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "jaguar_application",uniqueConstraints = {@UniqueConstraint(columnNames = {"account_id","name"})})
@AttributeOverride(name = "id",column = @Column(name = "application_id"))
public class Application extends CommonObject implements IApplication{

    @ManyToOne(targetEntity = Account.class,optional = false)
    @JoinColumn(name = "account_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_application_account_id")
    private IAccount account;

    @Column(name = "name",insertable = true,updatable = true,nullable = false,length = 100)
    private String name;

    @Column(name = "redirect_uri",insertable = true,updatable = true,nullable = false)
    private String redirectUri;

    @Column(name = "client_id",unique = true,insertable = true,updatable = false,nullable = false)
    private Integer clientId;

    @Column(name = "client_secret",insertable = true,updatable = false,nullable = false)
    private String clientSecret;

    @Column(name = "version_code",nullable = false,insertable = true,updatable = true,length = 150)
    private String versionCode;

    @Column(name = "package_name",nullable = false,insertable = true,updatable = true,length = 150)
    private String packageName;

    @OneToMany(mappedBy = "application",targetEntity = ApplicationRole.class,fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private Set<IApplicationRole> applicationRoles;


    public Application(){}
    public Application(final IAccount account,final String name,final String redirectUri) {
        this.account = account;
        this.clientId = Utils.generateKey();
        this.clientSecret = UUID.randomUUID().toString();
        this.name = name;
        this.redirectUri = redirectUri;
    }

    public Application(final IAccount account, final String name) {
        this.account = account;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getClientId() {
        return this.clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectURI() {
        return this.redirectUri;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectUri = redirectURI;
    }

    public Set<IApplicationRole> getRoles() {
        return this.applicationRoles;
    }

    public void setRoles(Set<IApplicationRole> roles) {
        this.applicationRoles = roles;
    }

    public IAccount getAccount() {
        return this.account;
    }

    public void setAccount(IAccount account) {
        this.account = account;
    }

    public String getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
