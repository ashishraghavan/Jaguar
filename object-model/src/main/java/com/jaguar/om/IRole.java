package com.jaguar.om;


public interface IRole extends ICommonObject {
    String getName();
    void setName(final String name);
    void setDescription(final String roleDescription);
    String getDescription();
}
