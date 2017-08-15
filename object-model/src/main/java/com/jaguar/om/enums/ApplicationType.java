package com.jaguar.om.enums;


public enum ApplicationType {
    WEB_APP(1),
    MOBILE_APP(2);

    private Integer appType;
    ApplicationType(final Integer appType) {
        this.appType = appType;
    }
    public Integer value() {
        return this.appType;
    }
}
