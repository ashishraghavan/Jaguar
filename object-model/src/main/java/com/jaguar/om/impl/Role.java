package com.jaguar.om.impl;


import com.jaguar.om.IAccount;
import com.jaguar.om.IAccountable;
import com.jaguar.om.IRole;

import javax.persistence.*;

@Entity
@Table(name = "jaguar_role",uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@AttributeOverride(name = "id",column = @Column(name = "role_id"))
public class Role extends CommonObject implements IRole {


    public Role() {}
    public Role(final String name,final String description) {
        super();
        this.name = name;
        this.description = description;
    }

    @Column(name = "name",insertable = true,updatable = true,nullable = false,length = 100)
    private String name;

    @Column(name = "description",insertable = true,updatable = true,nullable = false)
    private String description;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String roleDescription) {
        this.description = roleDescription;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
