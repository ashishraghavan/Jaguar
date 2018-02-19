package com.jaguar.om.impl;

import com.jaguar.om.ICart;
import com.jaguar.om.ICartProduct;
import com.jaguar.om.IProduct;
import org.hibernate.annotations.Type;

import javax.persistence.*;


@Entity
@Table(name = "cart_product",uniqueConstraints = {@UniqueConstraint(columnNames = {"cart_id","product_id"})})
@AttributeOverride(name = "id",column = @Column(name = "cart_product_id"))
public class CartProduct extends CommonObject implements ICartProduct {

    public CartProduct() {
        super();
    }

    @ManyToOne(targetEntity = Cart.class,optional = false)
    @JoinColumn(name = "cart_id",nullable = false)
    @org.hibernate.annotations.ForeignKey(name = "fk_cart_product_cart_id")
    private ICart cart;

    @ManyToOne(targetEntity = Product.class,optional = false)
    @JoinColumn(name = "product_id",nullable = false)
    @org.hibernate.annotations.ForeignKey(name = "fk_cart_product_product_id")
    private IProduct product;

    @Column(name = "product_quantity")
    @Type(type = "org.hibernate.type.IntegerType")
    private Integer productQuantity;

    public CartProduct(IProduct product,ICart cart) {
        this();
        this.product = product;
        this.cart = cart;
    }

    @Override
    public void setProduct(IProduct product) {
        this.product = product;
    }

    @Override
    public IProduct getProduct() {
        return this.product;
    }

    @Override
    public void setCart(ICart cart) {
        this.cart = cart;
    }

    @Override
    public ICart getCart() {
        return this.cart;
    }

    @Override
    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    @Override
    public int getProductQuantity() {
        return this.productQuantity;
    }
}
