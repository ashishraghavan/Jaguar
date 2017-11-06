package com.jaguar.om.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaguar.om.ICategory;
import com.jaguar.om.IFilter;
import com.jaguar.om.IFilterValue;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Set;

/**
 * A filter can have multiple values associated with it. For example if Storage is
 * a filter, which applies to the category Phones, possible values will be 16GB,32GB,64GB etc.
 * If color is a filter that applies to Phones, possible values will be Red, Yellow etc...
 * If the category is specific to iPhones, possible colors will be Rose Gold, Matte Black, Space
 * Grey etc.
 */
@Entity
@Table(name = "filter",uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@AttributeOverride(name = "id",column = @Column(name = "filter_id"))
public class Filter extends CommonObject implements IFilter {

    public Filter(){
        super();
    }

    public Filter(final String name) {
        this();
        this.name = name;
    }

    @Column(name = "name",length = 100)
    private String name;

    @JsonIgnore
    @OneToMany(targetEntity = FilterValue.class,cascade = CascadeType.ALL,fetch = FetchType.LAZY,mappedBy = "filter",orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<IFilterValue> filterValues;

    @ManyToOne(targetEntity = Category.class,cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    @org.hibernate.annotations.ForeignKey(name = "fk_filter_category_id")
    private ICategory category;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setValues(Set<IFilterValue> filterValues) {
        this.filterValues = filterValues;
    }

    @Override
    public void setCategory(ICategory category) {
        this.category = category;
    }

    @Override
    public Set<IFilterValue> getFilterValues() {
        return this.filterValues;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ICategory getCategory() {
        return this.category;
    }
}
