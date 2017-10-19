package com.jaguar.om;


public interface INotificationManager extends ICommonObject {
    void sendNotificationMessage(final Message notificationMessage) throws Exception;
}
