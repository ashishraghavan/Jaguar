package com.jaguar.om;


import java.util.Set;

public interface IApplication extends ICommonObject {
    String getName();
    void setName(final String name);
    Integer getClientId();
    void setClientId(final Integer clientId);
    String getClientSecret();
    void setClientSecret(final String clientSecret);
    String getRedirectURI();
    void setRedirectURI(final String redirectURI);
    Set<IApplicationRole> getRoles();
    void setRoles(Set<IApplicationRole> roles);
    IAccount getAccount();
    void setAccount(final IAccount account);
    //This corresponds to the bundle version (incremental or not)
    String getVersionCode();
    void setVersionCode(final String versionCode);
    //Corresponds to the android package name.
    String getPackageName();
    void setPackageName(final String packageName);
}