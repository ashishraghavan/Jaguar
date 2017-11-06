package com.jaguar.om;

/**
 * An Image interface which describes a product.
 * Each image belongs to a product. Needs to have
 * an association to a product.
 */
public interface IImage extends ICommonObject {
    void setProduct(final IProduct product);
    IProduct getProduct();
    void setImage(final byte[] image);
    byte[] getImage();
}
