package com.jaguar.om;


public interface IApplicationRole extends ICommonObject{
    IRole getRole();
    void setRole(final IRole role);
    IApplication getApplication();
    void setApplication(IApplication application);
}
