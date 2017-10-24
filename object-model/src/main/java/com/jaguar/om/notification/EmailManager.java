package com.jaguar.om.notification;


import com.jaguar.om.IEmailManager;
import com.jaguar.om.impl.CommonObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Component
public class EmailManager extends CommonObject implements IEmailManager {

    private static final Logger emailLogger = Logger.getLogger(EmailManager.class.getSimpleName());
    private static final String mail__smtp_host = "mail.smtp.host";
    private static final String mail_smtp_host_value = "smtp.mailgun.org";
    private static final String mail_smtp_port = "mail.smtp.port";
    private static final String mail_smtp_port_value = "456";
    private static final String mail_smtp_auth = "mail.smtp.auth";
    private static final String mail_smtp_auth_value = "true";
    private static final String mail_smtp_socketfactory_class = "mail.smtp.socketFactory.class";
    private static final String getMail_smtp_socketfactory_class_value = "javax.net.ssl.SSLSocketFactory";
    private static final String mail_smtp_socketFactory_port = "mail.smtp.socketFactory.port";
    private static final String getMail_smtp_socketFactory_port_value = "465";
    private static final String USERNAME = "postmaster@ashishraghavan.me";
    private static final String PASSWORD = "52759edfe8f6a63c00dd2fd8ff4d75cd";

    //Key Values for mail message header.
    private static final String CONTENT_TYPE = "Content-type";
    private static final String CONTENT_TYPE_VALUE = "text/HTML; charset=UTF-8";
    private static final String FORMAT = "format";
    private static final String FORMAT_VALUE = "flowed";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    private static final String CONTENT_TRANSFER_ENCODING_VALUE = "8bit";


    public static EmailBuilder builder() {
        return new EmailBuilder();
    }

    @Override
    public void sendEmail(Email email) throws Exception {
        final Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME,PASSWORD);
            }
        };
        final Properties properties = new Properties();
        properties.setProperty(mail__smtp_host,mail_smtp_host_value);
        properties.setProperty(mail_smtp_port,mail_smtp_port_value);
        properties.setProperty(mail_smtp_auth,mail_smtp_auth_value);
        properties.setProperty(mail_smtp_socketfactory_class,getMail_smtp_socketfactory_class_value);
        properties.setProperty(mail_smtp_socketFactory_port,getMail_smtp_socketFactory_port_value);
        final Session session = Session.getInstance(properties,authenticator);
        //Set the debug flag to true.
        session.setDebug(true);
        final MimeMessage message = new MimeMessage(session);
        message.addHeader(CONTENT_TYPE,CONTENT_TYPE_VALUE);
        message.addHeader(FORMAT,FORMAT_VALUE);
        message.addHeader(CONTENT_TRANSFER_ENCODING,CONTENT_TRANSFER_ENCODING_VALUE);
        message.setFrom(USERNAME);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getTo()));
        message.setSubject(email.getSubject());
        message.setText(email.getBody(),"UTF-8");
        Transport.send(message);
    }

    @Override
    public void sendNotificationMessage(com.jaguar.om.Message notificationMessage) throws Exception {
        if(!(notificationMessage instanceof Email)) {
            emailLogger.error("Expected notification message instance to be of type "+Email.class.getSimpleName()+", but found "+notificationMessage.getClass().getSimpleName());
            throw new IllegalArgumentException("\"Expected notification message instance to be of type "+Email.class.getSimpleName()+", but found "+notificationMessage.getClass().getSimpleName());
        }
        final Email email = (Email)notificationMessage;
        sendEmail(email);
    }

    public static class EmailBuilder {

        private String to;
        private String subject;
        private String body;

        private EmailBuilder(){}

        public Email build() {
            //Validate all fields.
            if(Strings.isNullOrEmpty(to)) {
                emailLogger.error("The parameter to is required to construct an email message");
                throw new IllegalArgumentException("The parameter to is required to construct an email message");
            }

            if(Strings.isNullOrEmpty(subject)) {
                this.subject = "No Subject was entered.";
            }

            if(Strings.isNullOrEmpty(body)) {
                this.body = "No body was entered for this message.";
            }

            return new Email(this.to,this.subject,this.body);
        }

        public EmailBuilder to(final String to) {
            this.to = to;
            return this;
        }

        public EmailBuilder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        public EmailBuilder body(final String body) {
            this.body = body;
            return this;
        }
    }

    public static void main(String[] args) throws Exception {
        final IEmailManager emailManager = new EmailManager();
        final Email email = EmailManager.builder()
                .body("Please verify yourself by clicking on the following link.\n" +
                        "\n" +
                        "http://localhost:8080/client/api/user/verify?email=ashishraghavan13687@gmail.com&code=b3916062-1aa6-4e8a-8ceb-4f776b03b0e3&device_uid=iOS6sPlus-A1687&role=seller")
                .to("jaguardevelopmental@gmail.com")
                .subject("Verify your registration")
                .build();
        for(int i = 0;i<3;i++) {
            emailManager.sendEmail(email);
        }
    }
}
