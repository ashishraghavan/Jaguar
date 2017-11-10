package com.jaguar.om;

import com.jaguar.om.enums.CurrencySymbolPosition;

/**
 * An interface that describes the currency, the symbol, its position within the
 * currency string.
 */
public interface ICurrency extends ICommonObject {
    void setSymbol(final String symbol);
    //This is the two letter country code.
    void setCountryCode(final String countryCode);
    //Set the full country name.
    void setCountryName(final String countryName);
    void setSymbolPosition(final CurrencySymbolPosition symbolPosition);
    void setCurrencyName(final String currencyName);

    String getSymbol();
    String getCountryCode();
    String getCountryName();
    CurrencySymbolPosition getSymbolPosition();
    String getCurrencyName();
}
