package com.jaguar.om.impl;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaguar.om.IAccount;
import com.jaguar.om.IDevice;
import com.jaguar.om.IUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.security.Principal;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "jaguar_user",uniqueConstraints = {@UniqueConstraint(columnNames = {"account_id","email"})})
@AttributeOverride(name = "id",column = @Column(name = "user_id"))
public class User extends CommonObject implements IUser,Principal{

    public User() {}

    public User(final String email) {
        this();
        this.email = email;
    }

    public User(final IAccount account) {
        this();
        this.account = account;
    }

    public User(final IAccount account,final String email) {
        this(account);
        this.email = email;
    }

    public User(final IAccount account,final String name,final String firstName,final String lastName,final String email,final String phoneNumber) {
        this(account);
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Column(name = "name",nullable = true,insertable = true,updatable = true,length = 100)
    private String name;

    @Column(name = "first_name",nullable = true,insertable = true,updatable = true,length = 50)
    private String firstName;

    @Column(name="last_name",nullable = true,insertable = true,updatable = true,length = 50)
    private String lastName;


    @JsonIgnore
    @Column(name = "password",nullable = false,insertable = true,updatable = true,length = 100)
    private String password;

    @ManyToOne(targetEntity = Account.class,optional = false)
    @JoinColumn(name = "account_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_user_account_id")
    private IAccount account;

    @Column(name = "email",nullable = false,insertable = true,updatable = true,length = 100)
    private String email;

    @Column(name = "last_online",nullable = true,insertable = true,updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastOnline;

    @Column(name = "phone_number",nullable = true, insertable = true,updatable = true)
    private String phoneNumber;

    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "profile_image")
    private byte[] profileImage;

    @JsonIgnore
    @OneToMany(targetEntity = Device.class,cascade = CascadeType.ALL,fetch = FetchType.LAZY,mappedBy = "user",orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<IDevice> devices;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password  = DigestUtils.sha256Hex((getEmail() + password).getBytes());
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastOnline() {
        return this.lastOnline;
    }

    public void setLastOnline(Date lastOnline) {
        this.lastOnline = lastOnline;
    }

    public IAccount getAccount() {
        return this.account;
    }

    public Set<IDevice> getDevices() {
        return this.devices;
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    @Override
    public void setImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

    @Override
    public byte[] getProfileImage() {
        return this.profileImage;
    }

}
