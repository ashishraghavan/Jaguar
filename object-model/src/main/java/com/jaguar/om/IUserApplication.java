package com.jaguar.om;


public interface IUserApplication extends ICommonObject {

    enum Authorization {
        AGREE(1),
        DISAGREE(2);

        int authorization;
        Authorization(final int authInt) {
            this.authorization = authInt;
        }

        public static String[] stringValues() {
            final Authorization[] values = Authorization.values();
            final String[] strValues = new String[values.length];
            int count = 0;
            for(Authorization authorization : values) {
                strValues[count++] = String.valueOf(authorization);
            }
            return strValues;
        }
    }

    void setUser(final IUser user);
    void setApplication(IApplication application);

    IUser getUser();
    IApplication getApplication();

    void setAuthorization(final Authorization authorization);
    Authorization getAuthorization();
}
