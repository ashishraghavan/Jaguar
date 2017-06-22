package com.jaguar.om;


public interface IAccount extends ICommonObject{
    String getAccountName();
    void setAccountName(String accountName);
    String getCity();
    void setCity(String city);
    String getCountry();
    void setCountry(String country);
    String getState();
    void setState(String state);
    String getPostalCode();
    void setPostalCode(String postalCode);
}
