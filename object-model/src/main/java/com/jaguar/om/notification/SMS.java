package com.jaguar.om.notification;

import com.jaguar.om.Message;

public class SMS extends Message {
    private String toPhone;
    private String messageBody;

    SMS(final String toPhone, final String messageBody) {
        this.toPhone = toPhone;
        this.messageBody = messageBody;
    }

    public String getToPhone() {
        return toPhone;
    }

    public void setToPhone(String toPhone) {
        this.toPhone = toPhone;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }
}
