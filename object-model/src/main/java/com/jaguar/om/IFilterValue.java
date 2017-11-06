package com.jaguar.om;

/**
 * A IFilterValue object describing a possible value for a filter object.
 * Must contain a filter object as an association.
 */
public interface IFilterValue extends ICommonObject {
    void setFilter(final IFilter filter);
    void setValue(final String value);

    IFilter getFilter();
    String getValue();
}
