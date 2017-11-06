package com.jaguar.om.impl;

import com.jaguar.om.IFilter;
import com.jaguar.om.IFilterValue;

import javax.persistence.*;

@Entity
@Table(name = "filter_value",uniqueConstraints = {@UniqueConstraint(columnNames = {"filter_value_id","filter_id"})})
@AttributeOverride(name = "id",column = @Column(name = "filter_value_id"))
public class FilterValue extends CommonObject implements IFilterValue {

    //Many filter values can have one filter.
    @ManyToOne(targetEntity = Filter.class,cascade = CascadeType.ALL)
    @JoinColumn(name = "filter_id")
    @org.hibernate.annotations.ForeignKey(name = "fk_filter_value_filter_id")
    private IFilter filter;

    @Column(name = "value",length = 100)
    private String value;

    @Override
    public void setFilter(IFilter filter) {
        this.filter = filter;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public IFilter getFilter() {
        return this.filter;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
