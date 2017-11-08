package com.jaguar.om.impl;

import com.jaguar.om.ICurrency;
import com.jaguar.om.enums.CurrencySymbolPosition;

import javax.persistence.*;

@Entity
@Table(name = "currency",uniqueConstraints = {@UniqueConstraint(columnNames = {"country_code"})})
public class Currency extends CommonObject implements ICurrency {

    public Currency() {
        super();
    }

    public Currency(final String symbol,final String countryCode) {
        this();
        this.symbol = symbol;
        this.countryCode = countryCode;
    }

    @Column(name = "country_code",nullable = false,length = 2)
    private String countryCode;

    @Column(name = "country_name",nullable = false)
    private String countryName;

    @Column(name = "symbol",nullable = false,length = 3)
    private String symbol;

    @Enumerated
    @Column(name = "symbol_position",nullable = false)
    private CurrencySymbolPosition symbolPosition;

    @Override
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Override
    public void setSymbolPosition(CurrencySymbolPosition symbolPosition) {
        this.symbolPosition = symbolPosition;
    }

    @Override
    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public String getCountryCode() {
        return this.countryCode;
    }

    @Override
    public String getCountryName() {
        return this.countryName;
    }

    @Override
    public CurrencySymbolPosition getSymbolPosition() {
        return this.symbolPosition;
    }
}
