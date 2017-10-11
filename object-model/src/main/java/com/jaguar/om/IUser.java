package com.jaguar.om;


import java.security.Principal;
import java.util.Date;
import java.util.Set;

public interface IUser extends ICommonObject,Principal {
    String getName();
    void setName(String name);
    String getFirstName();
    void setFirstName(String firstName);
    String getLastName();
    void setLastName(String lastName);
    String getPassword();
    void setPassword(String password);
    String getEmail();
    void setEmail(String email);
    Date getLastOnline();
    void setLastOnline(Date lastOnline);
    IAccount getAccount();
    Set<IDevice> getDevices();
    void setPhoneNumber(final String phoneNumber);
    String getPhoneNumber();
}
