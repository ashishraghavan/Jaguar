package com.jaguar.om.impl;

import com.jaguar.om.IImage;
import com.jaguar.om.IProduct;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "product_image",uniqueConstraints = {@UniqueConstraint(columnNames = {"product_image_id","product_id"})})
@AttributeOverride(name = "id",column = @Column(name = "product_image_id"))
public class Image extends CommonObject implements IImage {

    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "background_image", nullable = false)
    private byte[] productImage;

    @ManyToOne(targetEntity = Product.class,optional = false)
    @JoinColumn(name = "product_id",nullable = false)
    @org.hibernate.annotations.ForeignKey(name = "fk_image_product_id")
    private IProduct product;

    @Column(name = "file_name",nullable = false)
    private String fileName;

    public Image(){super();}

    public Image(final byte[] productImage, IProduct product) {
        this();
        this.productImage = productImage;
        this.product = product;
    }

    @Override
    public void setProduct(IProduct product) {
        this.product = product;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public IProduct getProduct() {
        return this.product;
    }

    @Override
    public void setProductImage(byte[] productImage) {
        this.productImage = productImage;
    }

    @Override
    public byte[] getProductImage() {
        return this.productImage;
    }
}
