package com.jaguar.om;

import com.jaguar.om.notification.Email;

public interface IEmailManager extends INotificationManager {
    void sendEmail(final Email email) throws Exception;
}
