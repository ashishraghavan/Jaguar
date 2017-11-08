package com.jaguar.om.impl;

import com.jaguar.om.*;
import com.jaguar.om.common.Utils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "product",uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id","product_id"})})
@AttributeOverride(name = "id",column = @Column(name = "product_id"))
public class Product extends CommonObject implements IProduct{

    public Product() {
        super();
        //Generate the item number
        this.itemNumber = Utils.generateItemNumber();
    }

    public Product(final String itemNumber) {
        this.itemNumber = itemNumber;
    }

    //Name column with default length
    @Column(name = "title",nullable = false)
    private String title;

    //description column with default length.
    @Column(name = "description",nullable = false)
    private String description;

    @Column(name = "price",nullable = false,precision = 10,scale = 2)
    private float price;

    @Column(name = "upc",nullable = false)
    private String upc;

    @Column(name = "mpn",nullable = false)
    private String mpn;

    @Column(name = "item_number",nullable = false)
    private String itemNumber;

    @ManyToOne(targetEntity = User.class,optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    @org.hibernate.annotations.ForeignKey(name = "fk_product_user_id")
    private IUser user;

    @Column(name = "buying_format",nullable = false)
    @Enumerated(EnumType.STRING)
    private BuyingFormat buyingFormat;

    //One product can be under multiple categories.
    @ManyToOne(targetEntity = Category.class,cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    @org.hibernate.annotations.ForeignKey(name = "fk_product_category_id")
    private ICategory category;

    @OneToMany(targetEntity = Image.class,mappedBy = "product",fetch = FetchType.LAZY,orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<IImage> productImages;

    @ManyToOne(targetEntity = Currency.class,cascade = CascadeType.ALL,optional = false)
    @JoinColumn(name = "currency_id")
    @org.hibernate.annotations.ForeignKey(name = "fk_product_currency_id")
    private ICurrency currency;

    public Product(final String title,final String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public Product(final String title,final String description,float price) {
        this(title,description);
        this.price = price;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public void setBuyingFormat(BuyingFormat buyingFormat) {
        this.buyingFormat = buyingFormat;
    }

    @Override
    public void setCategory(ICategory category) {
        this.category = category;
    }

    @Override
    public void setImages(Set<IImage> productImages) {
        this.productImages = productImages;
    }

    @Override
    public void setUser(IUser user) {
        this.user = user;
    }

    @Override
    public void setUPC(String upc) {
        this.upc = upc;
    }

    @Override
    public void setMPN(String mpn) {
        this.mpn = mpn;
    }

    @Override
    public void setCurrency(ICurrency currency) {
        this.currency = currency;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public float getPrice() {
        return this.price;
    }

    @Override
    public BuyingFormat getBuyingFormat() {
        return this.buyingFormat;
    }

    @Override
    public ICategory getCategory() {
        return this.category;
    }

    @Override
    public Set<IImage> getProductImages() {
        return this.productImages;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }

    @Override
    public String getUPC() {
        return this.upc;
    }

    @Override
    public String getMPN() {
        return this.mpn;
    }

    @Override
    public String getItemNumber() {
        return this.itemNumber;
    }

    @Override
    public ICurrency getCurrency() {
        return this.currency;
    }
}
