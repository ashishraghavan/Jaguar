package com.jaguar.om.impl;


import com.jaguar.om.*;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "application_role",uniqueConstraints = {@UniqueConstraint(columnNames = {"application_id","role_id"})})
public class ApplicationRole extends CommonObject implements IApplicationRole,IAccountable{

    public ApplicationRole() {
        super();
    }

    public ApplicationRole(final IApplication application) {
        this.application = application;
        this.account = this.application.getAccount();
    }

    public ApplicationRole(final IApplication application,final IRole role) {
        this();
        this.application = application;
        this.role = role;
        this.account = this.application.getAccount();
    }

    @ManyToOne(targetEntity = Application.class,optional = false)
    @JoinColumn(name = "application_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_applicationrole_application_id")
    private IApplication application;

    @ManyToOne(targetEntity = Role.class,optional = false)
    @JoinColumn(name = "role_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_applicationrole_role_id")
    private IRole role;

    @ManyToOne(targetEntity = Account.class,optional = false)
    @JoinColumn(name = "account_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_applicationrole_account_id")
    private IAccount account;

    public IRole getRole() {
        return role;
    }

    public void setRole(IRole role) {
        this.role = role;
    }

    public IApplication getApplication() {
        return application;
    }

    public void setApplication(IApplication application) {
        this.application = application;
    }

    @Override
    public void setAccount(IAccount account) {
        this.account = account;
    }

    @Override
    public IAccount getAccount() {
        return this.account;
    }
}
