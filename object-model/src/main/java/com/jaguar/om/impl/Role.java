package com.jaguar.om.impl;


import com.jaguar.om.IRole;

import javax.persistence.*;

@Entity
@Table(name = "jaguar_role",uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@AttributeOverride(name = "id",column = @Column(name = "role_id"))
public class Role extends CommonObject implements IRole {


    public Role() {}
    public Role(final String name) {
        super();
        this.name = name;
    }

    @Column(name = "name",insertable = true,updatable = true,nullable = false,length = 100)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
