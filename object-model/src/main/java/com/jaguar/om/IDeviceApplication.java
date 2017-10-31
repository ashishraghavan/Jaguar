package com.jaguar.om;


public interface IDeviceApplication extends ICommonObject{

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

    IDevice getDevice();
    IApplication getApplication();
    void setDevice(final IDevice device);
    void setApplication(IApplication application);
    Authorization getAuthorization();
    void setAuthorization(final Authorization authorization);
}
