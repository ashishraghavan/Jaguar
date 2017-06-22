package com.jaguar.om.impl;


import com.jaguar.om.IAccount;

import javax.persistence.*;


@Entity
@Table(name = "jaguar_account",uniqueConstraints = {@UniqueConstraint(columnNames = {"account_name"})})
@AttributeOverride(name = "id", column = @Column(name = "account_id"))
public class Account extends CommonObject implements IAccount{

    public Account() {
        super();
    }

    public Account(final String accountName) {
        this.accountName = accountName;
    }

    @Column(name = "account_name",nullable = false,insertable = true,updatable = true)
    private String accountName;

    @Column(name = "city", nullable = false,insertable = true,updatable = true)
    private String city;

    @Column(name = "country", nullable = false,insertable = true,updatable = true)
    private String country;

    @Column(name = "state", nullable = false,insertable = true,updatable = true)
    private String state;

    @Column(name = "postal_code",nullable = false,insertable = true,updatable=true)
    private String postalCode;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
