package com.jaguar.om;


public class GenericDaoException extends Exception {
    private static final long serialVersionUID = 1L;
    public GenericDaoException(){super();}
    public GenericDaoException(Throwable throwable) {
        super(throwable);
    }
    public GenericDaoException(String message) {
        super(message);
    }
    public GenericDaoException(String message,Throwable cause) {
        super(message,cause);
    }

    public int getCode() {
        return -9999;
    }
}
