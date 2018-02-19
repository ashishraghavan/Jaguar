package com.jaguar.om.notification;


import com.jaguar.om.IEmailManager;
import com.jaguar.om.impl.CommonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;

@Component
public class EmailManager extends CommonObject implements IEmailManager {

    private static final Logger emailLogger = Logger.getLogger(EmailManager.class.getSimpleName());
    private static final String DOMAIN = "www.dev-jaguar.xyz";
    private static final String API_URL = String.format("https://api.mailgun.net/v3/%s/messages",DOMAIN);
    private static final String API_AUTH_NAME = "api";
    private static final String API_AUTH_VALUE = "key-910ae7b7d0722488c0951dfce679fe76";
    private static final String FROM = "from";
    private static final String FROM_VALUE = "Ashish Raghavan <ashish.raghavan@www.dev-jaguar.xyz>";
    private static final String TO = "to";
    private static final String SUBJECT = "subject";
    private static final String TEXT = "text";

    public static EmailBuilder builder() {
        return new EmailBuilder();
    }
//{"id":"<20180219171555.1.390BA4669DA4A6A7@www.dev-jaguar.xyz>","message":"Queued. Thank you."}
    @Override
    public void sendEmail(Email email) throws Exception {
        HttpResponse<JsonNode> request = Unirest.post(API_URL)
                .basicAuth(API_AUTH_NAME, API_AUTH_VALUE)
                .queryString(FROM, FROM_VALUE)
                .queryString(TO, email.getTo())
                .queryString(SUBJECT, email.getSubject())
                .queryString(TEXT,email.getBody())
                .asJson();
        if(request.getStatus() != org.apache.http.HttpStatus.SC_OK) {
            final String errorMessage = "There was an error sending the email message to "+email.getTo()+" with exception "+request.getStatusText();
            emailLogger.error(errorMessage);
            throw new Exception(errorMessage);
        }
        //Check if we got an id in the JSON response.
        if(request.getBody().getObject() == null) {
            emailLogger.error("There was an error obtaining the JSON result object from the JSON response");
            throw new Exception("There was an error obtaining the JSON result object from the JSON response");
        }
        final JSONObject jsonObject = request.getBody().getObject();
        if(jsonObject.get("id") == null) {
            emailLogger.error("The JSON object does not contain the key id (no message id found)");
            throw new Exception("The JSON object does not contain the key id (no message id found)");
        }
        if(jsonObject.get("message") == null) {
            emailLogger.error("The JSON object does not contain the key message (no message found)");
            throw new Exception("The JSON object does not contain the key message (no message found)");
        }
        //At this point we can be sure that the email was sent successfully or atleast queued to be sent.
        emailLogger.info("The email message was sent/queued to be sent successfully");
        emailLogger.info("Headers : "+request.getHeaders());
        emailLogger.info("Status Text : "+request.getStatusText());
        emailLogger.info("Status : "+request.getStatus());
        emailLogger.info("Body:"+request.getBody());
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
        emailManager.sendEmail(email);
    }
}
