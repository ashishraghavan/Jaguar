package com.jaguar.om;

import java.util.Set;

public interface ICart extends ICommonObject {
    void setUser(final IUser user);
    IUser getUser();
    //Use the ICartProduct object to describe the product and its quantity
    void setCartProducts(final Set<ICartProduct> cartProducts);
    Set<ICartProduct> getCartProducts();
}
