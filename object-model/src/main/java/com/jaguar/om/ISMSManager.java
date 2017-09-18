package com.jaguar.om;

import com.jaguar.om.notification.SMS;

public interface ISMSManager extends INotificationManager {
    void sendSMS(final SMS sms) throws Exception;
}
