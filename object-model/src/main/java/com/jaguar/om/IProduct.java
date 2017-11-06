package com.jaguar.om;

import java.util.Set;

public interface IProduct extends ICommonObject {

    void setTitle(final String title);
    void setDescription(final String description);
    void setPrice(final float price);
    void setBuyingFormat(BuyingFormat buyingFormat);
    void setCategory(final Set<ICategory> categories);
    void setImages(final Set<IImage> images);

    String getTitle();
    String getDescription();
    float getPrice();
    BuyingFormat getBuyingFormat();
    Set<ICategory> getCategories();
    Set<IImage> getImages();
}
