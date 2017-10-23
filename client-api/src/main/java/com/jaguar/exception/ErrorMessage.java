package com.jaguar.exception;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.jaguar.common.CommonService;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.testng.util.Strings;

import java.io.StringWriter;
import java.util.Map;

public class ErrorMessage extends CommonService {

    private Integer errorCode;
    private String errorMessage;
    private static final Logger logger = Logger.getLogger(ErrorMessage.class.getSimpleName());
    private static final String serverErrorJSON = "{\"errorCode\":\"500\"," +
            "\"errorMessage\":\"The server encountered an error while processing this request\"}";
    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final StringWriter stringWriter = new StringWriter();
    static JsonGenerator jsonGenerator;

    static {
        try {
            jsonGenerator = jsonFactory.createGenerator(stringWriter);
        } catch (Exception e) {
            logger.error("There was an error creating the JSON generator");
        }
    }

    /* Error code declaration */
    public static final int ARGUMENT_REQUIRED = 1;
    public static final int INVALID_ARGUMENT = 2;
    public static final int EXCEPTION = 3;
    public static final int INVALID_HASH = 4;
    public static final int NULL_OBJECT_FROM_QUERY = 5;
    public static final int COOKIE_NOT_VALID = 6;
    public static final int FREE_FORM = 7;
    public static final int NOT_FOUND = 8;
    public static final int INVALID_SESSION = 9;
    public static final int INTERNAL_SERVER_ERROR = 10;
    public static final int NOT_AUTHORIZED = 11;
    public static final int LINK_EXPIRED = 12;

    public static Builder builder() {
        return new Builder();
    }

    private static final Map<Integer,String> errorCodeMap = ImmutableMap.<Integer,String>builder()
            .put(ARGUMENT_REQUIRED,"Argument %s is required for this request")
            .put(INVALID_ARGUMENT,"Argument %s is not in the correct format. Expected format is %s")
            .put(EXCEPTION,"An error occurred with message %s")
            //Do not send the value of the correct hash here! The first string argument is the hash sent by the client.
            .put(INVALID_HASH,"The computed hash %s is incorrect.")
            .put(NULL_OBJECT_FROM_QUERY,"The query for %s returned empty.")
            .put(COOKIE_NOT_VALID,"Invalid cookie. Please re-verify and try again")
            .put(FREE_FORM,"%s")
            .put(NOT_FOUND,"%s was not found on this system")
            .put(INVALID_SESSION,"%s has an invalid session. Please re-validate")
            .put(INTERNAL_SERVER_ERROR,"An error occurred while processing this request")
            .put(NOT_AUTHORIZED,"Authentication is required for this request")
            .put(LINK_EXPIRED,"This link has expired. Please try re-sending the link and try again")
            .build();
    /**
     * Try getting the error message from
     * @param errorCode The error code contained within the {@link #errorCodeMap}
     * @param errorMessage The substituted error message contained within {@link #errorCodeMap}
     */
    private ErrorMessage(final Integer errorCode, final String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "Error code = "+String.valueOf(errorCode) + ", Error message = "+errorMessage;
    }

    public static class Builder {
        private String[] message;
        private Integer errorCode;
        private String substitutedMessage;

        private Builder() {}

        public Builder withErrorCode(final Integer errorCode) {
            if(!errorCodeMap.containsKey(errorCode)) {
                throw new IllegalArgumentException("The error code "+errorCode+" is not a valid error code.");
            }
            this.errorCode = errorCode;
            return this;
        }

        public Builder withMessage(final String... message) {
            this.message = message;
            return this;
        }

        public String build() {
            if(jsonGenerator == null) {
                throw new IllegalStateException("Can't build an instance of "+ErrorMessage.class.getSimpleName()+" without having a JSON generator");
            }
            //The number of %s should be equal to the message length.
            final int countOfStrFormat = StringUtils.countOccurrencesOf(errorCodeMap.get(errorCode),"%s");
            if(this.message != null && countOfStrFormat != this.message.length) {
                throw new IllegalArgumentException("Expected the number of arguments to be "+countOfStrFormat+" but found "+this.message.length);
            }
            try {
                if(countOfStrFormat > 0) {
                    final Object[] objectArgs = new Object[countOfStrFormat];
                    if(this.message != null) {
                        for(int i =0;i<this.message.length;i++) {
                            objectArgs[i++] = this.message[i];
                        }
                    }
                    //We only need to format the string if there are any string identifiers (%s)
                    this.substitutedMessage = String.format(errorCodeMap.get(errorCode),objectArgs);
                } else {
                    //Other wise, we directly use the original message in the map.
                    this.substitutedMessage = errorCodeMap.get(errorCode);
                }

            } catch (Exception e) {
                logger.error("Formatting with arguments "+stringifyArray(message)+" failed with message "+e.getMessage());
                throw new IllegalArgumentException("Expected the number of arguments to be "+countOfStrFormat+" but found "+this.message.length);
            }

            //We can write a different method which does not convert to JSON always.
            try {
                if(jsonGenerator.isClosed()) {
                    jsonGenerator = jsonFactory.createGenerator(stringWriter);
                }
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("code");
                jsonGenerator.writeNumber(this.errorCode);
                jsonGenerator.writeFieldName("message");
                jsonGenerator.writeString(this.substitutedMessage);
                jsonGenerator.writeEndObject();
                jsonGenerator.flush();
                stringWriter.flush();
                stringWriter.close();
                return stringWriter.toString();
            } catch (Exception e) {
                logger.error("There was an error serializing the ErrorMessage object with message "+this.substitutedMessage+" and errorCode "+this.errorCode+" with exception "+e.getLocalizedMessage());
                logger.info("Sending an internal server error response.");
                return serverErrorJSON;
            } finally {
                if(jsonGenerator != null) {
                    try {
                        jsonGenerator.close();
                    } catch (Exception ignore){}
                }
            }
        }

        private String stringifyArray(final String[] strArray) {
            if(strArray == null || strArray.length <= 0) {
                return "";
            }
            final StringBuilder stringBuilder = new StringBuilder();
            for(String key : strArray) {
                if(!Strings.isNullOrEmpty(key)) {
                    stringBuilder.append(key);
                }
            }
            return stringBuilder.toString();
        }
    }
}
