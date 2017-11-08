package com.jaguar.om.enums;

public enum CurrencySymbolPosition {

    BEFORE(1),
    AFTER(2);

    private Integer symbolPosition;
    CurrencySymbolPosition(final Integer symbolPosition) {
        this.symbolPosition = symbolPosition;
    }
    public Integer value() {
        return this.symbolPosition;
    }
}
