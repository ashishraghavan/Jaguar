package com.jaguar.om.impl;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaguar.om.ICommonObject;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
public abstract class CommonObject implements ICommonObject {

    @JsonIgnore
    @Column(name = "created",nullable=false,insertable = false,updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @JsonIgnore
    @Column(name = "modified",nullable = true,insertable = true,updatable = true,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;

    @JsonIgnore
    @Column(name = "active",nullable = false,insertable = true,updatable = true,columnDefinition = "boolean default true")
    private boolean active;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public CommonObject(){
        this.active = true;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        return this.modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }
        if (this.getClass() != that.getClass()) {
            return false;
        }
        CommonObject thatObj = (CommonObject) that;
        return !(this.getId() == null || thatObj.getClass() == null) && (this == that || this.getId().equals(thatObj.getId()));
    }

    @Override
    public int hashCode() {
        long intermediate = 23 * System.identityHashCode(this.getClass());
        if(null != this.getId()) {
            intermediate = intermediate * 37 + this.getId();
        }
        return (int)intermediate % Integer.MAX_VALUE;
    }
}
