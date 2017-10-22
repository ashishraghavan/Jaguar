package com.jaguar.om.impl;


import com.jaguar.om.IAccount;
import com.jaguar.om.IAddress;
import com.jaguar.om.IUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.ForeignKey;
import org.testng.util.Strings;

import javax.persistence.*;

@Entity
@Table(name = "jaguar_address",uniqueConstraints = @UniqueConstraint(columnNames = {"account_id","address_hash"}))
@AttributeOverride(name = "id",column = @Column(name = "address_id"))
public class Address extends CommonObject implements IAddress {

    protected Address(){
        super();
    }

    public Address(final IAccount account,final IUser user){
        this();
        this.account = account;
        this.user = user;
        if(Strings.isNullOrEmpty(line1) || Strings.isNullOrEmpty(city) || Strings.isNullOrEmpty(state) ||
                Strings.isNullOrEmpty(zip1)) {
            throw new IllegalArgumentException("Line1, City, State & Zip1 are needed to compute the hash");
        }
        //Set the hash here.
        setHash(DigestUtils.sha256Hex(this.line1 + this.city + this.state + this.zip1));
    }

    public Address(final IAccount account,final IUser user,final String line1,
                   final String city, final String state,final String zip1) {
        this(account,user);
        this.line1 = line1;
        this.city = city;
        this.state = state;
        this.zip1 = zip1;
    }

    public Address(final IAccount account,final IUser user,final String line1,
                   final String line2,final String city,final String state,
                   final String zip1,final String zip2) {
        this(account,user,line1,city,state,zip1);
        this.line2 = line2;
        this.zip2 = zip2;
    }

    @Column(name = "line1",insertable = true,updatable = true,nullable = false,length = 150)
    private String line1;

    @Column(name = "line2",insertable = true,updatable = true,nullable = true,length = 150)
    private String line2;

    @Column(name = "city",insertable = true,updatable = true,nullable = false,length = 50)
    private String city;

    @Column(name = "state",insertable = true,updatable = true,nullable = false,length = 50)
    private String state;

    @Column(name = "zip1",insertable = true,updatable = true,nullable = false,length = 10)
    private String zip1;

    @Column(name = "zip2",insertable = true,updatable = true,nullable = true,length = 10)
    private String zip2;

    @Column(name = "address_hash",insertable = true,updatable = false,nullable = false)
    private String hash;

    @ManyToOne(targetEntity = Account.class,optional = false)
    @JoinColumn(name = "account_id",nullable = false,insertable = true,updatable = true)
    @ForeignKey(name = "fk_address_account_id")
    private IAccount account;

    @ManyToOne(targetEntity = User.class,optional = false)
    @JoinColumn(name = "user_id",nullable = false,insertable = true,updatable = true)
    @ForeignKey(name = "fk_address_user_id")
    private IUser user;

    @Override
    public void setLine1(String line1) {
        this.line1 = line1;
    }

    @Override
    public void setLine2(String lin2) {
        this.line2 = line2;
    }

    @Override
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public void setState(String state) {
        this.state= state;
    }

    @Override
    public void setZip1(String zip1) {
        this.zip1 = zip1;
    }

    @Override
    public void setZip2(String zip2) {
        this.zip2 = zip2;
    }

    @Override
    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String getLine1() {
        return this.line1;
    }

    @Override
    public String getLine2() {
        return this.line2;
    }

    @Override
    public String getCity() {
        return this.city;
    }

    @Override
    public String getState() {
        return this.state;
    }

    @Override
    public String getZip1() {
        return this.zip1;
    }

    @Override
    public String getZip2() {
        return this.zip2;
    }

    @Override
    public String getHash() {
        return this.hash;
    }

    @Override
    public void setUser(IUser user) {
        this.user = user;
    }

    @Override
    public IUser getUser() {
        return this.user;
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
