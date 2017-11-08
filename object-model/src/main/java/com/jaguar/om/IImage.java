package com.jaguar.om;

/**
 * An Image interface which describes a product.
 * Each image belongs to a product. Needs to have
 * an association to a product.
 */
public interface IImage extends ICommonObject {
    void setProduct(final IProduct product);
    void setFileName(final String fileName);
    String getFileName();
    IProduct getProduct();
    void setProductImage(final byte[] productImage);
    byte[] getProductImage();
}
