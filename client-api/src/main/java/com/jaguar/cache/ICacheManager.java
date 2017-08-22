package com.jaguar.cache;


import com.jaguar.om.IApplication;
import com.jaguar.om.IUser;
import jersey.repackaged.com.google.common.cache.Cache;

public interface ICacheManager {
    Cache<String, IApplication> getAppCache();
    Cache<IUser, IApplication> getTokenCache();
}
