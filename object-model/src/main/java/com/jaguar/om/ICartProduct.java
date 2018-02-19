package com.jaguar.om;

/**
 * An object specifying a product that is present in a cart
 * the product id and the quantity of this product.
 */
public interface ICartProduct extends ICommonObject {
    void setProduct(final IProduct product);
    IProduct getProduct();
    void setCart(final ICart cart);
    ICart getCart();
    void setProductQuantity(final int productQuantity);
    int getProductQuantity();
}
