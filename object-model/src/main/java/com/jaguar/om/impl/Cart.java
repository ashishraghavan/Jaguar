package com.jaguar.om.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaguar.om.ICart;
import com.jaguar.om.ICartProduct;
import com.jaguar.om.IUser;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "cart",uniqueConstraints = {@UniqueConstraint(columnNames = {"cart_id","user_id"})})
@AttributeOverride(name = "id",column = @Column(name = "cart_id"))
public class Cart extends CommonObject implements ICart {

    public Cart() {
        super();
    }

    public Cart(final IUser user) {
        this();
        this.user = user;
    }

    public Cart(final IUser user,Set<ICartProduct> cartProducts) {
        this();
        this.user = user;
        this.cartProducts = cartProducts;
    }

    @OneToOne(targetEntity = User.class,optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    @org.hibernate.annotations.ForeignKey(name = "fk_cart_user_id")
    private IUser user;

    @JsonIgnore
    @OneToMany(targetEntity = CartProduct.class,cascade = CascadeType.ALL,fetch = FetchType.LAZY,mappedBy = "product",orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ICartProduct> cartProducts;

    @Override
    public void setUser(IUser user) {
        this.user = user;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }


    @Override
    public void setCartProducts(Set<ICartProduct> cartProducts) {
        this.cartProducts = cartProducts;
    }

    @Override
    public Set<ICartProduct> getCartProducts() {
        return this.cartProducts;
    }
}
