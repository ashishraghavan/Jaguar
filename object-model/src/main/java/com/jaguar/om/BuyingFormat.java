package com.jaguar.om;

public enum BuyingFormat {

    BUY_IT_NOW(1),
    AUCTION(2);

    int buyingFormat;
    BuyingFormat(final int buyingFormat) {
        this.buyingFormat = buyingFormat;
    }

    public static String[] stringValues() {
        final BuyingFormat[] values = values();
        final String[] strValues = new String[values.length];
        int count = 0;
        for(BuyingFormat buyingFormat : values) {
            strValues[count++] = String.valueOf(buyingFormat);
        }
        return strValues;
    }
}
