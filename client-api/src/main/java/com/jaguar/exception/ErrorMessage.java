package com.jaguar.exception;


import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.testng.util.Strings;

import java.util.Map;

public class ErrorMessage {

    private Integer errorCode;
    private String errorMessage;
    private static final Logger logger = Logger.getLogger(ErrorMessage.class.getSimpleName());

    public enum ErrorCode {
        ARGUMENT_REQUIRED(1),
        INVALID_ARGUMENT(2),
        EXCEPTION(3),
        INVALID_HASH(4),
        NULL_OBJECT_FROM_QUERY(5);

        private Integer argumentCode;
        ErrorCode(final Integer argumentCode) {
            this.argumentCode = argumentCode;
        }
        public Integer getArgumentCode() {
            return this.argumentCode;
        }
    }

    private static final Map<Integer,String> errorCodeMap = ImmutableMap.<Integer,String>builder()
            .put(ErrorCode.ARGUMENT_REQUIRED.argumentCode,"Argument %s is required for this request")
            .put(ErrorCode.INVALID_ARGUMENT.argumentCode,"Argument %s is not in the correct format. Expected format is %s")
            .put(ErrorCode.EXCEPTION.argumentCode,"An error occurred with message %s")
            //Do not send the value of the correct hash here! The first string argument is the hash sent by the client.
            .put(ErrorCode.INVALID_HASH.argumentCode,"The computed hash %s is incorrect.")
            .put(ErrorCode.NULL_OBJECT_FROM_QUERY.argumentCode,"The query for %s returned empty.")
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

        public Builder() {}

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

        public ErrorMessage build() {
            //The number of %s should be equal to the message length.
            final int countOfStrFormat = StringUtils.countOccurrencesOf(errorCodeMap.get(errorCode),"%s");
            if(countOfStrFormat != this.message.length) {
                throw new IllegalArgumentException("Expected the number of arguments to be "+countOfStrFormat+" but found "+this.message.length);
            }
            try {
                this.substitutedMessage = String.format(errorCodeMap.get(errorCode),this.message);
            } catch (Exception e) {
                logger.error("Formatting with arguments "+stringifyArray(message)+" failed with message "+e.getMessage());
                throw new IllegalArgumentException("Expected the number of arguments to be "+countOfStrFormat+" but found "+this.message.length);
            }
            return new ErrorMessage(this.errorCode,this.substitutedMessage);
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
