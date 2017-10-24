package com.jaguar.om.impl;

import com.jaguar.om.IRole;
import com.jaguar.om.IUser;
import com.jaguar.om.IUserRole;

import javax.persistence.*;

@Entity
@Table(name = "user_role",uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id","role_id"})})
@AttributeOverride(name = "id",column = @Column(name = "user_role_id"))
public class UserRole extends CommonObject implements IUserRole {

    public UserRole(){
        super();
    }

    public UserRole(final IUser user,final IRole role) {
        this();
        this.user = user;
        this.role = role;
    }

    @Override
    public void setUser(IUser user) {
        this.user = user;
    }

    @Override
    public void setRole(IRole role) {
        this.role = role;
    }

    @ManyToOne(targetEntity = User.class,optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    @org.hibernate.annotations.ForeignKey(name = "fk_user_role_user_id")
    private IUser user;

    @ManyToOne(targetEntity = Role.class,optional = false)
    @JoinColumn(name = "role_id",nullable = false)
    @org.hibernate.annotations.ForeignKey(name = "fk_user_role_role_id")
    private IRole role;

    @Override
    public IRole getRole() {
        return this.role;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }
}
