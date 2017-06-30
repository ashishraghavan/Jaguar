package com.jaguar.om.impl;


import com.jaguar.om.IApplication;
import com.jaguar.om.IApplicationRole;
import com.jaguar.om.IRole;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "application_role",uniqueConstraints = {@UniqueConstraint(columnNames = {"application_id","role_id"})})
public class ApplicationRole extends CommonObject implements IApplicationRole{

    @ManyToOne(targetEntity = Application.class,optional = false)
    @JoinColumn(name = "application_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_applicationrole_application_id")
    private IApplication application;

    @ManyToOne(targetEntity = Role.class,optional = false)
    @JoinColumn(name = "role_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_applicationrole_role_id")
    private IRole role;

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
}
