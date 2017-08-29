package com.jaguar.om.impl;


import com.jaguar.om.IAccount;
import com.jaguar.om.IApplication;
import com.jaguar.om.IUser;
import com.jaguar.om.IUserApplication;
import org.hibernate.annotations.ForeignKey;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component
@Entity
@Table(name = "user_application",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","application_id","account_id"}))
public class UserApplication extends CommonObject implements IUserApplication {

    public UserApplication(){
        super();
    }

    public UserApplication(final IUser user,final IApplication application) {
        this();
        this.user = user;
        this.application = application;
        this.account = this.application.getAccount();
    }

    @ManyToOne(targetEntity = User.class,optional = false)
    @JoinColumn(name = "user_id",nullable = false,updatable = true,insertable = true)
    @ForeignKey(name = "fk_user_application_user_id")
    private IUser user;


    @ManyToOne(targetEntity = Application.class,optional = false)
    @JoinColumn(name = "application_id",nullable = false,updatable = true,insertable = true)
    @ForeignKey(name = "fk_user_application_application_id")
    private IApplication application;

    @ManyToOne(targetEntity = Account.class,optional = false)
    @JoinColumn(name = "account_id",nullable = false,updatable = true,insertable = true)
    @ForeignKey(name = "fk_user_application_account_id")
    private IAccount account;

    @Column(name = "authorization_status",nullable = true,insertable = true,updatable = true)
    @Enumerated(value = EnumType.STRING)
    private Authorization authorization;

    @Override
    public void setUser(IUser user) {
        this.user = user;
    }

    @Override
    public void setApplication(IApplication application) {
        this.application = application;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }

    @Override
    public IApplication getApplication() {
        return this.application;
    }

    @Override
    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    @Override
    public Authorization getAuthorization() {
        return this.authorization;
    }
}
