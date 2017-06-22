package com.jaguar.om;


import java.util.Date;

public interface ICommonObject {

    Long getId();

    void setId(Long id);

    Date getCreationDate();

    void setCreationDate(Date date);

    Date getModificationDate();

    boolean isActive();

    void setActive(boolean isActive);

}
