package com.jaguar.om.notification;

public class SMS {
    private String toPhone;
    private String fromPhone;
    private String messageBody;

    SMS(final String toPhone, final String fromPhone, final String messageBody) {
        this.toPhone = toPhone;
        this.fromPhone = fromPhone;
        this.messageBody = messageBody;
    }

    public String getToPhone() {
        return toPhone;
    }

    public void setToPhone(String toPhone) {
        this.toPhone = toPhone;
    }

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }
}
