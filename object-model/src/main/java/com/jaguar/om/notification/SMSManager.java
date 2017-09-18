package com.jaguar.om.notification;

import com.jaguar.om.ISMSManager;
import com.jaguar.om.impl.CommonObject;
import org.apache.log4j.Logger;
import org.testng.util.Strings;


public class SMSManager extends CommonObject implements ISMSManager {

    private static final Logger smsLogger = Logger.getLogger(SMSManager.class.getSimpleName());

    @Override
    public void sendNotificationMessage(Object notificationMessage) {

    }

    @Override
    public void sendSMS(SMS sms) throws Exception {

    }

    public static SMSBuilder smsBuilder() {
        return new SMSBuilder();
    }

    public static class SMSBuilder {

        private String toPhone;
        private String fromPhone;
        private String messageBody;

        private SMSBuilder(){}

        public SMS build() {
            //Validate all fields
            if(Strings.isNullOrEmpty(toPhone)) {
                smsLogger.error("The parameter toPhone is required to construct an SMS message");
                throw new IllegalArgumentException("The parameter toPhone is required to construct an SMS message");
            }

            if(Strings.isNullOrEmpty(fromPhone)) {
                smsLogger.error("THe parameter fromPhone is required to construct an sms message");
                throw new IllegalArgumentException("The parameter fromPhone is required to construct an SMS message");
            }

            if(Strings.isNullOrEmpty(messageBody)) {
                this.messageBody = "This message does not have a body";
            }

            return new SMS(this.toPhone,this.fromPhone,this.messageBody);
        }

        public SMSBuilder toPhone(final String toPhone) {
            this.toPhone = toPhone;
            return this;
        }

        public SMSBuilder fromPhone(final String fromPhone) {
            this.fromPhone = fromPhone;
            return this;
        }

        public SMSBuilder messageBody(final String messageBody) {
            this.messageBody = messageBody;
            return this;
        }
    }
}
