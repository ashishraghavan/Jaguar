package com.jaguar.om;

/**
 * USPS userId   : 017SOMEC7066
 * USPS password : 260ZB00NH881
 * http://production.shippingapis.com/ShippingAPI.dll
 * https://secure.shippingapis.com/ShippingAPI.dll
 *
 *      <?xml version="1.0" encoding="UTF-8"?>
        <AddressValidateRequest USERID="017SOMEC7066">
        <Address ID="0">
        <Address1 >3416 42nd St Apt 1C</Address1>
        <Address2/>
        <City>Long Is City</City>
        <State>NY</State>
        <Zip5>11101</Zip5>
        <Zip4/>
        </Address>
        </AddressValidateRequest>

        <?xml version="1.0" encoding="UTF-8"?>
        <AddressValidateResponse>
        <Address ID="0">
        <Address1>APT 1C</Address1>
        <Address2>3416 42ND ST</Address2>
        <City>LONG IS CITY</City>
        <State>NY</State>
        <Zip5>11101</Zip5>
        <Zip4>1290</Zip4>
        </Address>
        </AddressValidateResponse>
 */
public interface IAddress extends ICommonObject {
    void setLine1(final String line1);
    void setLine2(final String lin2);
    void setCity(final String city);
    void setState(final String state);
    void setZip1(final String zip1);
    void setZip2(final String zip2);
    //A unique hash will be generated every time
    //an address is created so that the same
    //address cannot be created with the same
    //account id. The address lines will be used
    //to generate the hash.
    void setHash(final String hash);

    String getLine1();
    String getLine2();
    String getCity();
    String getState();
    String getZip1();
    String getZip2();
    String getHash();

    void setUser(final IUser user);
    IUser getUser();

    void setAccount(final IAccount account);
    IAccount getAccount();
}
