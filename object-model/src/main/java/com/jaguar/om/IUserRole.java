package com.jaguar.om;

public interface IUserRole extends ICommonObject {
    void setUser(final IUser user);
    void setRole(final IRole role);
    IRole getRole();
    IUser getUser();
}
