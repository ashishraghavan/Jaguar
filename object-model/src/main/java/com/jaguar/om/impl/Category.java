package com.jaguar.om.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaguar.om.ICategory;
import com.jaguar.om.IFilter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "category",uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@AttributeOverride(name = "id",column = @Column(name = "category_id"))
public class Category extends CommonObject implements ICategory {

    public Category(){
        super();
    }

    public Category(final String name) {
        this();
        this.name = name;
    }

    public Category(final String name,final String description) {
        this(name);
        this.description = description;
    }

    @JsonIgnore
    @OneToMany(targetEntity = Category.class,cascade = CascadeType.ALL,fetch = FetchType.EAGER,mappedBy = "parentCategory",orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ICategory> childCategories;

    @JsonIgnore
    @OneToMany(targetEntity = Filter.class,cascade = CascadeType.ALL,fetch = FetchType.LAZY,mappedBy = "category",orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<IFilter> filters;

    @JsonIgnore
    @ManyToOne(targetEntity = Category.class,cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_category_id")
    @org.hibernate.annotations.ForeignKey(name = "fk_parent_category_id")
    private ICategory parentCategory;

    @Column(name = "description")
    private String description;

    @Column(name = "name",nullable = false,length = 100)
    private String name;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setParentCategory(ICategory parentCategory) {
        this.parentCategory = parentCategory;
    }


    @Override
    public void setFilters(Set<IFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void setChildCategories(Set<ICategory> childCategories) {
        this.childCategories = childCategories;
    }

    @Override
    public Set<IFilter> getFilters() {
        return this.filters;
    }

    @Override
    public ICategory getParentCategory() {
        return this.parentCategory;
    }

    @Override
    public Set<ICategory> getChildCategories() {
        return this.childCategories;
    }


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
