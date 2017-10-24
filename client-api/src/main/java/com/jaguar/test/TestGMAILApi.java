package com.jaguar.test;

import org.testng.util.Strings;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class TestGMAILApi {
    private static final Comparator<Message> messageComparator = (message1, message2) -> {
        try {
            return Long.compare(message2.getSentDate().getTime(), message1.getSentDate().getTime());
        } catch (Exception e) {
            System.out.println("There was an error comparing message sent times with exception "+e.getLocalizedMessage());
        }
        return -1;
    };
    public static void main(String[] args) throws Exception {
        final Properties properties  = new Properties();
        properties.setProperty("mail.smtp.host","smtp.gmail.com");
        properties.setProperty("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.auth","true");
        properties.setProperty("mail.smtp.port","465");
        Session session = Session.getDefaultInstance(properties, null);

        Store store = session.getStore("imaps");
        store.connect("smtp.gmail.com", "jaguardevelopmental@gmail.com", "Halos12345");

        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);
        int messageCount = inbox.getMessageCount();

        System.out.println("Total Messages:- " + messageCount);
        // creates a search criterion
        final SearchTerm searchCondition = new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    return !(message.isSet(Flags.Flag.SEEN)) &&
                            message.getSubject().equalsIgnoreCase("Verify your registration");
                } catch (Exception e) {
                    System.out.println("Failed to process matching criteria with exception "+e.getLocalizedMessage());
                    return false;
                }
            }
        };
        // performs search through the folder
        Message[] foundMessages = inbox.search(searchCondition);
        assert foundMessages != null;
        assert foundMessages.length > 0;
        final List<Message> messageList = Arrays.asList(foundMessages);
        if(messageList.size() > 1) {
            messageList.sort(messageComparator);
        }
        //This is the message we are interested in
        final Message message = messageList.get(0);
        final Object objMessageContent = message.getContent();
        assert objMessageContent != null;
        final String messageContent = (String)objMessageContent;
        //Parse the link out of the message.
        final int indexOfScheme = messageContent.indexOf("http");
        //Mark this message as read.
        assert indexOfScheme != -1;
        final String link = messageContent.substring(indexOfScheme).trim()
                .replace("\r","").replace("\n","");
        final URI uri = URI.create(link);
        assert !Strings.isNullOrEmpty(uri.getScheme());
        assert !Strings.isNullOrEmpty(uri.getAuthority());
        assert !Strings.isNullOrEmpty(uri.getQuery());
        final String queryString = uri.getQuery();
        final String[] splitQuery = queryString.split("&");
        assert splitQuery.length > 0;
        assert splitQuery[0].equals("email");
        assert splitQuery[1].equals("code");
        assert splitQuery[2].equals("device_uid");
        assert splitQuery[3].equals("role");
        final String[] splitUserKeyValue = splitQuery[0].split("=");
        assert splitUserKeyValue.length == 2;
        final String userEmail = splitUserKeyValue[1];
        assert !Strings.isNullOrEmpty(userEmail);
        message.setFlag(Flags.Flag.SEEN,true);
        inbox.close(true);
        store.close();
    }
}
