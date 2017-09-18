package com.jaguar.om.notification;

public class Email {
    private String to;
    private String subject;
    private String body;

    private Email(final String to) {
        this.to = to;
    }

    Email(final String to,final String subject, final String body) {
        this(to);
        this.body = body;
        this.subject = subject;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
