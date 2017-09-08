package com.jaguar.exception;


import com.google.gson.Gson;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.testng.util.Strings;

import java.util.Map;

public class ErrorMessage {

    private Integer errorCode;
    private String errorMessage;
    private static final Logger logger = Logger.getLogger(ErrorMessage.class.getSimpleName());
    private static final Gson gson = new Gson();

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
            //The number of %s should be equal to the message length.
            final int countOfStrFormat = StringUtils.countOccurrencesOf(errorCodeMap.get(errorCode),"%s");
            if(this.message != null && countOfStrFormat != this.message.length) {
                throw new IllegalArgumentException("Expected the number of arguments to be "+countOfStrFormat+" but found "+this.message.length);
            }
            try {
                if(countOfStrFormat > 0) {
                    //We only need to format the string if there are any string identifiers (%s)
                    this.substitutedMessage = String.format(errorCodeMap.get(errorCode),this.message);
                } else {
                    //Other wise, we directly use the original message in the map.
                    this.substitutedMessage = errorCodeMap.get(errorCode);
                }

            } catch (Exception e) {
                logger.error("Formatting with arguments "+stringifyArray(message)+" failed with message "+e.getMessage());
                throw new IllegalArgumentException("Expected the number of arguments to be "+countOfStrFormat+" but found "+this.message.length);
            }

            //We can write a different method which does not convert to JSON always.
            return gson.toJson(new ErrorMessage(this.errorCode,this.substitutedMessage));
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
