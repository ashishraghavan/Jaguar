package com.jaguar.om.notification;

import com.jaguar.om.ISMSManager;
import com.jaguar.om.Message;
import com.jaguar.om.impl.CommonObject;
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.message.MessageResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;

import java.util.LinkedHashMap;

@Component
public class SMSManager extends CommonObject implements ISMSManager {

    private static final Logger smsLogger = Logger.getLogger(SMSManager.class.getSimpleName());
    private static final String AUTH_ID = "MAMWQWMGU1ZDY5YJZJMM";
    private static final String AUTH_TOKEN = "M2E3N2IwY2YxNzBhNGQ5NmUzYTJjMTVlNjBhNzAw";
    private static final String FROM_NUMBER = "+19179935471";
    private final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();

    @Override
    public void sendNotificationMessage(Message notificationMessage) throws Exception {
        if(!(notificationMessage instanceof SMS)) {
            smsLogger.error("Expected notificationMessage to be an instance of "+SMS.class.getSimpleName()+" but found "+notificationMessage.getClass().getSimpleName());
            throw new IllegalArgumentException("Expected notificationMessage to be an instance of "+SMS.class.getSimpleName() +"but found "+notificationMessage.getClass().getSimpleName());
        }
        final SMS sms = (SMS)notificationMessage;
        sendSMS(sms);
    }

    @Override
    public void sendSMS(SMS sms) throws Exception {
        final RestAPI api = new RestAPI(AUTH_ID, AUTH_TOKEN, "v1");
        parameters.clear();
        parameters.put("src", FROM_NUMBER); // Sender's phone number with country code
        parameters.put("dst", sms.getToPhone()); // Receiver's phone number with country code
        parameters.put("text", sms.getMessageBody()); // Your SMS text message
        MessageResponse msgResponse = api.sendMessage(parameters);
        smsLogger.info("Api ID : " + msgResponse.apiId);
        smsLogger.info("Message : " + msgResponse.message);
    }

    public static SMSBuilder smsBuilder() {
        return new SMSBuilder();
    }

    public static class SMSBuilder {
        private String toPhone;
        private String messageBody;
        private SMSBuilder(){}

        public SMS build() {
            //Validate all fields
            if(Strings.isNullOrEmpty(toPhone)) {
                smsLogger.error("The parameter toPhone is required to construct an SMS message");
                throw new IllegalArgumentException("The parameter toPhone is required to construct an SMS message");
            }

            if(Strings.isNullOrEmpty(messageBody)) {
                this.messageBody = "This message does not have a body";
            }

            return new SMS(this.toPhone,this.messageBody);
        }

        public SMSBuilder toPhone(final String toPhone) {
            this.toPhone = toPhone;
            return this;
        }

        public SMSBuilder messageBody(final String messageBody) {
            this.messageBody = messageBody;
            return this;
        }
    }
}
