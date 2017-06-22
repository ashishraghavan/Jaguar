package com.jaguar.om.enums;


public enum EOrder {
    ASCENDING(0),
    DESCENDING(1);

    private int value;

    private EOrder(int value) {
        this.value = value;
    }
    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
