package com.jaguar.om;

import java.util.Set;

public interface IProduct extends ICommonObject {

    void setTitle(final String title);
    void setDescription(final String description);
    void setPrice(final float price);
    void setBuyingFormat(BuyingFormat buyingFormat);
    void setCategory(final ICategory category);
    void setImages(final Set<IImage> productImages);
    void setUser(final IUser user);
    void setUPC(final String upc);
    void setCurrency(final ICurrency currency);

    String getTitle();
    String getDescription();
    float getPrice();
    BuyingFormat getBuyingFormat();
    ICategory getCategory();
    Set<IImage> getProductImages();
    IUser getUser();
    String getUPC();
    String getItemNumber();
    ICurrency getCurrency();
}
